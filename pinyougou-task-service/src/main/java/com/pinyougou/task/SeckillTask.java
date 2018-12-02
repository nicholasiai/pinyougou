package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

/**
 * 秒杀定时任务
 * @author IAI
 *
 */
@Component
public class SeckillTask {
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	/**
	 * 刷新秒杀商品
	 */
	@Scheduled(cron="0 * * * * ? ") //每分钟零秒开始刷新
	public void refreshSeckillGoods() {
		System.out.println("秒杀任务调度刷新"+new Date());
		
		//查询出正在秒杀商品的集合
		List list = new ArrayList<>(redisTemplate.boundHashOps("seckillGoods").keys());
		
		//查询正在秒杀的商品列表
		
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		
		criteria.andStatusEqualTo("1"); //审核通过
		criteria.andStockCountGreaterThan(0); //库存大于0
		criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
		criteria.andEndTimeGreaterThan(new Date()); //结束时间大于当前时间
		criteria.andIdNotIn(list); //排除缓存中已有的商品
		
		List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example );
		
		//存入缓存
		for(TbSeckillGoods seckillGoods :seckillGoodsList) {
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
		}
		
		System.out.println("将"+seckillGoodsList.size()+"条商品装入缓存");
		
	}
	
	/**
	 * 定时删除过期商品
	 */
	@Scheduled(cron="* * * * * ?")
	public void removeSeckillGoods() {
		
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		
		for(TbSeckillGoods seckill:seckillGoodsList) {
			if(seckill.getEndTime().getTime()<new Date().getTime()) {
				
				//秒杀商品已过期
				seckillGoodsMapper.updateByPrimaryKey(seckill); //更新到数据库
				redisTemplate.boundHashOps("seckillGoods").delete(seckill.getId());//移除缓存数
				System.out.println("移除秒杀商品"+seckill.getId());
			}
			
		}
		System.out.println("移除秒杀商品任务在执行");
	}
}
