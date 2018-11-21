package com.pinyougou.solrutil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.druid.support.json.JSONParser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

@Component
public class SolrUtil {
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private SolrTemplate solrTemplate;
	
	/**
	* 导入商品数据
	*/
	public void importItemData() {
		
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		
		criteria.andStatusEqualTo("1");  //已审核商品
		
		List<TbItem> tbItems = itemMapper.selectByExample(example);
		
		System.out.println("----商品列表----");  
		
		for(TbItem tbItem : tbItems) {
			Map map = JSON.parseObject(tbItem.getSpec());//将 spec 字段中的 json 字符串转换为 map
			tbItem.setSpecMap(map);//给带注解的字段赋值 
			System.out.println(tbItem.getTitle());
		}
		
		solrTemplate.saveBeans(tbItems); //储存到solr
		solrTemplate.commit();
		System.out.println("----总数："+tbItems.size()+"----");
		System.out.println("----结束----");
		
		
		
	}
	
	
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*");
		
		SolrUtil solrUtil = context.getBean("solrUtil",SolrUtil.class);

		
		solrUtil.importItemData();
		
	}
	
	
	/**
	 * 添加单个solr
	 */
	public void add() {
		TbItem item = new TbItem();
		
		item.setId(1L);
		item.setBrand("华为");
		item.setCategory("手机");
		item.setGoodsId(1L);
		item.setSeller("华为 2 号专卖店");
		item.setTitle("华为 Mate9");
		item.setPrice(new BigDecimal(2000));
		solrTemplate.saveBean(item);
		solrTemplate.commit();
		
	}
	
	/**
	 * 根据id删除solr
	 */
	public void dele() {
		solrTemplate.deleteById("1");
		solrTemplate.commit();
		
	}

}
