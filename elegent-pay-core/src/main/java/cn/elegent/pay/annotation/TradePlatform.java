package cn.elegent.pay.annotation;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TradePlatform {


    String value();
}
