package com.example.practice.msscssm.service.impl;

import com.example.practice.msscssm.domain.Payment;
import com.example.practice.msscssm.domain.PaymentEvent;
import com.example.practice.msscssm.domain.PaymentState;
import com.example.practice.msscssm.repositories.PaymentRepository;
import com.example.practice.msscssm.service.PaymentService;
import com.example.practice.msscssm.service.PaymentStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    public static final String PAYMENT_ID_HEADER = "payment-id";

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;


    @Override
    public Payment createPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }


    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = buildStateMachine(paymentId);
        sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTHORIZE);
        return stateMachine;
    }


    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = buildStateMachine(paymentId);
        sendEvent(paymentId, stateMachine, PaymentEvent.AUTHORIZE);
        return stateMachine;
    }


    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = buildStateMachine(paymentId);
        sendEvent(paymentId, stateMachine, PaymentEvent.AUTH_DECLINED);
        return stateMachine;
    }


    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> stateMachine, PaymentEvent event) {
        Message<PaymentEvent> message = MessageBuilder.withPayload(event)
            .setHeader(PAYMENT_ID_HEADER, paymentId)
            .build();
        stateMachine.sendEvent(message);
    }


    private StateMachine<PaymentState, PaymentEvent> buildStateMachine(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(RuntimeException::new);
        StateMachine<PaymentState, PaymentEvent> stateMachine = stateMachineFactory.getStateMachine(Long.toString(paymentId));
        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
            .doWithAllRegions(stateMachineAccess -> {
                stateMachineAccess.addStateMachineInterceptor(paymentStateChangeInterceptor);
                stateMachineAccess.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
            });

        stateMachine.start();
        return stateMachine;
    }
}