package com.example.practice.msscssm.service.impl;

import com.example.practice.msscssm.domain.Payment;
import com.example.practice.msscssm.domain.PaymentEvent;
import com.example.practice.msscssm.domain.PaymentState;
import com.example.practice.msscssm.repositories.PaymentRepository;
import com.example.practice.msscssm.service.PaymentService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }


    @Test
    void preAuth() {
        Payment savedPayment = paymentService.createPayment(payment);
        paymentService.preAuth(savedPayment.getId());

        Payment payment1 = paymentRepository.findById(savedPayment.getId()).get();
        System.out.println(payment1);
    }

    @RepeatedTest(10)
    void auth() {
        Payment savedPayment = paymentService.createPayment(payment);
        StateMachine<PaymentState, PaymentEvent> preAuthSm = paymentService.preAuth(savedPayment.getId());

        if(preAuthSm.getState().getId() == PaymentState.PRE_AUTH){
            System.out.println("Payment is preauthorized....");
            StateMachine<PaymentState, PaymentEvent> authSm = paymentService.authorizePayment(savedPayment.getId());
            System.out.println("Result of auth is: " + authSm.getState().getId());
        } else {
            System.out.println("Payment failed pre-auth....");
        }
    }
}