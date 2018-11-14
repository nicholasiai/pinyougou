package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;
	
	
	/**
	 * 查询所有商标
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();
	}
	
	/**
	 * 查询所有商标并分页
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int size) {
		return brandService.findPage(page, size);
	}
	
	/**
	 * 添加品牌
	 * @param brand
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand) {
		try {
			brandService.add(brand);
			return new Result(true,"添加成功");
		} catch (Exception e) {
			return new Result(false,"添加失败");
		}
		
	}
	
	/**
	 * 修改品牌
	 * @param brand
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand) {
		try {
			brandService.update(brand);
			return new Result(true,"修改成功");
		} catch (Exception e) {
			return new Result(false,"修改失败");
		}
		
	}
	
	/**
	 * 根据id查询品牌
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) {
		return brandService.findOne(id);
	}
	
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		return brandService.delete(ids);
	}
	
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand searchEntity,int page,int size) {
		return brandService.search(searchEntity,page,size);
	}
	
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		return brandService.selectOptionList();
		
	}
	
}
