package com.olelllka.auth_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

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
    public DirectExchange createExchange() {
        return new DirectExchange(create_user_exchange);
    }

    @Bean
    public DirectExchange updateExchange() {
        return new DirectExchange(update_user_exchange);
    }

    @Bean
    public Binding createUserBinding(Queue createUserQueue, DirectExchange createExchange) {
        return BindingBuilder.bind(createUserQueue).to(createExchange).with("create.user");
    }

    @Bean
    public Binding updateUserBinding(Queue updateUserQueue, DirectExchange updateExchange) {
        return BindingBuilder.bind(updateUserQueue).to(updateExchange).with("update.user");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
