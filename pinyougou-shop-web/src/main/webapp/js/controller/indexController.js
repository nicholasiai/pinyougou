app.controller("indexController",function($scope,$controller,loginService){
	
	//获取登录用户姓名
	$scope.showLoginName=function(){
		loginService.loginName().success(function(reponse){
			$scope.loginName=reponse.loginName;
			
		})
		
		
	}
	
})