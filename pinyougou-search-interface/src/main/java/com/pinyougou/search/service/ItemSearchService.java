package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
	
	/**
	 * 搜索
	 * @return
	 */
	public Map<String,Object> search(Map searchMap);
	
	/**
	 * 导入数据solr
	 * @param list
	 */
	public void importList(List list);
	
	/**
	 * 删除数据solr
	 * @param goodsIdList
	 */
	public void deleteByGoodsIds(List goodsIdList);
}
