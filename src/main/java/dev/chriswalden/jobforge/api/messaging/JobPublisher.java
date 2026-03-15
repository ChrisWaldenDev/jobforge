package dev.chriswalden.jobforge.api.messaging;

import dev.chriswalden.jobforge.config.RabbitMqConfig;
import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.messaging.JobMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobPublisher {

    private static final Logger log = LoggerFactory.getLogger(JobPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public JobPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Job job) {
        JobMessage message = new JobMessage(job.getId(), job.getType());
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY, message);
        log.info("[job={}] Published to RabbitMQ type={}", job.getId(), job.getType());
    }
}