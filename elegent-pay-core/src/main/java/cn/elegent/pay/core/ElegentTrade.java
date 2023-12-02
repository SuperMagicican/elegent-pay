package cn.elegent.pay.core;


import cn.elegent.pay.dto.*;
import cn.elegent.pay.exceptions.TradeException;

public interface ElegentTrade {



    PayResponse requestPay(PayRequest payRequest,String tradeType) throws TradeException;


    Boolean closePay(String orderSn) throws TradeException;


    Boolean refund(RefundRequest refundRequest) throws TradeException;

    QueryResponse queryTradingOrderNo(String orderSn) throws TradeException;


    QueryRefundResponse queryRefundTrading(String orderSn) throws TradeException;


    String getOpenid(String code);

}
