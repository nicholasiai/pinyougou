app.controller("searchController",function($scope,$location,searchService){
	
	
	//加载查询字符串
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords= $location.search()['keywords'];
		$scope.search();
	}
	
	
	//搜索查询
	$scope.search=function(){
		if($scope.searchMap.keywords!=null && !$scope.searchMap.keywords==""){
			$scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ; //将页码字符换转换为int类型
			
			searchService.search($scope.searchMap).success(
					function(response){
						$scope.resultMap=response; //搜索返回的结果
						
						buildPageLabel();//调用分页方法
					}
			);
		}
		
		return;
	}
	
	
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sort':'' };//初始化搜索对象
	
	
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		if(key=='category'||key=='brand'|| key=='price'){  
			$scope.searchMap[key]=value;
		}else{ //选中的是规格选项
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//执行搜索
	}
	
	//移除搜索项
	$scope.removeSearchItem=function(key){
		if(key=='category'||key=='brand'|| key=='price'){  
			$scope.searchMap[key]=""
		}else{
			delete $scope.searchMap.spec[key]; //移除此属性
		}
		$scope.search();//执行搜索
	}
	
	//构建分页标签
	
	buildPageLabel=function(){
		$scope.pageLabel=[];//新增分页栏属性 
		var maxPageNo= $scope.resultMap.totalPages;//得到最后页码
		var firstPage=1;  //开始页码
		var lastPage= maxPageNo; //结束页码
		
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后边有点
		if($scope.resultMap.totalPages>5){ //总页码数大于5，显示部分页码
			
			if($scope.searchMap.pageNo<=3){ //如果当前页码小于3，结束页码为5
				
				lastPage=5;  
				$scope.firstDot=false;//前面没点
				
			}else if($scope.searchMap.pageNo >= lastPage-2){ //如果当前页大于等于最大页码-2
				firstPage=lastPage-4;  //开始页码等于最大页码数减4
				$scope.lastDot=false;//后边有点
				
			}else{  //在页码中心，显示前2后2
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
			
		}else{ //总页码数小于5
			$scope.firstDot=false;//前面无点
			$scope.lastDot=false;//后边无点
		}
		
		//循环遍历页码
		
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){ //当前页码小于1 或者页码大于最大页码数
			return;
		}
		
		$scope.searchMap.pageNo=pageNo; 
		$scope.search();
		
	}
	
	//判断当前页为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	//判断当前页是否未最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
	
	//排序查询
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		$scope.search();
	}
	
	//判断搜索内容是否为品牌
	$scope.keywordsIsBrand=function(){
		
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
				//搜索词中包含品牌
				return true;
			}
		}
		return false;
	}
	
	
	
	
	
})