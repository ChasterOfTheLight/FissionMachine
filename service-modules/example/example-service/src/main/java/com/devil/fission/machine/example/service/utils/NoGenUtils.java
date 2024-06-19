package com.devil.fission.machine.example.service.utils;

import cn.hutool.core.date.DateUtil;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 订单号生成工具.
 *
 * @author Devil
 * @date Created in 2024/4/26 16:44
 */
@Component
public class NoGenUtils {
    
    /**
     * 分布式锁工具.
     */
    private final RedissonClient redissonClient;
    
    public NoGenUtils(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    
    
    /**
     * 生成订单号.
     *
     * @param noPrefix 单号前缀
     * @param noSuffix 单号后缀
     * @return 订单号字符串
     */
    public String generateNumber(String noPrefix, String noSuffix) {
        StringBuilder orderNoBuilder = new StringBuilder();
        orderNoBuilder.append(noPrefix);
        RLock orderNumberLock = redissonClient.getLock("NoLock");
        try {
            // 加分布式锁处理（先这样处理，后期看业务量改为批量发号）
            orderNumberLock.lock(1L, TimeUnit.SECONDS);
            RAtomicLong noAtomicLong = redissonClient.getAtomicLong("NoAtomicLong");
            if (noAtomicLong.get() < 168L) {
                noAtomicLong.set(168);
            }
            Date currentDate = new Date();
            // 获取当前日期时间字符串，确保始终为12位   每次都需要是最新时间
            String dateTimeStr = DateUtil.format(currentDate, "yyMMddHHmmss");
            // 获取并增加序列号
            long seq = noAtomicLong.getAndIncrement();
            // 当序列号马上到1000时，重置序列号并增加秒数
            if (seq == 999) {
                // 重置序列号
                noAtomicLong.set(168);
                // 强制睡1秒
                Thread.sleep(1000);
            }
            // 拼装最终单号
            orderNoBuilder.append(dateTimeStr).append(String.format("%03d", seq)).append(noSuffix);
        } catch (Exception e) {
            throw new RuntimeException("生成订单号失败", e);
        } finally {
            if (orderNumberLock.isHeldByCurrentThread()) {
                orderNumberLock.unlock();
            }
        }
        return orderNoBuilder.toString();
    }
    
    /**
     * 生成主订单号 DD+年月日时分秒(12位）+递增序列号（168起）+类型（XSD/YSK)（备注“年月日时分秒都是两位起，168从此基础上加数，当并发递增序列超过3位时，秒数+1，类型XSD表示销售单，YSK表示预收款，预收款修改为销售单后单号不能进行调整）.
     */
    public String genOrderNo(int orderType) {
        if (1 == orderType) {
            return generateNumber("DD", "XSD");
        } else if (2 == orderType) {
            return generateNumber("DD", "YSK");
        }
        return null;
    }
    
    /**
     * 生成子订单号（支付单号）ZFD+年月日时分秒(12位）+递增序列号（068起）（备注“时分秒都是两位起，068从此基础上加数，当并发递增序列超过3位时，秒数+1）.
     */
    public String genSubOrderNo() {
        return generateNumber("ZFD", "");
    }
    
    
    /**
     * 生成退款单号 TKD+年月日时分秒(12位）+递增序列号（068起）（备注“时分秒都是两位起，068从此基础上加数，当并发递增序列超过3位时，秒数+1）.
     */
    public String genRefundOrderNo() {
        return generateNumber("TKD", "");
    }
    
}
