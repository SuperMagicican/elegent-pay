package cn.elegent.pay.wxpay;

import java.util.HashMap;
import java.util.Map;

public class WxpayConstant {

    public static final Map SUCCESS = new HashMap<String,Object>(){
        {
            put("code", "SUCCESS");
        }
    };

    public static final Map FAIL  = new HashMap<String,Object>(){
        {
            put("code", "FAIL");
            put("message","微信回调错误结果");
        }
    };


    public final static String domain ="https://api.mch.weixin.qq.com/v3";

    /**
     * 创建订单
     */
    public final static String createOrder = domain +"/pay/transactions/";

    /**
     * 关闭订单
     */
    public final static String closeOrder = domain+"/pay/transactions/out-trade-no/{out_trade_no}/close";

    /**
     * 查询订单编号
     */
    public static String queryOrderNo = domain+"/pay/transactions/out-trade-no/";

    /**
     * 查询退款订单
     */
    public static String queryRufundOrderNo =domain+ "/refund/domestic/refunds/";

    /**
     * 退款接口
     */
    public static String refund = domain+"/refund/domestic/refunds";
}
