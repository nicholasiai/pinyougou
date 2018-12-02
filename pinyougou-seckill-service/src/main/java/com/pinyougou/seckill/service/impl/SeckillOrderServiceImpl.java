package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
	@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	
	/**
	 * 提交订单
	 */
	@Override
	public void submitOrder(Long seckillId, String userId) {
		//从redis中获取数据
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		
		if(seckillGoods==null) {
			throw new RuntimeException("商品不存在");
		}
		
		if(seckillGoods.getStockCount()<=0) {
			throw new RuntimeException("商品已经被抢光了");
		}
		
		seckillGoods.setStockCount(seckillGoods.getStockCount()-1); //商品库存减1
		//将商品存入缓存
		redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
		System.out.println("提交订单");
		if(seckillGoods.getStockCount()<=0) { //库存为0，缓存中移除商品,更新到数据库
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
			
		}
		
		//保存(redis)订单
		long orderId = idWorker.nextId();
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(orderId);  //订单id
		seckillOrder.setCreateTime(new Date()); //创建时间
		seckillOrder.setMoney(seckillGoods.getCostPrice()); //秒杀价格
		seckillOrder.setSeckillId(seckillId); //商品id
		seckillOrder.setSellerId(seckillGoods.getSellerId()); //商家id
		seckillOrder.setUserId(userId); //用户id
		seckillOrder.setStatus("0"); //状态
		//存入到秒杀订单缓存
		redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
	}
	
	/**
	 * 根据用户id查询秒杀订单
	 */
	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);
	}
	
	/**
	 * 支付成功保存订单
	 */
	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		System.out.println("保存订单到数据库"+userId);
		
		//从redis中查询订单数据
		TbSeckillOrder seckillOrder = (TbSeckillOrder)redisTemplate.boundHashOps("seckillOrder").get(userId);
		
		if(seckillOrder==null) {
			throw new RuntimeException("订单不存在");
		}
		
		if(seckillOrder.getId().longValue()!=orderId) {
			throw new RuntimeException("订单不相符");
		}
		
		seckillOrder.setTransactionId(transactionId); //交易流水号
		seckillOrder.setPayTime(new Date()); //支付时间
		seckillOrder.setStatus("1"); //交易状态
		seckillOrderMapper.insert(seckillOrder); //保存到数据库
		redisTemplate.boundHashOps("seckillOrder").delete(userId); //删除缓存中的数据
	}

	/**
	 * 删除redis中订单
	 */
	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		//获取订单
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		
		if(seckillOrder!=null&&seckillOrder.getId().longValue()==orderId) {
			redisTemplate.boundHashOps("seckillOrder").delete(userId); //删除用户缓存订单
			
			//恢复库存
			//获取秒杀商品
			 TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
			
			 if(seckillGoods!=null) {//库存不为空，商品数量加1
				 seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
				 redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
			 }else {//库存为空，重新上架商品
				 	seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
					seckillGoods.setStockCount(1);//数量为1
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
			}
			 
		}
		
		
	}
	
	
	
}
