//服务层
app.service('cartService', function ($http) {

    //读取列表数据绑定到表单中
    this.findCartList = function () {
        return $http.get('../cart/findCartList.do');
    }

    this.addGoodsToCartList = function (itemId, num) {
        return $http.get('../cart/addGoodsToCartList.do?itemId=' + itemId + '&num=' + num);
    }

    //获取地址列表
    this.findAddressList = function () {
        return $http.get('address/findListByLoginUser.do');
    }

    //保存订单
    this.submitOrder = function (order) {
        return $http.post('order/add.do', order);
    }

});
