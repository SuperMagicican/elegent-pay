package cn.elegent.pay.core;

import cn.elegent.pay.dto.ValidResponse;
import cn.elegent.pay.exceptions.TradeException;
import org.springframework.http.HttpEntity;

import javax.servlet.http.HttpServletRequest;

public interface ElegentValid {


    ValidResponse validPay(HttpEntity<String> httpEntity, HttpServletRequest request) throws TradeException;


    ValidResponse validRefund(HttpEntity<String> httpEntity, HttpServletRequest request) throws TradeException;


    String successResult();


    String failResult();

}
