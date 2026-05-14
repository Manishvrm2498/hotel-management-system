package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.exception.BadRequestException;
import com.university.project.hotelmanagement.entity.Booking;
import com.university.project.hotelmanagement.entity.Hotel;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.repository.BookingRepository;
import com.university.project.hotelmanagement.repository.HotelRepository;
import com.university.project.hotelmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class OllamaService {

    private static final String SYSTEM_INSTRUCTION =
            "You are an AI assistant for the AR Hotel Management system. "
                    + "You must only answer hotel-management related questions. "
                    + "You can help with hotel suggestions, hotel search by state/district/rating, user booking details, "
                    + "last booking information, room guidance, payments, receipts, and reviews. "
                    + "Use only the provided application data for hotel names and booking facts. "
                    + "If the user asks outside hotel management, politely say you can only help with hotel-related topics.";

    private final WebClient webClient;
    private final String baseUrl;
    private final String model;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public OllamaService(
            WebClient.Builder webClientBuilder,
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:llama3.2}") String model,
            HotelRepository hotelRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository
    ) {
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.model = model;
        this.hotelRepository = hotelRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    public String chat(String userPrompt) {
        String prompt = userPrompt == null ? "" : userPrompt.trim();
        if (prompt.isEmpty()) {
            throw new BadRequestException("Prompt is required");
        }

        AiContext context = buildContext(prompt);
        String contextualPrompt = """
                User question:
                %s

                Application data available to you:
                %s

                Answer rules:
                - Reply in the same language style as the user when possible.
                - Be concise and practical.
                - For hotel recommendations, mention hotel name, district, state, rating, and why it matches.
                - For booking questions, only use the authenticated user's booking data shown above.
                - If matching hotel data is empty, clearly say no matching hotels are available in the database.
                """.formatted(prompt, context.summary());

        ChatRequest body = new ChatRequest(
                model,
                List.of(
                        new Message("system", SYSTEM_INSTRUCTION),
                        new Message("user", contextualPrompt)
                ),
                false
        );

        try {
            ChatResponse response = webClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .block();

            return extractText(response);
        } catch (WebClientResponseException ex) {
            String responseBody = ex.getResponseBodyAsString();
            String message = (responseBody == null || responseBody.isBlank())
                    ? "Ollama request failed with status " + ex.getStatusCode().value()
                    : "Ollama request failed with status "
                    + ex.getStatusCode().value()
                    + ": "
                    + responseBody;
            return fallbackAnswer(prompt, context, message);
        } catch (WebClientRequestException ex) {
            return fallbackAnswer(
                    prompt,
                    context,
                    "Could not connect to Ollama at " + baseUrl + ". Make sure Ollama is running and the model is available."
            );
        }
    }

    private AiContext buildContext(String prompt) {
        List<Hotel> allHotels = hotelRepository.findAll();
        HotelFilters filters = extractFilters(prompt, allHotels);
        List<Hotel> matchingHotels = allHotels.stream()
                .filter(hotel -> filters.state().isEmpty()
                        || containsIgnoreCase(hotel.getState(), filters.state().orElse("")))
                .filter(hotel -> filters.district().isEmpty()
                        || containsIgnoreCase(hotel.getDistrict(), filters.district().orElse("")))
                .filter(hotel -> filters.rating().isEmpty()
                        || hotel.getRating() >= filters.rating().orElse(0.0))
                .sorted(Comparator.comparing(Hotel::getRating).reversed())
                .limit(8)
                .toList();

        Optional<UserEntity> currentUser = getAuthenticatedUser();
        List<Booking> bookings = currentUser
                .map(user -> bookingRepository.findAllBookingsByUserId(user.getId()).stream()
                        .sorted(Comparator.comparing(
                                Booking::getBookedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        ))
                        .limit(8)
                        .toList())
                .orElse(List.of());

        return new AiContext(filters, matchingHotels, currentUser, bookings);
    }

    private HotelFilters extractFilters(String prompt, List<Hotel> hotels) {
        String normalizedPrompt = normalize(prompt);
        Optional<String> state = hotels.stream()
                .map(Hotel::getState)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .filter(value -> normalizedPrompt.contains(normalize(value)))
                .findFirst();

        Optional<String> district = hotels.stream()
                .map(Hotel::getDistrict)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .filter(value -> normalizedPrompt.contains(normalize(value)))
                .findFirst();

        Optional<Double> rating = extractRating(normalizedPrompt);
        return new HotelFilters(state, district, rating);
    }

    private Optional<Double> extractRating(String prompt) {
        Matcher matcher = Pattern
                .compile("(?:rating|star|stars|above|minimum|at least|se upar|se jyada|ya usse upar)\\D*([1-5](?:\\.\\d)?)|([1-5](?:\\.\\d)?)\\D*(?:rating|star|stars)")
                .matcher(prompt);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private Optional<UserEntity> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        String email = authentication.getName();
        if (email == null || "anonymousUser".equals(email)) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email);
    }

    private String fallbackAnswer(String prompt, AiContext context, String serviceMessage) {
        String normalizedPrompt = normalize(prompt);
        if (!isHotelRelated(normalizedPrompt)) {
            return "Main sirf hotel related help kar sakta hoon, jaise hotel suggestion, booking details, payment, receipt, rooms aur reviews.";
        }

        StringBuilder answer = new StringBuilder();
        if (normalizedPrompt.contains("last") || normalizedPrompt.contains("pich") || normalizedPrompt.contains("book")) {
            if (context.bookings().isEmpty()) {
                answer.append("Is user ke liye abhi koi booking database me nahi mili.");
            } else {
                Booking last = context.bookings().get(0);
                answer.append("Aapki last booking: ")
                        .append(last.getHotel().getName())
                        .append(", room ")
                        .append(last.getRoom().getType())
                        .append(", ")
                        .append(last.getCheckInDate())
                        .append(" se ")
                        .append(last.getCheckOutDate())
                        .append(", status ")
                        .append(last.getStatus())
                        .append(", amount Rs. ")
                        .append(last.getTotalPrice())
                        .append(".");
            }
        } else if (!context.hotels().isEmpty()) {
            answer.append("Database ke according matching hotel suggestions:\n");
            context.hotels().stream().limit(5).forEach(hotel ->
                    answer.append("- ")
                            .append(hotel.getName())
                            .append(" (")
                            .append(hotel.getDistrict())
                            .append(", ")
                            .append(hotel.getState())
                            .append(") - rating ")
                            .append(hotel.getRating())
                            .append("\n")
            );
        } else {
            answer.append("Is filter ke according database me koi matching hotel nahi mila.");
        }

        answer.append("\n\nNote: ").append(serviceMessage);
        return answer.toString();
    }

    private boolean isHotelRelated(String prompt) {
        return List.of("hotel", "booking", "book", "room", "payment", "receipt", "review", "rating", "state", "district", "stay", "guest")
                .stream()
                .anyMatch(prompt::contains);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private boolean containsIgnoreCase(String source, String expected) {
        return normalize(source).contains(normalize(expected));
    }

    private String extractText(ChatResponse response) {
        if (response == null || response.message() == null) {
            throw new IllegalStateException("Ollama returned an empty response");
        }

        String text = response.message().content();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalStateException("Ollama returned an empty message");
        }

        return text.trim();
    }

    private record ChatRequest(String model, List<Message> messages, boolean stream) {}

    private record Message(String role, String content) {}

    private record ChatResponse(ResponseMessage message) {}

    private record ResponseMessage(String role, String content) {}

    private record HotelFilters(Optional<String> state, Optional<String> district, Optional<Double> rating) {}

    private record AiContext(
            HotelFilters filters,
            List<Hotel> hotels,
            Optional<UserEntity> user,
            List<Booking> bookings
    ) {
        private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String summary() {
            StringBuilder builder = new StringBuilder();
            builder.append("Detected filters: state=")
                    .append(filters.state().orElse("not provided"))
                    .append(", district=")
                    .append(filters.district().orElse("not provided"))
                    .append(", minimumRating=")
                    .append(filters.rating().map(String::valueOf).orElse("not provided"))
                    .append("\n\n");

            builder.append("Matching hotels:\n");
            if (hotels.isEmpty()) {
                builder.append("No matching hotels found.\n");
            } else {
                hotels.forEach(hotel -> builder
                        .append("- id=")
                        .append(hotel.getId())
                        .append(", name=")
                        .append(hotel.getName())
                        .append(", state=")
                        .append(hotel.getState())
                        .append(", district=")
                        .append(hotel.getDistrict())
                        .append(", rating=")
                        .append(hotel.getRating())
                        .append(", address=")
                        .append(hotel.getAddress())
                        .append(", description=")
                        .append(hotel.getDescription())
                        .append("\n"));
            }

            builder.append("\nAuthenticated user:\n");
            if (user.isEmpty()) {
                builder.append("No logged-in user. Do not provide booking details.\n");
            } else {
                UserEntity currentUser = user.get();
                builder.append("id=")
                        .append(currentUser.getId())
                        .append(", name=")
                        .append(currentUser.getFirstName())
                        .append(" ")
                        .append(currentUser.getLastName())
                        .append(", email=")
                        .append(currentUser.getEmail())
                        .append("\n");
            }

            builder.append("\nUser bookings, latest first:\n");
            if (bookings.isEmpty()) {
                builder.append("No bookings found for authenticated user.\n");
            } else {
                builder.append(bookings.stream()
                        .map(booking -> "- bookingId=" + booking.getId()
                                + ", hotel=" + booking.getHotel().getName()
                                + ", state=" + booking.getHotel().getState()
                                + ", district=" + booking.getHotel().getDistrict()
                                + ", roomType=" + booking.getRoom().getType()
                                + ", checkIn=" + booking.getCheckInDate()
                                + ", checkOut=" + booking.getCheckOutDate()
                                + ", amount=" + booking.getTotalPrice()
                                + ", status=" + booking.getStatus()
                                + ", bookedAt=" + (booking.getBookedAt() == null ? "not available" : DATE_TIME_FORMAT.format(booking.getBookedAt())))
                        .collect(Collectors.joining("\n")));
            }
            return builder.toString();
        }
    }
}
