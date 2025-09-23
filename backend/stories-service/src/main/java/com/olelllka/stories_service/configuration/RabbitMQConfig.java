package com.olelllka.stories_service.configuration;

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

    public final static String CREATE_STORY_QUEUE = "create_story_queue";
    public final static String CREATE_QUEUE_EXCHANGE = "create.queue.exchange";

    @Bean
    public Queue create_story_queue() {
        return new Queue(CREATE_STORY_QUEUE, true);
    }

    @Bean
    public DirectExchange create_queue_exchange() {
        return new DirectExchange(CREATE_QUEUE_EXCHANGE);
    }

    @Bean
    public Binding create_queue_binding(Queue create_story_queue, DirectExchange create_queue_exchange) {
        return BindingBuilder.bind(create_story_queue).to(create_queue_exchange).with("story.create");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
