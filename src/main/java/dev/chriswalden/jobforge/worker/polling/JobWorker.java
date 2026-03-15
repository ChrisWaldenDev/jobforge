package dev.chriswalden.jobforge.worker.polling;

import dev.chriswalden.jobforge.api.repository.JobRepository;
import dev.chriswalden.jobforge.config.RabbitMqConfig;
import dev.chriswalden.jobforge.core.messaging.JobMessage;
import dev.chriswalden.jobforge.worker.execution.JobExecutor;
import dev.chriswalden.jobforge.core.domain.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class JobWorker {

    private static final Logger log = LoggerFactory.getLogger(JobWorker.class);

    private final JobRepository jobRepository;
    private final JobExecutor jobExecutor;

    public JobWorker(JobRepository jobRepository, JobExecutor jobExecutor) {
        this.jobRepository = jobRepository;
        this.jobExecutor = jobExecutor;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void onMessage(JobMessage message) {
        log.info("[job={}] Received from RabbitMQ type={}", message.jobId(), message.jobType());

        Job job = jobRepository.findById(message.jobId())
                .orElseThrow(() -> new NoSuchElementException("Job not found: " + message.jobId()));

        jobExecutor.execute(job);
    }
}