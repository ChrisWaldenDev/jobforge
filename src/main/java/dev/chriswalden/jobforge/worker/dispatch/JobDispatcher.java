package dev.chriswalden.jobforge.worker.dispatch;

import dev.chriswalden.jobforge.core.domain.Job;
import dev.chriswalden.jobforge.core.domain.JobType;
import dev.chriswalden.jobforge.worker.handler.JobHandler;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class JobDispatcher {

    private final Map<JobType, JobHandler> handlersByType;

    public JobDispatcher(List<JobHandler> handlers) {
        this.handlersByType = new EnumMap<>(JobType.class);

        for (JobHandler handler : handlers) {
            JobType type = handler.supports();
            JobHandler existing = handlersByType.put(type, handler);

            if (existing != null) {
                throw new IllegalStateException("Duplicate JobHandler for type " + type +
                        ": " + existing.getClass().getName() +
                        " and " + handler.getClass().getName());
            }
        }
    }

    public JobHandler handleFor(Job job) {
        JobType type = job.getType();
        JobHandler handler = handlersByType.get(type);

        if (handler == null) {
            throw new IllegalStateException("No JobHandler registered for type " + type);
        }

        return handler;
    }

    public String dispatch(Job job) throws Exception {
        return handleFor(job).handle(job);
    }

}
