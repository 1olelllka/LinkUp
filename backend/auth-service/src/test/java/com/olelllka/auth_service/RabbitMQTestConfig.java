package com.olelllka.auth_service;

import org.springframework.amqp.core.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitMQTestConfig {

    public static final String delete_queue_auth = "delete_profile_queue_auth";
    public static final String profile_fanout_exchange = "profile_fanout_exchange";
    
    @Bean
    public Queue deleteQueueAuth() {
        return new Queue(delete_queue_auth, true);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(profile_fanout_exchange);
    }

    @Bean
    public Binding deleteProfileAuthBinding(Queue deleteQueueAuth, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(deleteQueueAuth).to(fanoutExchange);
    }

}
