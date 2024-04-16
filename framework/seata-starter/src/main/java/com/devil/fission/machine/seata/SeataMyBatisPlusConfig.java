package com.devil.fission.machine.seata;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * seata的mybatis plus配置 需要提前于MybatisPlusAutoConfiguration和DataSourceAutoConfiguration，提前暴露DataSource（代理）.
 *
 * @author devil
 * @date Created in 2024/03/21 14:07
 */
@Configuration
@AutoConfigureBefore(value = {DataSourceAutoConfiguration.class, MybatisPlusAutoConfiguration.class})
public class SeataMyBatisPlusConfig {
    
    /**
     * 默认使用hikari数据库连接池.
     *
     * @return 数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        return new HikariDataSource();
    }
    
    /**
     * 使用代理完成seata事务处理.
     *
     * @param dataSource 数据源
     * @return 代理数据源
     */
    @Primary
    @Bean(value = "dataSource")
    public DataSourceProxy dataSourceProxy(DataSource dataSource) {
        return new DataSourceProxy(dataSource);
    }
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
