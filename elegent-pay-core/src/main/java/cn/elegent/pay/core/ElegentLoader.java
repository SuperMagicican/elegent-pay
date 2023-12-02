package cn.elegent.pay.core;

import cn.elegent.pay.annotation.TradePlatform;
import cn.elegent.pay.exceptions.TradeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class ElegentLoader implements ApplicationContextAware {

    private static Map<String, ElegentTrade> elegentTradeMap = new HashMap<>();

    private static Map<String, ElegentValid> elegentValidMap = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        //加载所有的交易实现类
        Collection<ElegentTrade> elegentTrades = applicationContext.getBeansOfType(ElegentTrade.class).values();
        elegentTrades.stream().forEach(e->{
            //通过反射拿到类上的平台注解
            TradePlatform annotation = e.getClass().getAnnotation(TradePlatform.class);
            if (annotation != null) {
                elegentTradeMap.put(annotation.value(), e);
            }
        });

        //加载所有的验证实现类
        Collection<ElegentValid> elegentValids = applicationContext.getBeansOfType(ElegentValid.class).values();
        elegentValids.stream().forEach(e->{
            //通过反射拿到类上的平台注解
            TradePlatform annotation = e.getClass().getAnnotation(TradePlatform.class);
            if (annotation != null) {
                elegentValidMap.put(annotation.value(), e);
            }
        });

    }


    public static ElegentTrade getElegentTrade(String platform){
        ElegentTrade elegentPayTemplate = elegentTradeMap.get(platform);
        if(elegentPayTemplate!=null){
            return elegentPayTemplate;
        }else{
            throw new TradeException("未找到适配的交易类型,交易平台id:"+platform);
        }
    }



    public static ElegentValid getElegentValid(String platform){
        ElegentValid elegentValidTemplate = elegentValidMap.get(platform);
        if(elegentValidTemplate!=null){
            return elegentValidTemplate;
        }else{
            throw new TradeException("未找到适配的交易类型,交易平台id:"+platform);
        }
    }

}
