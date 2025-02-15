package com.olelllka.feed_service;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitMQConfiguration {
    public static final String feedQueue = "feed_updates_queue";
    public static final String feedExchange = "post_exchange";
    public static final String delete_queue = "delete_profile_queue_feed";
    public static final String fanoutExchange = "profile_fanout_exchange";

    @Bean
    public Queue feedQueue() {
        return new Queue(feedQueue, true);
    }

    @Bean
    public Queue deleteQueue() {
        return new Queue(delete_queue, true);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(fanoutExchange);
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
    public Binding deleteProfileBinding(Queue deleteQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(deleteQueue).to(fanoutExchange);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
