package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.PaymentResponseDTO;
import com.university.project.hotelmanagement.entity.Payment;
import com.university.project.hotelmanagement.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {


    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDTO> pay(@RequestParam Long bookingId, @RequestParam Double amount, @RequestParam String method) {
        return ResponseEntity.ok(paymentService.processPayment(bookingId, amount, method));
    }
}