package cn.elegent.pay.dto;

import lombok.Data;

/**
 * 验证签名
 */
@Data
public class ValidResponse {

    private boolean isValid;// 是否通过验签

    private String orderSn;// 订单号

}
