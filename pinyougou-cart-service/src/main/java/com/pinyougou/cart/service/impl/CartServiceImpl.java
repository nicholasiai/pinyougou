package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService{

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	/**
	 * 添加商品到购物车
	 */
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		
		//1.根据商品 SKU ID 查询 SKU 商品信息
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if(item==null) {
			throw new RuntimeException("商品不存在");
		}
		if(!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态为无效");
		}
		
		//2.获取商家 ID 
		String sellerId = item.getSellerId();
		//3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
		Cart cart = searchCartBySellerId(cartList,sellerId);
		
		if(cart==null) {
			//4.如果购物车列表中不存在该商家的购物车
			//4.1 新建购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			TbOrderItem orderItem = createOrderItem(item,num);
			
			List orderItemList = new ArrayList<>(); //新建订单列表
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			//4.2 将新建的购物车对象添加到购物车列表
			cartList.add(cart);
		}else {
			//5.如果购物车列表中存在该商家的购物车 
			// 查询购物车明细列表中是否存在该商品
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
			if(orderItem==null) {
				//5.1. 如果没有，新增购物车明细
				orderItem = createOrderItem(item,num);
				cart.getOrderItemList().add(orderItem);
			}else {
				//5.2. 如果有，在原购物车明细上添加数量，更改金额
				orderItem.setNum(orderItem.getNum()+num);  //更改商品数量
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
				
				if(orderItem.getNum()<=0) {
					//订单数量小于0，移除该订单
					cart.getOrderItemList().remove(orderItem);
				}
				
				if(cart.getOrderItemList().size()==0) {
					//购物车明细数量小于0，移除该明细
					cartList.remove(cart);
				}
				
			}
			
			
			
		}
		
		return cartList;
	}
	
	/**
	 * 根据商家ID查询购物车对象
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		
		for(Cart cart : cartList) {
			if(cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	
	
	/**
	 * 创建订单明细
	 * @return
	 */
	private TbOrderItem createOrderItem(TbItem item,Integer num) {
		
		if(num<0) {
			throw new RuntimeException("商品数量异常");
		}
		TbOrderItem orderItem = new TbOrderItem();
		
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));//设置总金额
		return orderItem;
	}
	
	
	/**
	 * 根据商品id查询订单明细
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList ,Long itemId) {
		
		for(TbOrderItem orderItem : orderItemList) {
			if(orderItem.getItemId().longValue()==itemId.longValue()) {
				return orderItem;
			}
		}
		
		return null;
	}

	/**
	 * 从redis中提取购物车数据
	 */
	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中提取购物车数据");
		
		List<Cart> cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		
		if(cartList==null) {
			cartList = new ArrayList<>();
		}
		
		return cartList;
	}

	
	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("向redis中存储数据..."+username);
		
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	/**
	 * 合并购物车
	 */
	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		System.out.println("合并购物车");
		
		for(Cart cart2: cartList2) {
			
			for(TbOrderItem orderItem:cart2.getOrderItemList()) {
				
				cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
			
		}
		
		return cartList1;
	}

}
