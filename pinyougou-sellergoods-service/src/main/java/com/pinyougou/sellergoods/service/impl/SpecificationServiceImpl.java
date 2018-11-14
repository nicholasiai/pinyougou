package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		// 添加商品规格
		specificationMapper.insert(specification.getSpecification());

		List<TbSpecificationOption> list = specification.getSpecificationOptionList();
		// 遍历添加商品规格选项
		for (TbSpecificationOption tbSpecificationOption : list) {
			// 设置商品规格的id值
			tbSpecificationOption.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insert(tbSpecificationOption);
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification) {
		
		specificationMapper.updateByPrimaryKey(specification.getSpecification());
		
		//删除原有的商品柜规格选项
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		
		com.pinyougou.pojo.TbSpecificationOptionExample.Criteria createCriteria = example.createCriteria();
		
		createCriteria.andSpecIdEqualTo(specification.getSpecification().getId());
		
		specificationOptionMapper.deleteByExample(example);
		
		List<TbSpecificationOption> optionList = specification.getSpecificationOptionList();
		//添加规格选项
		for (TbSpecificationOption option : optionList) {
			option.setSpecId(specification.getSpecification().getId());//设置规格id
			specificationOptionMapper.insert(option);//添加规格
		}
		
		
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id) {

		// 创建Specification对象
		Specification specification = new Specification();
		// 查询TbSpecification对象
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		// 设置条件
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		com.pinyougou.pojo.TbSpecificationOptionExample.Criteria createCriteria = example.createCriteria();
		createCriteria.andSpecIdEqualTo(id);
		// 查询TbSpecificationOption集合
		List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(example);
		// 封装参数
		specification.setSpecification(tbSpecification);
		specification.setSpecificationOptionList(specificationOptionList);

		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		
		
		
		for (Long id : ids) {
			//批量删除规格选项
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			com.pinyougou.pojo.TbSpecificationOptionExample.Criteria createCriteria = example.createCriteria();
			createCriteria.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(example);
			//批量删除规格
			specificationMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSpecificationExample example = new TbSpecificationExample();
		Criteria criteria = example.createCriteria();

		if (specification != null) {
			if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
				criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
			}

		}

		Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}

}
