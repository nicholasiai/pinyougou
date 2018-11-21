var app = angular.module('pinyougou', []);


//服务过滤器
app.filter("trustHtml",['$sce',function($sce){
	
	return function(data){
		
		return $sce.trustAsHtml(data);  //信任展示标签的html代码
	}
	
	
}])