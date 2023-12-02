package cn.elegent.pay.constant;

import lombok.Data;

/**
 * 交易类型
 */
@Data
public class TradeType {

    /**
     * native（扫码）
     */
    public final static String NATIVE = "native";

    /**
     * jsapi(小程序)
     */
    public final static String JSAPI = "jsapi";


    /**
     * app
     */
    public final static String APP = "app";


    /**
     * h5
     */
    public final static String H5 = "h5";

}
