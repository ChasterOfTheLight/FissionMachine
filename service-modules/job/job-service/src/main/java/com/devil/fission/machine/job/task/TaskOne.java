package com.devil.fission.machine.job.task;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * TaskOne.
 *
 * @author Devil
 * @date Created in 2023/9/19 14:37
 */
public class TaskOne implements Tasklet {
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("TaskOne start..");
        
        // ... your code
        
        System.out.println("TaskOne done..");
        return RepeatStatus.FINISHED;
    }
}
