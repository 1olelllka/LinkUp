package com.olelllka.profile_service.configuration;

import org.springframework.amqp.core.*;
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
    public static final String profile_fanout_exchange = "profile_fanout_exchange";
    public static final String delete_queue_elastic = "delete_profile_queue_elastic";
    public static final String delete_queue_post = "delete_profile_queue_post";
    public static final String delete_queue_story = "delete_profile_queue_story";
    public static final String delete_queue_feed = "delete_profile_queue_feed";
    public static final String delete_queue_notification = "delete_profile_queue_notification";
    public static final String delete_queue_auth = "delete_profile_queue_auth";
    public static final String notification_queue = "notification_queue";
    public static final String notification_exchange = "notification_exchange";

    @Bean
    public Queue createUpdateQueue() {
        return new Queue(create_update_queue, true);
    }

    @Bean
    public Queue deletePostQueue() {
        return new Queue(delete_queue_post, true);
    }

    @Bean
    public Queue deleteStoryQueue() {
        return new Queue(delete_queue_story, true);
    }

    @Bean
    public Queue deleteFeedQueue() {
        return new Queue(delete_queue_feed, true);
    }

    @Bean
    public Queue deleteNotificationQueue() {
        return new Queue(delete_queue_notification, true);
    }

    @Bean
    public Queue deleteQueueElastic() {
        return new Queue(delete_queue_elastic, true);
    }

    @Bean
    public Queue deleteQueueAuth() {
        return new Queue(delete_queue_auth, true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(notification_queue, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(profile_exchange);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(profile_fanout_exchange);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notification_exchange);
    }

    @Bean
    public Binding createProfileBinding(Queue createUpdateQueue, TopicExchange exchange) {
        return BindingBuilder.bind(createUpdateQueue).to(exchange).with("create_and_update_profile");
    }

    @Bean
    public Binding deleteProfileElasticBinding(Queue deleteQueueElastic, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(deleteQueueElastic).to(fanoutExchange);
    }

    @Bean
    public Binding deleteProfilePostBinding(Queue deletePostQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(deletePostQueue).to(fanoutExchange);
    }

    @Bean
    public Binding deleteProfileStoryBinding(Queue deleteStoryQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(deleteStoryQueue).to(fanoutExchange);
    }

    @Bean
    public Binding deleteProfileFeedBinding(Queue deleteFeedQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(deleteFeedQueue).to(fanoutExchange);
    }

    @Bean
    public Binding deleteProfileNotificationBinding(Queue deleteNotificationQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(deleteNotificationQueue).to(fanoutExchange);
    }

    @Bean
    public Binding deleteProfileAuthBinding(Queue deleteQueueAuth, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(deleteQueueAuth).to(fanoutExchange);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with("notifications");
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
