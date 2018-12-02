package com.pinyougou.search.service.impl;

import java.io.Serializable;
import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.pinyougou.search.service.ItemSearchService;

@Component
public class ItemDeleteListener implements MessageListener{

	@Autowired
	private ItemSearchService itemSearchService;

	@Override
	public void onMessage(Message session) {
		
		ObjectMessage objectMessage = (ObjectMessage) session;
		Long[] goodsIds;
		try {
			goodsIds = (Long[]) objectMessage.getObject();
			System.out.println("监听接收消息....."+goodsIds);
			itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
			System.out.println("成功删除索引库中的记录"); 
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}
	
	

}
