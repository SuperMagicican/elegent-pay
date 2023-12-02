package cn.elegent.pay.config;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 不需要程序员关心的 微信权限对接类
 */
@Component
@ConfigurationProperties("elegent.pay.callback")
@Data
public class CallbackConfig {

    private String domain; //回调域名

    @Value("${elegent.pay.callback.watch:false}")
    private boolean watch; //是否开启监听

    @Value("${elegent.pay.callback.cycle:10}")
    private int cycle;//检查周期


}