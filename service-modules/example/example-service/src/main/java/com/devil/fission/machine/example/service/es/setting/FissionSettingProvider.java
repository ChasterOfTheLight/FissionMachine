package com.devil.fission.machine.example.service.es.setting;

import org.dromara.easyes.annotation.rely.DefaultSettingsProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * FissionSettingProvider.
 *
 * @author Devil
 * @date Created in 2024/5/10 17:55
 */
public class FissionSettingProvider extends DefaultSettingsProvider {
    
    @Override
    public Map<String, Object> getSettings() {
        // 这里可以自定义你的settings实现,将自定义的settings置入map并返回即可
        Map<String, Object> mySettings = new HashMap<>(8);
        // 例如指定查询操作的慢日志阈值为30秒,当查询操作的执行时间超过此阈值时，Elasticsearch会记录相应的慢日志并发出警告
        mySettings.put("index.search.slowlog.threshold.query.warn", "30s");
        return mySettings;
    }
}