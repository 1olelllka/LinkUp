package com.olelllka.feed_service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitMQConfiguration {
    public static final String feedQueue = "feed_updates_queue";
    public static final String feedExchange = "post_exchange";

    @Bean
    public Queue feedQueue() {
        return new Queue(feedQueue, true);
    }

    @Bean
    public TopicExchange feedExchange() {
        return new TopicExchange(feedExchange);
    }

    @Bean
    public Binding notificationBinding(Queue feedQueue, TopicExchange feedExchange) {
        return BindingBuilder.bind(feedQueue).to(feedExchange).with("post.new");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
