package com.pinyougou.content.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import com.pinyougou.content.service.ContentService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		//清楚redis缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		
		contentMapper.insert(content);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//清楚修改前的redis缓存
		Long oldCategoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		
		redisTemplate.boundHashOps("content").delete(oldCategoryId);
		
		//如果分类 ID 发生了修改,清除修改后的分类 ID 的缓存
		if (oldCategoryId.longValue()!=content.getCategoryId().longValue()) {
			//删除新ID的缓存
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
		
		contentMapper.updateByPrimaryKey(content);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//删除redis缓存
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
			redisTemplate.boundHashOps("content").delete(categoryId);
			
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
		/**
		 * 根据广告类型ID查询列表
		 */
		@Override
		public List<TbContent> findByCategoryId(Long categoryId) {
			//尝试获取缓存数据
			List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
			
			if(contentList==null) {
				System.out.println("从数据库中读取数据");
				
				TbContentExample example = new TbContentExample();
				
				Criteria criteria = example.createCriteria();
				
				criteria.andCategoryIdEqualTo(categoryId);
				
				criteria.andStatusEqualTo("1");//选择开启状态的广告
				
				example.setOrderByClause("sort_order");//默认排序 
				
				contentList = contentMapper.selectByExample(example);
				
				//将数据存入缓存
				redisTemplate.boundHashOps("content").put(categoryId, contentList);
				
			}else {
				System.out.println("从缓存中读取数据");
			}
			
			return contentList;
			
			
		}
	
}
