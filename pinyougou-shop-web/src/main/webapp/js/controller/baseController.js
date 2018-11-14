app.controller('baseController',function($scope){
	
	
	//重新加载列表
	$scope.reloadList = function() {
		$scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
	}
	
	//分页控件配置
	$scope.paginationConf = {
		currentPage : 1,
		totalItems : 10,
		itemsPerPage : 10,
		perPageOptions : [ 10, 20, 30, 40, 50 ],
		onChange : function() {
			$scope.reloadList();
		}
	};
	
	
	//单选框单机事件
	$scope.selectIds=[];
	$scope.updateSelection=function($event,id){
		if($event.target.checked){
			$scope.selectIds.push(id); //添加id
		}else{
			var index = $scope.selectIds.indexOf(id);
			$scope.selectIds.splice(index,1);  //移除id
		}
	};
	
	
	
	
	
	//全选框单机事件		
	/*$scope.check="";
	$scope.updateAllSelection=function($event){
		if($event.target.checked){
			$scope.check="checked";
				for(i=0;i<$scope.list.length;i++){
					$scope.selectIds.push($scope.list[i].id);
				}
				
		}else{
			$scope.check="";
			for(i=0;i<$scope.list.length;i++){
				var index = $scope.selectIds.indexOf($scope.list[i].id);
				$scope.selectIds.splice(index,1);
				
			}
			
		}
		
	};*/
	
	
	//将json字符串转换为对象字符串
	$scope.jsonToString=function(jsonString,key){
		var json = JSON.parse(jsonString);
		var value="";
		
		for(i=0;i<json.length;i++){
			if(i>0){
				value+=","+json[i][key];
			}else{
				value+=json[i][key];
			}
			
		}
		return value;
	}
	
	//从集合中按照 key 查询对象
	$scope.searchObjectByKey=function(list,key,keyValue){
		
		for(var i=0;i<list.length;i++){
			
			if(list[i][key]==keyValue){
				return list[i];
			}
		}
		
		return null;
	}

	
	
});
	
	
	
