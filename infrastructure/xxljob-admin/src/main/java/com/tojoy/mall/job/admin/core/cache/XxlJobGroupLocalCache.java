package com.tojoy.mall.job.admin.core.cache;

import com.tojoy.mall.job.admin.core.conf.XxlJobAdminConfig;
import com.tojoy.mall.job.admin.core.model.XxlJobGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yangkun
 * @createDate: 2022/9/13
 * @since: JDK 1.8
 * @Description:
 */
public class XxlJobGroupLocalCache {

    private static final Map<Integer, XxlJobGroup> groupCache = new ConcurrentHashMap<>();

    public static XxlJobGroup loadOne(int id) {
        XxlJobGroup xxlJobGroup = groupCache.get(id);
        if (xxlJobGroup == null) {
            xxlJobGroup = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().load(id);
            if (xxlJobGroup != null) {
                groupCache.put(id, xxlJobGroup);
            }
        }
        return xxlJobGroup;
    }

    public static void refreshCache(XxlJobGroup group) {
        groupCache.put(group.getId(), group);
    }

}
