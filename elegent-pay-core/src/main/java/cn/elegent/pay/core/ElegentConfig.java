package cn.elegent.pay.core;

import cn.elegent.pay.CallBackService;
import cn.elegent.pay.key.KeyManager;
import cn.elegent.pay.key.impl.DefaultKeyManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElegentConfig {

    @Bean
    @ConditionalOnMissingBean
    public CallBackService callBackService(){
        return new CallBackServiceImpl();
    }


    @Bean
    @ConditionalOnMissingBean
    public KeyManager keyManager(){
        return new DefaultKeyManager();
    }

}
