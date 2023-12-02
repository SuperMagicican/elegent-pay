package cn.elegent.pay;
import cn.elegent.pay.dto.*;
import cn.elegent.pay.exceptions.TradeException;
public interface ElegentPay {


    PayResponse requestPay(PayRequest payRequest,String tradeType,String platform) throws TradeException;


    Boolean closePay(String orderSn, String platform) throws TradeException;


    Boolean refund(RefundRequest refundRequest,String platform) throws TradeException;


    QueryResponse queryTradingOrderNo(String orderSn , String platform) throws TradeException;



    QueryRefundResponse queryRefundTrading(String orderSn , String platform) throws TradeException;


    /**
     * 获得openID
     * @param code
     * @return
     */
    String getOpenid(String code, String platform);

}
