package com.olelllka.profile_service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitMQTestConfig {
    public static final String create_user_exchange = "create.user.exchange";
    public static final String update_user_exchange = "update.user.exchange";
    public static final String update_user_queue = "update_user_auth_queue";
    public static final String create_user_queue = "create_user_auth_queue";

    @Bean
    public Queue updateUserQueue() {
        return new Queue(update_user_queue, true);
    }

    @Bean
    public Queue createUserQueue() {
        return new Queue(create_user_queue, true);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(create_user_exchange);
    }

    @Bean
    public DirectExchange updateExchange() {
        return new DirectExchange(update_user_exchange);
    }

    @Bean
    public Binding createUserBinding(Queue createUserQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(createUserQueue).to(directExchange).with("create.user");
    }

    @Bean
    public Binding updateUserBinding(Queue updateUserQueue, DirectExchange updateExchange) {
        return BindingBuilder.bind(updateUserQueue).to(updateExchange).with("update.user");
    }
}
