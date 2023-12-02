package cn.elegent.pay.wxpay;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 不需要程序员关心的 微信权限对接类
 */
@Component
@ConfigurationProperties("elegent.pay.wxpay")
@Data
public class WxpayConfig {

    private String mchId; //商户号
    private String appId; //APPID
    private String appSecret;//app密钥
    private String mchSerialNo; //商户证书序列号
    private String apiV3Key; //V3密钥

}