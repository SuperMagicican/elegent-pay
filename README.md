![](doc/images/logob.png)

# ElegentPay【支付框架】

## 介绍

​	ElegentPay是封装了支付宝和微信支付的支付框架，用户使用该框架，可以用最小的学习成本，在几分钟内快速集成并在项目中使用。

1. 为支付宝和微信提供了统一的调用入口。
2. 支持native、小程序、H5、APP等多种支付方式，并提供统一入口。
3. 提供了统一的dto类作为前端的调用参数，用户使用简便。
4. 封装了回调入口和验签逻辑，简化了用户编写支付回调中繁琐的验签逻辑。
5. 提供了扩展机制，用户可以自定义其它的支付方式。
6. 对支付回调和退款回调提供了幂等性校验。
7. 提供了回调补偿功能。

## 使用说明

### 准备工作

1.在项目中引入依赖：（按需引入）

```xml
<!--微信支付-->
<dependency>
    <groupId>cn.elegent.pay</groupId>
    <artifactId>elegent-pay-wxpay</artifactId>
    <version>1.0.0</version>
</dependency>
<!--支付宝支付-->
<dependency>
    <groupId>cn.elegent.pay</groupId>
    <artifactId>elegent-pay-alipay</artifactId>
    <version>1.0.0</version>
</dependency>
```
2.在项目配置文件添加配置：（按需配置）

```yaml
elegent:
  pay:
    wxpay:
      mchId: 1561414331
      appId: wx6592a2db3f85ed25
      appSecret: d9a9ff00a633cd7353a8925119063b01
      mchSerialNo: 25FBDE3EFD31B03A4377EB9A4A47C517969E6620
      apiV3Key: CZBK51236435wxpay435434323FFDuv3
    alipay:
      appId: 2021003141676135
    callback:
      domain: https://2d3ac179.r5.cpolar.top
      watch: true
      cycle: 10
```

wxpay（微信支付）配置说明：

  （1）mchId： 商户号

  （2）appId:  APP编号

  （3）appSecret： app密钥

  （4）mchSerialNo : 商户证书序列号

  （5）apiV3Key :  v3版本密钥

alipay（支付宝）配置说明：

  （1）appId：APP编号

callback（回调通知）配置说明：

  （1）domain：工程域名根目录。如果在本地调试，建议使用内网穿透工具生成此地址。

  （2）watch：是否开启回调监察。如果开启，将根据用户给出的本地未支付订单记录进行支付状态查询，如果已支付则自动进行回调处理。缺省值为false。

  （3）cycle: 回调监察周期，单位为秒。缺省值为10。

3.添加微信私钥和支付宝公钥、私钥

在resources目录下创建wxpay_private.key，用户存储微信私钥

在resources目录下创建alipay_private.key，用户存储支付宝私钥

在resources目录下创建alipay_public.key，用户存储支付宝公钥

### 代码编写

#### 调用方法

 类中引入 ElegentPay

```java
@Autowired
private ElegentPay elegentPay;
```

 通过调用elegentPay的一系列方法实现下单、查询、退款、退款查询等方法

（1）下单

```java
PayRequest payRequest = new PayRequest();
payRequest.setTotalFee(1);//金额（分）
payRequest.setOrderSn(System.currentTimeMillis()+""); //订单号
payRequest.setBody("橙汁");//商品名称
PayResponse payResponse = elegentPay.requestPay(payRequest, TradeType.NATIVE,  Platform.WX);
```

PayRequest是下单请求的对象，我们创建一个PayRequest对象后，设置金额、订单号和商品名称，如果是小程序支付，需要再设置openId。设置好属性值后，调用elegentPay的requestPay方法，参数说明：

参数1：支付请求对象，封装了订单号、商品名称、金额、openid等信息。如果是小程序支付，必须要传递openid参数。

参数2：交易类型：  目前支持四种：

​	本地支付（native）、 小程序（ jsapi） 、app（app）、h5（h5）

参数3：平台：目前支持微信支付（wxpay）和支付宝（alipay），通过Platform的常量指定。

返回值PayResponse 属性如下：

| 属性        | 类型      | 说明                                       |
| --------- | ------- | ---------------------------------------- |
| success   | boolean | 是否成功                                     |
| message   | String  | 错误信息，success为false时返回此信息                 |
| code_url  | String  | 二维码支付链接。当交易类型为native时返回此信息。前端收到此信息可生成支付二维码。 |
| prepay_id | String  | 预支付id。当交易类型为jsapi（小程序）时返回此信息。前端收到此信息可以唤醒支付模块。 |
| h5_url    | String  | 支付跳转页面                                   |
| order_sn  | String  | 订单号。                                     |
| expand    | Map     | 支付平台返回的全部信息，如果是小程序支付，小程序需要使用这个数据唤醒支付窗口。  |



 （2）关闭订单

```java
Boolean aBoolean = elegentPay.closePay(orderNo, Platform.WX);
return aBoolean;
```

当我们不需要让用户支付此订单需要中止交易的时候，可以调用closePay方法。

参数1：订单号

参数2：平台：目前支持微信支付（wxpay）和支付宝（alipay），通过Platform的常量指定。

返回值：布尔类型。true表示关闭成功，false表示关闭失败。

（3）退款

```java
RefundRequest refundRequest=new RefundRequest();
refundRequest.setTotalFee(1);//总金额
refundRequest.setRefundAmount(1);//退款金额
refundRequest.setOrderSn("1112222211");//订单号
Boolean refund = elegentPay.refund(refundRequest, Platform.WX);
```

参数1：退款请求对象

参数2：平台：目前支持微信支付（wxpay）和支付宝（alipay），通过Platform的常量指定。

返回值：布尔类型。true表示退款成功，false表示退款失败。

（4）查询订单

```java
QueryResponse queryResponse = elegentPay.queryTradingOrderNo(orderNo, Platform.WX);
```

参数1：订单号

参数2：平台：目前支持微信支付（wxpay）和支付宝（alipay），通过Platform的常量指定。

返回值QueryResponse属性如下：

| 属性             | 类型     | 说明        |
| -------------- | ------ | --------- |
| transaction_id | String | 交易单号      |
| order_sn       | String | 订单号       |
| trade_state    | String | 交易状态      |
| total          | int    | 金额（分）     |
| openid         | String | 用户id      |
| expand         | Map    | 平台全部返回的数据 |

（5）查询退款订单

```java
QueryRefundResponse response = elegentPay.queryRefundTrading(orderNo, Platform.WX);
```

参数1：订单号

参数2：平台：目前支持微信支付（wxpay）和支付宝（alipay），通过Platform的常量指定。

返回值QueryResponse属性如下：

| 属性                    | 类型     | 说明        |
| --------------------- | ------ | --------- |
| refund_id             | String | 退款单号      |
| out_refund_no         | String | 商户退款订单号   |
| channel               | String | 退款渠道      |
| user_received_account | String | 退款账号      |
| success_time          | String | 退款成功时间    |
| status                | String | 退款状态      |
| expand                | Map    | 平台全部返回的数据 |



#### 处理回调

处理支付回调，不需要用户编写Controller，本框架已经内置了回调的验签逻辑，用户只需要创建一个实现CallBackService接口的类即可。用户需要实现接口定义的4个方法，示例代码：

```java
@Service
@Slf4j
public class CallBackServiceImpl implements CallBackService {


    @Override
    public void successPay(String orderSn) {
        log.info("支付成功回调！"+orderSn);
    }

    @Override
    public void failPay(String orderSn) {
        log.info("支付失败回调！"+orderSn);
    }

    @Override
    public void successRefund(String orderSn) {
        log.info("退款成功回调！"+orderSn);
    }

    @Override
    public void failRefund(String orderSn) {
        log.info("退款失败回调！"+orderSn);
    }

}
```

方法说明：

| 方法名           | 方法含义     | 补充说明               |
| ------------- | -------- | ------------------ |
| successPay    | 支付成功回调方法 | 通常逻辑为修改订单状态为支付成功   |
| failPay       | 支付失败回调方法 | 通常逻辑为修改订单状态为支付失败   |
| successRefund | 退款成功回调方法 | 通常逻辑为修改订单退款状态为退款成功 |
| failRefund    | 退款失败回调方法 | 通常逻辑为修改订单退款状态为退款失败 |

### 用户扩展

#### 扩展支付平台

本框架内置了微信支付和支付宝两个常见平台的逻辑封装，用户如果需要其它的支付平台，可以基于本框架自行扩展。具体可以如下步骤完成：

（1）新建模块，elegent-pay-core引入依赖 以及平台提供的SDK

```xml
<dependency>
    <groupId>cn.elegent.pay</groupId>
    <artifactId>elegent-pay-core</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- 平台提供的SDK  (略) -->
```

（2）参照wxpay和alipay ，设计配置结构，并根据配置结构编写配置类

```java
@Component
@ConfigurationProperties("elegent.pay.xxxxx")
@Data
public class XXXXXConfig {

    //todo: 配置属性
}
```

（3）编写类实现ElegentTrade接口，并通过@TradePlatform指定平台名称，示例

```java
@Service
@TradePlatform(Platform.XXXXXX)
@Slf4j
public class XXXXXElegentTrade implements ElegentTrade {

    @Autowired
    private XXXXXConfig xxxxxConfig;

    @Autowired
    private CallBackConfig config;

	//todo: 实现ElegentTrade定义的方法
  
}
```

（4）编写类实现ElegentValid接口，并通过@TradePlatform指定平台名称，示例

```java
@Service
@TradePlatform(Platform.XXXX)
@Slf4j
public class XXXXXElegentValid implements ElegentValid {

}
```



#### 自定义私钥管理器

系统默认采用文件来管理密钥（私钥和公钥），如果用户需要自定义密钥的来源，可以自己编写私钥管理器的实现类。编写类，实现KeyManager接口：

```java
/**
 * 用户自定义密钥管理器
 */
@Component
public class XXXXXKeyManager implements KeyManager {

    @Override
    public String getKey(String name) {
        //todo: 用户编写的提取密钥的逻辑
    }

}
```

XXXXXXX为自定义密钥管理器的名称。

## 参与贡献

1.  从 `master` 分支 `checkout` 一个新分支（**注**：*请务必保证 master 代码是最新的*）
2.  新分支命名格式：`docs/username_description`，例如：`docs/tom_新增分布式锁配置项`
3.  在新分支上编辑文档、代码，并提交代码
4.  最后 `PR` 合并到 `develop` 分支，等待作者合并即可


