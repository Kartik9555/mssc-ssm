package com.example.practice.msscssm.config;

import com.example.practice.msscssm.domain.PaymentEvent;
import com.example.practice.msscssm.domain.PaymentState;
import java.util.EnumSet;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import static com.example.practice.msscssm.domain.PaymentEvent.AUTHORIZE;
import static com.example.practice.msscssm.domain.PaymentEvent.AUTH_APPROVED;
import static com.example.practice.msscssm.domain.PaymentEvent.AUTH_DECLINED;
import static com.example.practice.msscssm.domain.PaymentEvent.PRE_AUTHORIZE;
import static com.example.practice.msscssm.domain.PaymentEvent.PRE_AUTH_APPROVED;
import static com.example.practice.msscssm.domain.PaymentEvent.PRE_AUTH_DECLINED;
import static com.example.practice.msscssm.domain.PaymentState.AUTH;
import static com.example.practice.msscssm.domain.PaymentState.AUTH_ERROR;
import static com.example.practice.msscssm.domain.PaymentState.NEW;
import static com.example.practice.msscssm.domain.PaymentState.PRE_AUTH;
import static com.example.practice.msscssm.domain.PaymentState.PRE_AUTH_ERROR;
import static com.example.practice.msscssm.service.impl.PaymentServiceImpl.PAYMENT_ID_HEADER;

@EnableStateMachineFactory
@Slf4j
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
            .initial(PaymentState.NEW)
            .states(EnumSet.allOf(PaymentState.class))
            .end(PaymentState.AUTH)
            .end(PaymentState.PRE_AUTH_ERROR)
            .end(PaymentState.AUTH_ERROR);
    }


    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
            .withExternal().source(NEW).target(NEW).event(PRE_AUTHORIZE).action(preAuthAction()).guard(paymentIdGuard())
            .and()
            .withExternal().source(NEW).target(PRE_AUTH).event(PRE_AUTH_APPROVED)
            .and()
            .withExternal().source(NEW).target(PRE_AUTH_ERROR).event(PRE_AUTH_DECLINED)
            .and()
            .withExternal().source(PRE_AUTH).target(PRE_AUTH).event(AUTHORIZE).action(authorizeAction()).guard(paymentIdGuard())
            .and()
            .withExternal().source(PRE_AUTH).target(AUTH).event(AUTH_APPROVED)
            .and()
            .withExternal().source(PRE_AUTH).target(AUTH_ERROR).event(AUTH_DECLINED);
    }


    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> stateMachineListenerAdapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info("State changed from {} to {}", from, to);
            }
        };
        config.withConfiguration()
            .listener(stateMachineListenerAdapter);
    }

    public Guard<PaymentState, PaymentEvent> paymentIdGuard(){
        return context -> context.getMessageHeader(PAYMENT_ID_HEADER) != null;
    }


    public Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> {
            System.out.println("Pre auth was called");
            if (new Random().nextInt(10) < 8) {
                System.out.println("Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PRE_AUTH_APPROVED)
                    .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                    .build());
            }
            else {
                System.out.println("Declined. No credit !!!!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PRE_AUTH_DECLINED)
                    .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                    .build());
            }
        };
    }


    public Action<PaymentState, PaymentEvent> authorizeAction() {
        return context -> {
            System.out.println("Authorize was called");
            if (new Random().nextInt(10) < 8) {
                System.out.println("Authorize Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(AUTH_APPROVED)
                    .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                    .build());
            }
            else {
                System.out.println("Authorize Declined. No credit !!!!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(AUTH_DECLINED)
                    .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                    .build());
            }
        };
    }
}
