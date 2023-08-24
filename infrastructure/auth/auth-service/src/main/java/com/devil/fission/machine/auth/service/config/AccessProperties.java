package com.devil.fission.machine.auth.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 访问配置.
 *
 * @author Devil
 * @date Created in 2023/5/4 14:51
 */
@ConfigurationProperties(prefix = "machine.auth")
public class AccessProperties {
    
    /**
     * 访问数组.
     */
    private List<Access> access;
    
    /**
     * get the access .
     */
    public List<Access> getAccess() {
        return access;
    }
    
    /**
     * the access to set.
     */
    public void setAccess(List<Access> access) {
        this.access = access;
    }
    
    /**
     * get the access Map .
     */
    public Map<String, Access> getAccessMap() {
        if (access != null) {
            return access.stream().filter(Objects::nonNull).collect(Collectors.toMap(a -> a.accessKey, a -> a));
        } else {
            return new HashMap<>(16);
        }
    }
    
    public static final class Access {
        
        /**
         * 访问key.
         */
        private String accessKey;
        
        /**
         * 访问secret.
         */
        private String accessSecret;
        
        /**
         * 访问source.
         */
        private String accessSource;
        
        /**
         * 可访问uri，不配置默认全部可访问.
         */
        private List<String> accessUriList;
        
        /**
         * get the accessKey .
         */
        public String getAccessKey() {
            return accessKey;
        }
        
        /**
         * the accessKey to set.
         */
        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }
        
        /**
         * get the accessSecret .
         */
        public String getAccessSecret() {
            return accessSecret;
        }
        
        /**
         * the accessSecret to set.
         */
        public void setAccessSecret(String accessSecret) {
            this.accessSecret = accessSecret;
        }
        
        /**
         * get the accessSource .
         */
        public String getAccessSource() {
            return accessSource;
        }
        
        /**
         * the accessSource to set.
         */
        public void setAccessSource(String accessSource) {
            this.accessSource = accessSource;
        }
        
        /**
         * get the accessUriList .
         */
        public List<String> getAccessUriList() {
            return accessUriList;
        }
        
        /**
         * the accessUriList to set.
         */
        public void setAccessUriList(List<String> accessUriList) {
            this.accessUriList = accessUriList;
        }
    }

}
