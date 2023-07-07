package com.devil.fission.machine.common.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * 精准数据计算工具类
 *
 * @author yuanhai
 * @version 1.0.0
 * @date 2019-02-22 15:16
 */
public class BigDecimalUtil {

    /**
     * 数据计算方法
     *
     * @param t        参数T
     * @param u        参数U
     * @param function 函数接口
     * @param <T>      extends Comparable<? super T>
     * @param <U>      extends Comparable<? super U>
     * @param <R>      extends BigDecimal
     * @return
     */
    public static <T extends Comparable<? super T>, U extends Comparable<? super U>, R extends BigDecimal> R compute(T t, U u, BiFunction<BigDecimal, BigDecimal, R> function) {
        Objects.requireNonNull(t, "参数T不能为空");
        Objects.requireNonNull(u, "参数U不能为空");
        return function.apply(new BigDecimal(String.valueOf(t)), new BigDecimal(String.valueOf(u)));
    }

    public static final int MONEY_POINT_2 = 2; // 货币保留两位小数

    public static String format2Or0(BigDecimal number) {
        if (new BigDecimal(number.intValue()).compareTo(number)==0){
            //整数
            return number.setScale(0, BigDecimal.ROUND_FLOOR).toPlainString();
        }else {
            //小数
            return number.setScale(2, BigDecimal.ROUND_FLOOR).toPlainString();
        }

    }

    /**
     * 提供精确的加法运算。
     *
     * @param v1
     *            被加数
     * @param v2
     *            加数
     * @return 两个参数的和
     */
    public static BigDecimal add(BigDecimal v1, BigDecimal v2) {
        if (v1 == null) {
            v1 = BigDecimal.valueOf(0);
        }
        if (v2 == null) {
            v2 = BigDecimal.valueOf(0);
        }
        return v1.add(v2).setScale(MONEY_POINT_2, BigDecimal.ROUND_HALF_UP);
    }



    /**
     * @Description: 对象四舍五入
     * @Author: GuXiYang
     * @Date: 2021/6/18 9:16
     * @Param o
     * @Return void
     */
    public static void reflect(Object o)  {
        try {
            if(null!=o) {
                //获取参数类
                Class cls = o.getClass();
                //将参数类转换为对应属性数量的Field类型数组（即该类有多少个属性字段 N 转换后的数组长度即为 N）
                Field[] fields = cls.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    f.setAccessible(true);
                    //f.getName()得到对应字段的属性名，f.get(o)得到对应字段属性值,f.getGenericType()得到对应字段的类型
                    //System.out.println("属性名：" + f.getName() + "；属性值：" + f.get(o) + ";字段类型：" + f.getGenericType());
                    if (f.getGenericType() == BigDecimal.class) {
                        DecimalFormat formater = new DecimalFormat("#0.##");
                        formater.setRoundingMode(RoundingMode.HALF_UP);
                        if (null != f.get(o)) {
                            BigDecimal num1 = new BigDecimal(formater.format(f.get(o)));
                            f.set(o, num1);
                            // System.out.println(f.get(o));
                        } else {
                            f.set(o, new BigDecimal("0"));
                            //System.out.println(f.get(o));
                        }
                    }
                    if(f.getGenericType()==String.class){
                        if(null==f.get(o)){
                            f.set(o,"");
                        }
                    }

                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 格式化BigDecimal, 小数位格式化为两位小数位
     * @return
     */
    public static String format2(BigDecimal number) {
        return number.setScale(2, BigDecimal.ROUND_FLOOR).toPlainString();

    }

}
