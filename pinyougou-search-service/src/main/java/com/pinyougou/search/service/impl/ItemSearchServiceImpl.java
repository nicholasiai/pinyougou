package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.w3c.dom.ls.LSInput;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService{
	
	@Autowired
	private SolrTemplate solrTemplate;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Override
	public Map<String, Object> search(Map searchMap){
		Map<String,Object> map = new HashMap<>();
		
		/*  普通查询
		 * Query query = new SimpleQuery();
		//添加查询条件
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		
		query.addCriteria(criteria);
		
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		
		//将查询结果存入到map返回
		map.put("rows", page.getContent());*/
		
		
		
		//1.高亮查询列表
		map.putAll(searchList(searchMap));
		
		//2.根据关键字查询商品分类
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		
		//3.根据商品分类查询品牌和规格列表
		String categoryName=(String)searchMap.get("category");
		if(!"".equals(categoryName)) { //分类选项不为空
			map.putAll(searchBrandAndSpecList(categoryName));
			
		}else {//如果没有分类名称，按照第一个查询
			if (categoryList.size()>0) {
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
		
		
		return map;
	}
	
	/**
	 * 关键字搜索高亮显示
	 * @return
	 */
	private Map searchList(Map searchMap) {
		
		//关键字空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		
		Map map = new HashMap<>();
		
		//******初始化高亮选项******
		HighlightQuery query = new SimpleHighlightQuery();//创建高亮查询
		HighlightOptions options = new HighlightOptions().addField("item_title");//设置高亮查询域
		options.setSimplePrefix("<em style='color:red'>");//设置高亮前缀
		options.setSimplePostfix("</em>");//设置高亮后缀
		query.setHighlightOptions(options);//设置高亮选项
		
		//1.1关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));//添加查询条件
		query.addCriteria(criteria);
		
		//1.2按分类过滤
		if(!"".equals(searchMap.get("category"))) {
			Criteria criteriaFilter = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
			query.addFilterQuery(filterQuery);
		}
		
		//1.3按品牌过滤
		if(!"".equals(searchMap.get("brand"))) {
			Criteria criteriaFilter = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
			query.addFilterQuery(filterQuery);
		}
		
		//1.4按规格过滤
		if(searchMap.get("spec")!=null) {
			Map<String,String> specMap= (Map) searchMap.get("spec");
			for(String key: specMap.keySet()) {
				Criteria criteriaFilter = new Criteria("item_spec_"+key).is(specMap.get(key));
				FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
				query.addFilterQuery(filterQuery);	
			}
		}
		
		//1.5按价格过滤
		if(!"".equals(searchMap.get("price"))) {
			String[] price = ((String) searchMap.get("price")).split("-");
			if(!price[0].equals("0")) {//如果区间起点不等于 0  则大于起始点
				Criteria criteriaFilter = new Criteria("item_price").greaterThanEqual(price[0]);
				FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
				query.addFilterQuery(filterQuery);
			}
			
			if(!price[1].equals("*")) {//如果区间终点不等于* 则小于*
				Criteria criteriaFilter = new Criteria("item_price").lessThanEqual(price[1]);
				FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
				query.addFilterQuery(filterQuery);
			}
			
		}
		
		//1.6分页查询
		Integer pageNo = (Integer) searchMap.get("pageNo"); //获取当前页码
		if(pageNo==null) {
			pageNo=1;  //默认为第一页
		}
													
		Integer pageSize = (Integer) searchMap.get("pageSize"); //获取每页记录数
		if(pageSize==null) {
			pageSize=20;  //默认显示20条
		}
		
		query.setOffset((pageNo-1)*pageSize); //从第几条开始查询
		query.setRows(pageSize);
		
		//1.7排序
		String sortValue= (String) searchMap.get("sort");//ASC DESC
		String sortField= (String) searchMap.get("sortField");//排序字段
		
		if(sortValue!=null && !sortValue.equals("")) {
			
			if(sortValue.equals("ASC")){  //升序排列
				Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			
			if(sortValue.equals("DESC")){  //降序排列
				Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
			
		}
		
		//执行查询
		//*******获取高亮结果集*******
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();//获取高亮查询入口
		
		for(HighlightEntry<TbItem> h:highlighted) {
			TbItem entity = h.getEntity();//获取原数据
			if(h.getHighlights().size()>0&&h.getHighlights().get(0).getSnipplets().size()>0) {
				//设置高亮结果
				entity.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//返回总页数
		map.put("total", page.getTotalElements());//返回总记录数
		
		return map;
	}
	
	/**
	 * 查询分类列表
	 * @param searchMap
	 * @return
	 */
	public List searchCategoryList(Map searchMap) {
		
		List<String> list= new ArrayList<>();
		
		Query query = new SimpleQuery();
		
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));//添加查询条件
		
		query.addCriteria(criteria);
		
		GroupOptions options = new GroupOptions().addGroupByField("item_category");//添加分组的域
		
		query.setGroupOptions(options);
		
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class); //获取分组结果集
		
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");//获取分组页入口页
		
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();//得到分组入口集合
		
		List<GroupEntry<TbItem>> content = groupEntries.getContent(); //获取分组内容集合
		
		for(GroupEntry<TbItem> entity:content) {
			list.add(entity.getGroupValue()); //将分组结果存入集合
		}
		
		return list;
	}
	
	/**
	 * 查询品牌和规格列表
	 * @return
	 */
	public Map searchBrandAndSpecList(String category) {
		Map map = new HashMap<>();
		
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (typeId!=null) {
			//查询品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);
			//查询规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
		}
		return map;
	}

	/**
	 * 导入数据
	 */
	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}

	/**
	 * 删除数据
	 */
	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
	
	
	
	
}
