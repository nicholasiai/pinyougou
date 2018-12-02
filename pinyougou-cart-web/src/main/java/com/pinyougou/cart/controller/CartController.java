package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonParser;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;
import util.CookieUtil;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Reference(timeout=6000)
	private CartService cartService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	
	/**
	 * 查询购物车列表
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();	
		//读取本地cookie
		String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		
		if(cartListString==null||cartListString.equals("")) {
			cartListString = "[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		
		if (username.equals("anonymousUser")) {//用户未登录,只读取本地cookie
			
			return cartList_cookie;
		}else {//用户已登录，从redis和cookie中读取数据
			
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			
			if(cartList_cookie.size()>0) { //cookie中有数据
				
				//合并购物车
				cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
				
				//清除cartList_cookie
				CookieUtil.deleteCookie(request, response, "cartList");
				
				//将合并后的数据存入 redis
				cartService.saveCartListToRedis(username, cartList_redis);
			}
			
			return cartList_redis;
		}
		
		
		
		
	}
	
	/**
	 * 添加商品到购物车集合
	 * @param itemId
	 * @param num
	 * @return
	 */
	@RequestMapping("/addGoodsToCartList")
	@CrossOrigin(origins="http://localhost:9105",allowCredentials="true") //允许跨域访问
	public Result addGoodsToCartList(Long itemId,Integer num) {
		
		//response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105"); //允许跨域请求
		//response.setHeader("Access-Control-Allow-Credentials", "true"); //允许操作cookie
		
		try {
			//得到登陆人账号,判断当前是否有人登陆
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			System.out.println("登录用户："+username);
			
			List<Cart> cartList = findCartList(); //获取当前购物车列表
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			
			if(username.equals("anonymousUser")){
				//当前用户未，存入到cookie中
				CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600*24, "UTF-8");
				System.out.println("向 cookie 存入数据");
			}else {
				//当前用户已登录，存入到redis中
				cartService.saveCartListToRedis(username, cartList);
				System.out.println("向 redis 存入数据");
			}
			
			return new Result(true,"添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
		
	}
	
	
	
}
