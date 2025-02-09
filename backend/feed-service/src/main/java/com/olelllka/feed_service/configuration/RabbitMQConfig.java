package com.olelllka.feed_service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue feedUpdatesQueue() {
        return new Queue("feed_updates_queue");
    }

    @Bean
    public TopicExchange postsExchange() {
        return new TopicExchange("posts_exchange");
    }

    @Bean
    public Binding feedBinding(Queue feedUpdatesQueue, TopicExchange postsExchange) {
        return BindingBuilder.bind(feedUpdatesQueue).to(postsExchange).with("post.new");
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }

}
