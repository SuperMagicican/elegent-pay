package cn.elegent.pay.core;
import cn.elegent.pay.ElegentPay;
import cn.elegent.pay.config.CallbackConfig;
import cn.elegent.pay.dto.*;
import cn.elegent.pay.exceptions.TradeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ElegentPayImpl implements ElegentPay {

    @Autowired
    private CallbackConfig callbackConfig;

    @Override
    public PayResponse requestPay(PayRequest payRequest,String tradeType,String platform) throws TradeException {
        //获取交易策略
        PayResponse payResponse = getPlatFormService(platform).requestPay(payRequest, tradeType);

        //加入监听列表
        if(callbackConfig.isWatch()){
            WatchDTO watchDTO=new WatchDTO();
            watchDTO.setOrderSn(payRequest.getOrderSn());
            watchDTO.setPlatform(platform);
            WatchList.payList.add( watchDTO );
        }
        return payResponse;
    }


    @Override
    public Boolean closePay(String orderSn, String platform) throws TradeException {
        //调用对应第三方的创建订单接口
        Boolean aBoolean = getPlatFormService(platform).closePay(orderSn);
        //加入监听列表
        if(callbackConfig.isWatch()){
            WatchDTO watchDTO=new WatchDTO();
            watchDTO.setOrderSn(orderSn);
            watchDTO.setPlatform(platform);
            WatchList.payList.remove( watchDTO );
        }

        return aBoolean;
    }


    @Override
    public Boolean refund(RefundRequest refundRequest,String platform) throws TradeException {
        Boolean refund = getPlatFormService(platform).refund(refundRequest);
        //加入监听列表
        if(callbackConfig.isWatch()  && refund){
            WatchDTO watchDTO=new WatchDTO();
            watchDTO.setOrderSn(refundRequest.getRequestNo());
            watchDTO.setPlatform(platform);
            WatchList.refundList.add( watchDTO );
        }
        return refund;
    }


    @Override
    public QueryResponse queryTradingOrderNo(String  orderSn ,String Platform) throws TradeException {
        //调用具体第三方的退款接口
        return getPlatFormService(Platform).queryTradingOrderNo(orderSn);
    }


    @Override
    public QueryRefundResponse queryRefundTrading(String orderSn , String platform) throws TradeException {
        //获取请求参数
        return getPlatFormService(platform).queryRefundTrading(orderSn);
    }

    @Override
    public String getOpenid(String code, String platform) {
        return getPlatFormService(platform).getOpenid(code);
    }

/**
 * ====================================提供的模板类需要使用的方法=====================================
 */


    /**
     * 跟进具体内容获取实现类的方法
     * @param platForm
     * @return
     */
    private ElegentTrade getPlatFormService(String platForm) {
        return ElegentLoader.getElegentTrade(platForm);
    }

}
