package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private WeixinPayService weixinPayService;
	
	@Reference
	private OrderService orderService;
	
	/**
	 * 生成二维码
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map createNative() {
		//获取当前用户
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		//到 redis 查询支付日志	
		TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
		if(payLog!=null) { //有支付日志
			return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
		}else {
			return new HashMap();
		}
		
		
	}
	
	
	/**
	 * 查询订单状态
	 * @param out_trade_no
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		Result result = null;
		int x=0; //设置初始化查询次数
		
		while(true) {
		//循环调用查询订单支付状态	
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
			
			if (map==null) {
				result = new Result(false,"支付出现异常");
				break;
			}
			
			if(map.get("trade_state").equals("SUCCESS")) {//支付成功
				result = new Result(true,"支付成功");
				//修改订单状态
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
				break;
			}
			
			try {
				Thread.sleep(3000);  //每三秒钟调用一次
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			x++;
			if(x>=100) {
				result=new Result(false, "二维码超时");
				break;
			}
			
		}
		
		
		return result;
	}
}
