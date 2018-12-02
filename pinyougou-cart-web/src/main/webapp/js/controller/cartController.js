app.controller("cartController",function($scope,cartService){
	
	
	//查询购物车列表
	$scope.findCartList=function(){
		
		cartService.findCartList().success(
				function(response){
					$scope.cartList=response;
					$scope.totalValue=cartService.sum($scope.cartList);//求合计数
				}
		)
	};
	
	/**
	 * 加减数量
	 */
	$scope.addGoodsToCartList=function(itemId,num){
		
		cartService.addGoodsToCartList(itemId,num).success(
				function(response){
					if(response.success){
						$scope.findCartList(); //刷新购物车列表
					}else{
						alert(response.message); //弹出错误提示信息
					}
				}
		)
	}
	
	/**
	 * 获取收货列表
	 */
	$scope.findAddressList=function(){
		cartService.findAddressList().success(
				function(response){
					$scope.addressList=response;
					
					//默认选中地址
					for(var i=0;i<$scope.addressList.length;i++){
						if($scope.addressList[i].isDefault=='1'){
							$scope.address=$scope.addressList[i];
							break;
						}
						
					}
				}
		);
	}
	
	//选择地址
	$scope.selectAddress=function(address){
		$scope.address=address; 
	}
	
	
	//判断当前是否是选中地址
	$scope.isSelectedAddress=function(address){
		if(address==$scope.address){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.order={paymentType:'1'};
	
	//支付方式
	$scope.selectPayType=function(type){
		scope.order.paymentType=type;
	}
	
	//保存订单
	$scope.submitOrder=function(){
		
		$scope.order.receiverAreaName=$scope.address.address;//地址
		$scope.order.receiverMobile=$scope.address.mobile;//手机
		$scope.order.receiver=$scope.address.contact;//联系人
		
		cartService.submitOrder($scope.order).success(
				function(response){
					//页面跳转
					if(response.success){
						if($scope.order.paymentType=='1'){ //微信支付
							location.href="pay.html";
						}else{ //货到付款
							location.href="paysuccess.html";
						}
						
					}else{
						alert(response.message);
					}
				}
			);
	}
	
});