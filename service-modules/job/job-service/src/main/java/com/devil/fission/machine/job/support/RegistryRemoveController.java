package com.devil.fission.machine.job.support;

import com.xxl.job.core.thread.ExecutorRegistryThread;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * xxl-job elegance shutdown.
 *
 * @author devil
 * @date Created in 2023/9/19 10:11
 */
@RestController
@RequestMapping("/xxljob")
public class RegistryRemoveController {
    
    @DeleteMapping("/registryRemove")
    public String registryRemove() {
        ExecutorRegistryThread.getInstance().toStop();
        return "success";
    }
    
}
