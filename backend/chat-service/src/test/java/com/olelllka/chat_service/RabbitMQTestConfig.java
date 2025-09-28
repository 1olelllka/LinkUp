package com.olelllka.chat_service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;


@TestConfiguration
public class RabbitMQTestConfig {

    public static final String delete_queue_chat = "delete_profile_queue_chat";
    public static final String profile_delete_exchange = "profile_fanout_exchange";

    @Bean
    public Queue deleteChatQueue() {
        return new Queue(delete_queue_chat, true);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(profile_delete_exchange);
    }

    @Bean
    public Binding deleteProfileChatBinding(Queue deleteChatQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(deleteChatQueue).to(fanoutExchange);
    }
}
