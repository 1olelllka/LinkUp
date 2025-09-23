package com.olelllka.stories_service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitMQConfig {
    public static final String delete_queue = "delete_profile_queue_story";
    public static final String profile_exchange = "profile_fanout_exchange";
    public final static String CREATE_STORY_QUEUE = "create_story_queue";

    @Bean
    public Queue deleteQueue() {
        return new Queue(delete_queue, true);
    }

    @Bean
    public FanoutExchange exchange() {
        return new FanoutExchange(profile_exchange);
    }

    @Bean
    public Binding deleteProfileBinding(Queue deleteQueue, FanoutExchange exchange) {
        return BindingBuilder.bind(deleteQueue).to(exchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
