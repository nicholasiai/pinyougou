app.service('brandService',function( $http){
		//查询所有商标
	 	this.findAll = function() {
			return	$http.get('../brand//findAll.do')
		};
		
	 	//查询所有品牌并分页
		this.findPage = function(page, size) {
			return $http.get('../brand/findPage.do?page=' + page + '&size='+ size)
		};
		
		//添加品牌方法
		this.add=function(entity){
			return $http.post("../brand/add.do",entity);
		}
		
		//修改品牌方法
		this.update=function(entity){
			return $http.post("../brand/update.do",entity);
		}
		
		//根据id查询品牌
		this.findOne=function(id){
			return $http.get("../brand/findOne.do?id="+id);
		}
		
		//删除品牌
		this.dele=function(selectIds){
			return $http.get("../brand/delete.do?ids="+selectIds);
		}
		
		//条件查询
		this.search=function(page,size,searchEntity){
			return $http.post("../brand/search.do?page=" + page + "&size="+ size,searchEntity)
		}
		//返回所有下拉列表商标
		this.selectOptionList=function(){
			return $http.get("../brand/selectOptionList.do")
		}
		
	});