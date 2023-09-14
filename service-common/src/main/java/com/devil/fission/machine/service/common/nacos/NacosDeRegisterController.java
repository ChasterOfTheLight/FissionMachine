package com.devil.fission.machine.service.common.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * nacos优雅停机.
 *
 * @author Devil
 * @date Created in 2022/3/29 15:44
 */
@ApiIgnore
@RestController
public class NacosDeRegisterController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosDeRegisterController.class);
    
    @Autowired
    private NacosServiceManager nacosServiceManager;
    
    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;
    
    /**
     * 请求nacos取消登记.
     */
    @GetMapping(value = "deregisterInstance")
    public String deregisterInstance() {
        String serviceName = nacosDiscoveryProperties.getService();
        String groupName = nacosDiscoveryProperties.getGroup();
        String clusterName = nacosDiscoveryProperties.getClusterName();
        String ip = nacosDiscoveryProperties.getIp();
        int port = nacosDiscoveryProperties.getPort();
        LOGGER.info("deregister from nacos, serviceName:{}, groupName:{}, clusterName:{}, ip:{}, port:{}", serviceName, groupName, clusterName, ip,
                port);
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            namingService.deregisterInstance(serviceName, groupName, ip, port, clusterName);
        } catch (NacosException e) {
            LOGGER.error("deregister from nacos error", e);
            return "error";
        }
        return "success";
    }
    
}
