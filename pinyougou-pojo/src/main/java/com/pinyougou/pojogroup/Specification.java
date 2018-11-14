package com.pinyougou.pojogroup;

import java.io.Serializable;
/**
 * 规格列表实体类
 * @author IAI
 *
 */
import java.util.List;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
public class Specification implements Serializable{
	
	private TbSpecification specification;
	private List<TbSpecificationOption> specificationOptionList;;
	
	public TbSpecification getSpecification() {
		return specification;
	}
	public void setSpecification(TbSpecification specification) {
		this.specification = specification;
	}
	public List<TbSpecificationOption> getSpecificationOptionList() {
		return specificationOptionList;
	}
	public void setSpecificationOptionList(List<TbSpecificationOption> specificationOptionList) {
		this.specificationOptionList = specificationOptionList;
	}
	
	
	
	
	
	

}
