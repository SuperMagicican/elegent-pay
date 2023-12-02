package cn.elegent.pay.dto;


import lombok.Data;


@Data
public class PayRequest {

    private String body;//商品描述

    private String orderSn; //订单号

    private int totalFee; //订单金额

    private String openid;//openId
}
