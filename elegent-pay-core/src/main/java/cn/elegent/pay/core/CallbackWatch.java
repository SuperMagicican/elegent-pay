package cn.elegent.pay.core;

import cn.elegent.pay.CallBackService;
import cn.elegent.pay.ElegentPay;
import cn.elegent.pay.config.CallbackConfig;
import cn.elegent.pay.dto.QueryRefundResponse;
import cn.elegent.pay.dto.QueryResponse;
import cn.elegent.pay.dto.WatchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Component
@ConditionalOnProperty(prefix = "elegent.pay.callback",name = "watch",havingValue = "true")
@Slf4j
public class CallbackWatch {

    @Autowired
    private CallBackService callBackService;

    @Autowired
    private CallbackConfig callbackConfig;

    @Autowired
    private ElegentPay elegentPay;

    @PostConstruct
    public void queryWatch(){
        if(callbackConfig.getCycle()<=0){
            return;
        }
        log.info("开启支付结果定期巡检");
        Timer timer = new Timer();
        // 2、创建 TimerTask 任务线程
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                try{
                    //查询支付状态
                    log.info("支付状态定期巡检,{}",WatchList.payList);
                    for( WatchDTO watchDTO: WatchList.payList ){
                        //查询订单是否支付成功
                        QueryResponse queryResponse = elegentPay.queryTradingOrderNo(watchDTO.getOrderSn(),watchDTO.getPlatform());
                        if("SUCCESS".equals(queryResponse.getTrade_state())){
                            callBackService.successPay(queryResponse.getOrder_sn());
                            WatchList.payList.remove(watchDTO );
                        }
                    }
                    log.info("退款状态定期巡检,{}",WatchList.refundList);
                    //查询退款状态
                    for( WatchDTO watchDTO: WatchList.refundList ){
                        //查询退款中订单
                        QueryRefundResponse queryResponse = elegentPay.queryRefundTrading( watchDTO.getOrderSn(),watchDTO.getPlatform());
                        if("SUCCESS".equals(queryResponse.getStatus())){
                            callBackService.successRefund(queryResponse.getOrder_sn());
                            WatchList.refundList.remove(watchDTO);
                        }
                    }
                }catch (Exception ex){
                }
            }
        };
        // 4、启动定时任务
        timer.schedule(task, callbackConfig.getCycle()*1000, callbackConfig.getCycle()*1000);
    }

}
