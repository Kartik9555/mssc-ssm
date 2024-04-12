package com.example.practice.msscssm.service;

import com.example.practice.msscssm.domain.Payment;
import com.example.practice.msscssm.domain.PaymentEvent;
import com.example.practice.msscssm.domain.PaymentState;
import com.example.practice.msscssm.repositories.PaymentRepository;
import com.example.practice.msscssm.service.impl.PaymentServiceImpl;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {
    private final PaymentRepository paymentRepository;


    @Override
    public void preStateChange(
        State<PaymentState, PaymentEvent> state,
        Message<PaymentEvent> message,
        Transition<PaymentState, PaymentEvent> transition,
        StateMachine<PaymentState, PaymentEvent> stateMachine,
        StateMachine<PaymentState, PaymentEvent> rootStateMachine) {

        Optional.ofNullable(message)
            .flatMap(msg -> Optional.ofNullable((Long) msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
            .ifPresent(paymentId -> {
                Payment payment = paymentRepository.findById(paymentId).orElseThrow(RuntimeException::new);
                payment.setState(state.getId());
                paymentRepository.save(payment);
            });
    }
}
