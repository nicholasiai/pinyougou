package com.pinyougou.search.service.impl;

import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Component
public class ItemSearchListener implements MessageListener{

	@Autowired
	private ItemSearchService itemSearchListener;
	
	@Override
	public void onMessage(Message message) {
		
		
		TextMessage textMessage = (TextMessage) message;
		try {
			String text = textMessage.getText();
			System.out.println("监听接收消息"+text);
			List<TbItem> itemList = JSON.parseArray(text,TbItem.class);
			for(TbItem item: itemList) {
				Map specMap = JSON.parseObject(item.getSpec());//将 spec 字段中的 json字符串转换为 map
				item.setSpecMap(specMap);//给带注解动态域的字段赋值
			}
			
			itemSearchListener.importList(itemList);
			System.out.println("成功导入到索引库");
		} catch (JMSException e) {
			
			e.printStackTrace();
		}
		
		
		
	}

	

}
