app.controller('brandController',function($scope, $controller,brandService) {
				
	$controller('baseController',{$scope:$scope});//继承
	
				//查询所有商标
			 	$scope.findAll=function(){
					brandService.findAll().success(
						function(response){
							$scope.list=response;
						}			
					);
				} 
				
				
				
				
				
				//查询所有品牌并分页
				$scope.findPage = function(page, size) {
					brandService.findPage(page,size).success(
							function(response) {
						$scope.paginationConf.totalItems = response.total;
						$scope.list = response.rows;
					});
				}
				
				//保存品牌方法
				$scope.save=function(){
					var serviceObject;
					if($scope.entity.id!=null){
						//修改品牌方法
						serviceObject=brandService.update($scope.entity);
					}else{
						//添加品牌方法
						serviceObject=brandService.add($scope.entity);
					}
					serviceObject.success(
							function(response){
								if(response.success){
									$scope.reloadList();
								}else{
									alert(response.message);
								}
							})
					
				}
				
				
				//根据id查询品牌
				$scope.findOne=function(id){
					brandService.findOne(id).success(
							function(response){
								$scope.entity= response;					
							}
						);		
					
				}
				
				
				//删除品牌
				$scope.dele=function(){
					brandService.dele($scope.selectIds).success(
							function(response){
								if(response.success){
									$scope.reloadList();
								}else{
									alert(response.message);
								}
					})
				}
				
				$scope.searchEntity={};//自定义搜索条件
				
				// 条件查询
				$scope.search=function(page,size){
					brandService.search(page,size,$scope.searchEntity).success(
							function(response){
						$scope.paginationConf.totalItems = response.total;
						$scope.list = response.rows;
					});
					
				}
				
			})