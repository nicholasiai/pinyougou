package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;
	
	@Override
	public List<TbBrand> findAll() {
		return brandMapper.selectByExample(null);
	}

	@Override
	public PageResult findPage(int page,int size) {
		PageHelper.startPage(page,size);
		Page<TbBrand> pageList = (Page<TbBrand>) brandMapper.selectByExample(null);
		return new PageResult(pageList.getTotal(),pageList.getResult());
	}

	@Override
	public void add(TbBrand brand) {
		brandMapper.insert(brand);
	}

	@Override
	public void update(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);
	}

	@Override
	public TbBrand findOne(Long id) {
		return brandMapper.selectByPrimaryKey(id);
	}

	@Override
	public Result delete(Long[] ids) {
		
		try {
			for (Long id : ids) {
				brandMapper.deleteByPrimaryKey(id);
			}
			return new Result(true,"删除成功");
		} catch (Exception e) {
			return new Result(false,"删除失败");
		}
		
		
	}

	@Override
	public PageResult search(TbBrand searchEntity, int page, int size) {
		TbBrandExample example = new TbBrandExample();
		Criteria criteria = example.createCriteria();
		//分页
		PageHelper.startPage(page, size);
		//判断条件实体类是否为空
		if (searchEntity!=null) {
			//创建条件类
			if (searchEntity.getName()!=null) {
			criteria.andNameLike("%"+searchEntity.getName()+"%");	
			}
			if (searchEntity.getFirstChar()!=null) {
				criteria.andFirstCharLike("%"+searchEntity.getFirstChar()+"%");
			}
		}
		Page<TbBrand> pageEntity = (Page<TbBrand>) brandMapper.selectByExample(example);
	
		return new PageResult(pageEntity.getTotal(),pageEntity.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return brandMapper.selectOptionList();
	}
	
	

}
