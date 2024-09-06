package com.devil.fission.machine.jdbc.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devil.fission.machine.common.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

/**
 * sql打印拦截器.
 *
 * @author Devil
 * @date Created in 2024/9/4 15:57
 */
@Slf4j
@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class SqlPrintInterceptor implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 统计sql执行耗时
        long startTime = System.currentTimeMillis();
        Object proceed = invocation.proceed();
        long endTime = System.currentTimeMillis();
        String printSql = null;
        try {
            printSql = generateSql(invocation);
        } catch (Exception e) {
            log.error("Execute SQL Error: {}, SQL: {}", e.getMessage(), printSql, e);
        } finally {
            long costTime = endTime - startTime;
            long effectLines = 0;
            if (proceed instanceof List) {
                List<?> effectLinesList = (List<?>) proceed;
                int size = effectLinesList.size();
                if (size == 1) {
                    Object o = effectLinesList.get(0);
                    if (o instanceof Integer) {
                        effectLines = (Integer) o;
                    } else if (o instanceof Long) {
                        effectLines = (Long) o;
                    }
                } else if (size > 1) {
                    effectLines = size;
                }
            } else if (proceed instanceof Long) {
                effectLines = (Long) proceed;
            } else if (proceed instanceof Integer) {
                effectLines = (Integer) proceed;
            }
            log.info("========== Sql Print ==========\n    Execute SQL：{}\n    Total: {}\n    Execute Time：{}ms", printSql, effectLines, costTime);
        }
        return proceed;
    }
    
    private static String generateSql(Invocation invocation) {
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        Configuration configuration = statement.getConfiguration();
        // 如果是查询需要拿具体mybatis-plus的真正查询语句
        boolean isUpdate = invocation.getArgs().length == 2;
        BoundSql boundSql;
        if (isUpdate) {
            // 拿到执行sql
            boundSql = statement.getBoundSql(parameter);
        } else {
            if (invocation.getArgs().length == 4) {
                boundSql = statement.getBoundSql(parameter);
            } else {
                boundSql = (BoundSql) invocation.getArgs()[5];
            }
        }
        Object parameterObject = boundSql.getParameterObject();
        // 获取参数映射
        List<ParameterMapping> params = boundSql.getParameterMappings();
        // 获取到执行的SQL
        String sql = boundSql.getSql();
        // SQL中多个空格使用一个空格代替
        sql = sql.replaceAll("[\\s]+", " ");
        
        if (!ObjectUtils.isEmpty(params) && !ObjectUtils.isEmpty(parameterObject)) {
            // TypeHandlerRegistry 是 MyBatis 用来管理 TypeHandler 的注册器 TypeHandler 用于在 Java 类型和 JDBC 类型之间进行转换
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            // 如果参数对象的类型有对应的 TypeHandler，则使用 TypeHandler 进行处理
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
            } else {
                // 否则，逐个处理参数映射
                for (ParameterMapping param : params) {
                    // 获取参数的属性名
                    String propertyName = param.getProperty();
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    // 检查对象中是否存在该属性的 getter 方法，如果存在就取出来进行替换
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                        // 检查 BoundSql 对象中是否存在附加参数
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else {
                        // SQL匹配不上，带上“缺失”方便找问题
                        sql = sql.replaceFirst("\\?", "缺失");
                    }
                }
            }
        }
        
        // 添加 LIMIT 子句
        if (invocation.getArgs().length >= 3 && invocation.getArgs()[2] instanceof RowBounds) {
            RowBounds rowBounds = (RowBounds) invocation.getArgs()[2];
            int limit = rowBounds.getLimit();
            int offset = rowBounds.getOffset();

            if (limit != RowBounds.NO_ROW_LIMIT || offset != RowBounds.NO_ROW_OFFSET) {
                sql += " LIMIT ";
                if (offset != RowBounds.NO_ROW_OFFSET) {
                    sql += offset + ", ";
                }
                sql += limit;
            } else {
                // 解析 limit
                if (parameterObject instanceof MapperMethod.ParamMap) {
                    MapperMethod.ParamMap<?> paramMap = (MapperMethod.ParamMap<?>) parameterObject;
                    try {
                        Object pageParam = paramMap.get("page");
                        if (pageParam != null) {
                            if (pageParam instanceof Page) {
                                Page<?> plusPage = (Page<?>) pageParam;
                                if (sql.contains("LIMIT")) {
                                    sql = statement.getBoundSql(parameter).getSql();
                                    sql = sql.replaceAll("[\\s]+", " ");
                                    sql += " LIMIT " + (plusPage.getCurrent() - 1) * plusPage.getSize() + ", " + plusPage.getSize();
                                }
                            }
                        }
                    } catch (BindingException e) {
                        // 不处理
                    }
                }
            }
        }
        
        return sql;
    }
    
    private static String getParameterValue(Object object) {
        String value = "";
        if (object instanceof String) {
            value = "'" + object + "'";
        } else if (object instanceof Date) {
            value = "'" + DateUtils.fastDateTimeFormat((Date) object) + "'";
        } else if (!ObjectUtils.isEmpty(object)) {
            value = object.toString();
        }
        return value;
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}