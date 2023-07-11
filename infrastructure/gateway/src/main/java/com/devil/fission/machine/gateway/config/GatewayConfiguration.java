package com.devil.fission.machine.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关配置类uri白名单，ip黑名单.
 *
 * @author devil
 * @date Created in 2022/12/26 14:22
 */
@Component
@ConfigurationProperties(prefix = "gateway-config")
public class GatewayConfiguration {
    
    /**
     * uri的白名单  在名单的不再认证，直接放行.
     */
    private List<String> uriWhitelist;
    
    /**
     * ip的黑名单   在名单的直接返回拒绝.
     */
    private List<String> ipBlackList;
    
    /**
     * the uriWhitelist.
     */
    public List<String> getUriWhitelist() {
        return uriWhitelist;
    }
    
    /**
     * uriWhitelist : the uriWhitelist to set.
     */
    public void setUriWhitelist(List<String> uriWhitelist) {
        this.uriWhitelist = uriWhitelist;
    }
    
    /**
     * the ipBlackList.
     */
    public List<String> getIpBlackList() {
        return ipBlackList;
    }
    
    /**
     * ipBlackList : the ipBlackList to set.
     */
    public void setIpBlackList(List<String> ipBlackList) {
        this.ipBlackList = ipBlackList;
    }
}
