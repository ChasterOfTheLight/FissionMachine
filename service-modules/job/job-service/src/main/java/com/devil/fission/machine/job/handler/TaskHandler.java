package com.devil.fission.machine.job.handler;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Component;

/**
 * TaskHandler.
 *
 * @author Devil
 * @date Created in 2023/9/19 16:06
 */
@Component
public class TaskHandler {
    
    private final JobLauncher jobLauncher;
    
    private final Job job;
    
    public TaskHandler(JobLauncher jobLauncher, Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }
    
    @XxlJob("batchJobDemo")
    public void batchJobDemo() {
        JobParameters params = new JobParametersBuilder().addString("JobID", String.valueOf(System.currentTimeMillis())).toJobParameters();
        try {
            jobLauncher.run(job, params);
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | JobParametersInvalidException | JobRestartException e) {
            throw new RuntimeException(e);
        }
    }
}
