package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;

/**
 * 微信支付
 * @author IAI
 *
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService{

	@Value("${appid}")
	private String appid;  //微信公众账号或开放平台 APP 的唯一标识
	
	@Value("${partner}")
	private String partner;  //财付通平台的商户账号
	
	@Value("${partnerkey}")
	private String partnerkey; //财付通平台的商户密钥
	
	
	/**
	 * 生成二维码
	 */
	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		
		//1.创建参数map集合
		Map<String, String> param= new HashMap<>();  
		
		param.put("appid", appid);  //公众账号ID
		param.put("mch_id", partner);    //商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
		param.put("body", "品优购商城");  //商品描述
		param.put("out_trade_no", out_trade_no);	//订单号
		param.put("total_fee", total_fee);  //标价金额
		param.put("spbill_create_ip", "127.0.0.1");  //终端IP
		param.put("notify_url", "http://test.itcast.cn");   //通知地址未使用到，随便写）
		param.put("trade_type", "NATIVE ");  //交易类型
		
		//2.将map集合转换成要发送的XML格式
		
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println(xmlParam);
			
			//发送数据
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");	
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
		
		//3.获取返回的数据
			String result = client.getContent();
			System.out.println(result);
			
			Map<String, String> resultMap =  WXPayUtil.xmlToMap(result); //将返回结果解析为map
			
			Map<String, String> map=new HashMap<>();  
			
			map.put("code_url", resultMap.get("code_url"));  //支付地址
			map.put("total_fee", total_fee);  //总金额
			map.put("out_trade_no", out_trade_no);  //订单号
			return map;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new HashMap<>();
		}
		
		
		
		
	}

	/**
	 * 查询支付状态
	 */
	@Override
	public Map queryPayStatus(String out_trade_no) {
		
		//1.创建map参数集合
		Map<String, String> param = new HashMap<>();
		
		param.put("appid", appid);    //公众账号ID
		param.put("mch_id", partner);	 //商户号
		param.put("out_trade_no", out_trade_no);  //商户订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		
		
		//2.将map集合转换成要发送的XML格式并发送
		
		try {
			String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
			
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			client.setHttps(true);
			client.setXmlParam(signedXml);
			client.post();
			
		//3.获取返回的数据
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	/**
	 * 关闭支付
	 */
	@Override
	public Map closePay(String out_trade_no) {
		//1.创建map参数集合
		Map<String, String> param = new HashMap<>();
		
		param.put("appid", appid);    //公众账号ID
		param.put("mch_id", partner);	 //商户号
		param.put("out_trade_no", out_trade_no);  //商户订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
		
		
		//2.将map集合转换成要发送的XML格式并发送
		
		try {
			String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
			
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
			client.setHttps(true);
			client.setXmlParam(signedXml);
			client.post();
			
		//3.获取返回的数据
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
