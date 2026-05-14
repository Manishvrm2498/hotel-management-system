package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.dto.PaymentResponseDTO;
import com.university.project.hotelmanagement.entity.Booking;
import com.university.project.hotelmanagement.entity.Payment;
import com.university.project.hotelmanagement.enums.BookingStatus;
import com.university.project.hotelmanagement.exception.BadRequestException;
import com.university.project.hotelmanagement.exception.ResourceNotFoundException;
import com.university.project.hotelmanagement.exception.UnauthorizedAccessException;
import com.university.project.hotelmanagement.repository.BookingRepository;
import com.university.project.hotelmanagement.repository.PaymentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PaymentService {


    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final EmailService mailService;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository, EmailService mailService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.mailService = mailService;
    }

    @Transactional
    public PaymentResponseDTO processPayment(Long bookingId, double userProvidedAmount, String method) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found!"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        if (booking.getUser() == null || !currentUserEmail.equals(booking.getUser().getEmail())) {
            throw new UnauthorizedAccessException("You are not allowed to pay for this booking.");
        }
        if (BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            throw new BadRequestException("Payment already completed for this booking.");
        }
        if (BookingStatus.CANCELLED.equals(booking.getStatus())
                || BookingStatus.EXPIRED.equals(booking.getStatus())
                || BookingStatus.FAILED.equals(booking.getStatus())) {
            throw new BadRequestException("This booking is no longer eligible for payment.");
        }
        if (!BookingStatus.PENDING.equals(booking.getStatus())) {
            throw new BadRequestException("Only pending bookings can be paid.");
        }
        if (booking.getExpiryTime() != null && booking.getExpiryTime().isBefore(LocalDateTime.now())) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new BadRequestException("Payment session expired. Please create a new booking.");
        }

        long numberOfDays = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        if (numberOfDays <= 0) {
            numberOfDays = 1;
        }

        double actualPricePerDay = booking.getRoom().getPrice();
        double expectedTotalAmount = numberOfDays * actualPricePerDay;

        if (Math.abs(userProvidedAmount - expectedTotalAmount) > 0.01) {
            throw new BadRequestException("Payment amount mismatch! Expected: " + expectedTotalAmount + " but received: " + userProvidedAmount);
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(expectedTotalAmount);
        payment.setPaymentMethod(method);
        payment.setTransactionId("PAY-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentTime(LocalDateTime.now());

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPrice(expectedTotalAmount);
        booking.setExpiryTime(null);

        bookingRepository.save(booking);
        Payment savedPayment = paymentRepository.save(payment);

        try {
            mailService.sendBookingConfirmation(
                    booking.getUser().getEmail(),
                    booking.getUser().getFirstName(),
                    booking.getHotel().getName(),
                    booking.getRoom().getType().name(),
                    booking.getTotalPrice()
            );
        } catch (Exception e) {
            System.err.println("Error sending confirmation email: " + e.getMessage());
        }

        return mapToPaymentResponseDTO(savedPayment);
    }

    private PaymentResponseDTO mapToPaymentResponseDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setTransactionId(payment.getTransactionId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentTime(payment.getPaymentTime());
        dto.setHotelName(payment.getBooking().getHotel().getName());
        dto.setDistrict(payment.getBooking().getHotel().getDistrict());
        dto.setCheckInDate(payment.getBooking().getCheckInDate());
        dto.setCheckOutDate(payment.getBooking().getCheckOutDate());
        return dto;
    }
}
