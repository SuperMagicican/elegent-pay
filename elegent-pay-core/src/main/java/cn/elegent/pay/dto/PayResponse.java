package cn.elegent.pay.dto;

import lombok.Data;

import java.util.Map;

/**
 * 结果统一封装类
 */
@Data
public class PayResponse {

    private boolean success;  //是否成功

    private String message;//信息

    private String order_sn;//订单编号

    private Map<String,String>  expand;//扩展属性

    private String code_url;//二维码连接（native返回）

    private String prepay_id;//预支付Id（小程序返回）

    private String h5_url;//支付跳转链接

    private Map<String,String>  jsapiData;//小程序返回

}
