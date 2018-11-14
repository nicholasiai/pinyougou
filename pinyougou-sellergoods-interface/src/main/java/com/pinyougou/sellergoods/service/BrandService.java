package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;
import entity.Result;

public interface BrandService {
	
	
	List<TbBrand> findAll();
	
	PageResult findPage(int page,int size);
	
	void add(TbBrand brand);

	void update(TbBrand brand);

	TbBrand findOne(Long id);

	Result delete(Long[] ids);

	PageResult search(TbBrand searchEntity, int page, int size);
	
	List<Map> selectOptionList(); 

}
