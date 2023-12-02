package cn.elegent.pay.alipay;

import cn.elegent.pay.key.KeyManager;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/**
 * 不需要程序员关心的 支付宝权限对接类
 */
@Component
@ConfigurationProperties("elegent.pay.alipay")
@Data
public class AlipayConfig {
    /**
     * 应用识别码
     */
    private String appId;

    /**
     * 密钥加密方式 RSA2
     */
    @Value("${elegent.pay.alipay.charset:RSA2}")
    private String signType;

    @Autowired
    private KeyManager keyManager;

    public String getPrivateKey(){
        return keyManager.getKey("alipay_private.key");
    }

    public String getPublicKey(){
        return keyManager.getKey("alipay_public.key");
    }


}