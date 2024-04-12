package com.example.practice.msscssm.config.actions;

import com.example.practice.msscssm.domain.PaymentEvent;
import com.example.practice.msscssm.domain.PaymentState;
import java.util.Random;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import static com.example.practice.msscssm.domain.PaymentEvent.AUTH_APPROVED;
import static com.example.practice.msscssm.domain.PaymentEvent.AUTH_DECLINED;
import static com.example.practice.msscssm.service.impl.PaymentServiceImpl.PAYMENT_ID_HEADER;

@Component
public class AuthAction implements Action<PaymentState, PaymentEvent> {
    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        System.out.println("Authorize Action was called");
        if (new Random().nextInt(10) < 8) {
            System.out.println("Authorization is approved");
            context.getStateMachine().sendEvent(MessageBuilder.withPayload(AUTH_APPROVED)
                .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                .build());
        }
        else {
            System.out.println("Authorization declined. No credit !!!!");
            context.getStateMachine().sendEvent(MessageBuilder.withPayload(AUTH_DECLINED)
                .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                .build());
        }
    }
}
