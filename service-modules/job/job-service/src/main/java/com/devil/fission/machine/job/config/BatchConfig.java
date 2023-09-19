package com.devil.fission.machine.job.config;

import com.devil.fission.machine.job.task.TaskOne;
import com.devil.fission.machine.job.task.TaskTwo;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BatchConfig.
 *
 * @author Devil
 * @date Created in 2023/9/19 14:38
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    private final JobBuilderFactory jobs;
    
    private final StepBuilderFactory steps;
    
    public BatchConfig(JobBuilderFactory jobs, StepBuilderFactory steps) {
        this.jobs = jobs;
        this.steps = steps;
    }
    
    @Bean
    public Step stepOne() {
        return steps.get("stepOne").tasklet(new TaskOne()).build();
    }
    
    @Bean
    public Step stepTwo() {
        return steps.get("stepTwo").tasklet(new TaskTwo()).build();
    }
    
    @Bean
    public Job demoJob() {
        return jobs.get("demoJob").incrementer(new RunIdIncrementer()).start(stepOne()).next(stepTwo()).build();
    }
    
}
