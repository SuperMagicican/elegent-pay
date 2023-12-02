package cn.elegent.pay.alipay;

import java.util.HashMap;
import java.util.Map;

public class AlipayConstant {

    public static final String SUCCESS = "SUCCESS";

    public static final String  FAIL  = "FAIL";

    /**
     * 交易状态(用于转换)
     */
    public static final Map<String,String> TRADE_STATE = new HashMap<String,String>(){
        {
            put("TRADE_SUCCESS", "SUCCESS");
            put("WAIT_BUYER_PAY", "NOTPAY");
            put("TRADE_CLOSED", "CLOSED");
            put("TRADE_FINISHED", "FINISHED");
        }
    };

    public static final String domain="https://openapi.alipay.com/gateway.do";

}
