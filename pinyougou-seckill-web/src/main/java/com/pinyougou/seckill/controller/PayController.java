package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private WeixinPayService weixinPayService;
	
	@Reference
	private SeckillOrderService seckillOrderService;
	
	@RequestMapping("/createNative")
	public Map createNative() {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		
		TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
		
		if(seckillOrder!=null) {//订单存在
			long fen= (long)(seckillOrder.getMoney().doubleValue()*100);//金额（分）
			return weixinPayService.createNative(seckillOrder.getId()+"", fen+"");
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
		//获取当前用户 
		String userId=SecurityContextHolder.getContext().getAuthentication().getName();
		
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
				seckillOrderService.saveOrderFromRedisToDb(userId,Long.valueOf(out_trade_no), map.get("transaction_id"));
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
				
				//关闭微信订单
				Map payresult = weixinPayService.closePay(out_trade_no);
				if(!"SUCCESS".equals(payresult.get("result_code"))) { //调用正常关闭订单
					
					if("ORDERPAID".equals(payresult.get("err_code"))){//关闭订单前已支付
						result=new Result(true, "支付成功"); 
						seckillOrderService.saveOrderFromRedisToDb(userId,Long.valueOf(out_trade_no), map.get("transaction_id"));
					}
					
					
				}
				
				if(result.getSuccess()==false){
					System.out.println("超时，取消订单");
					//2.调用删除
					seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
				}
				
				break;
			}
			
		}
		
		return result;
	}
}
