package com.olelllka.auth_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String create_user_exchange = "create.user.exchange";

    @Bean
    public Queue createUserQueue() {
        String create_user_queue = "create_user_queue";
        return new Queue(create_user_queue, true);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(create_user_exchange);
    }

    @Bean
    public Binding createUserBinding(Queue createUserQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(createUserQueue).to(directExchange).with("create.user");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
