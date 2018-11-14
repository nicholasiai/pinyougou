app.service("loginService",function($http){

	
	//获取登录用户姓名
	this.loginName=function(){
		return $http.get("../login/name.do");
	}
	
	
	
});