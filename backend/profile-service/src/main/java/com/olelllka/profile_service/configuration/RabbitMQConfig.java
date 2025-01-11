package com.olelllka.profile_service.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String create_update_queue = "create_update_profile_queue";
    public static final String profile_exchange = "profile_exchange";
    public static final String delete_queue = "delete_profile_queue";
    public static final String notification_queue = "notification_queue";
    public static final String notification_exchange = "notification_exchange";

    @Bean
    public Queue createUpdateQueue() {
        return new Queue(create_update_queue, true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(notification_queue, true);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notification_exchange);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with("notifications");
    }

    @Bean
    public Queue deleteQueue() {
        return new Queue(delete_queue, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(profile_exchange);
    }

    @Bean
    public Binding createProfileBinding(Queue createUpdateQueue, TopicExchange exchange) {
        return BindingBuilder.bind(createUpdateQueue).to(exchange).with("create_and_update_profile");
    }

    @Bean
    public Binding deleteProfileBinding(Queue deleteQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deleteQueue).to(exchange).with("delete_profile");
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
