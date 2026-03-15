package dev.chriswalden.jobforge.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String QUEUE    = "jobforge.jobs";
    public static final String EXCHANGE = "jobforge.exchange";
    public static final String ROUTING_KEY = "jobforge.jobs";

    @Bean
    public Queue jobQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public DirectExchange jobExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding jobBinding(Queue jobQueue, DirectExchange jobExchange) {
        return BindingBuilder.bind(jobQueue).to(jobExchange).with(ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}