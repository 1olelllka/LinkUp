package com.olelllka.notification_service;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitMQTestConfig {

    public static final String notification_queue = "notification_queue";
    public static final String notification_exchange = "notification_exchange";
    public static final String delete_profile_queue = "delete_profile_queue_notification";
    public static final String fanout_exchange = "profile_fanout_exchange";

    @Bean
    public Queue notificationQueue() {
        return new Queue(notification_queue, true);
    }

    @Bean
    public Queue profileDeleteQueue() {
        return new Queue(delete_profile_queue, true);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notification_exchange);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(fanout_exchange);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with("notifications");
    }

    @Bean
    public Binding deleteProfileBinding(Queue profileDeleteQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(profileDeleteQueue).to(fanoutExchange);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
