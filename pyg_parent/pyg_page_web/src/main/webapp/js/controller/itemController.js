app.controller('itemController', function ($scope, $http) {
    //数量操作
    $scope.addNum = function (x) {
        $scope.num = $scope.num + x;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    }

    //记录用户选择规格
    $scope.specificationItems = {};
    //用户选择的规格
    $scope.selectSpecification = function (name, value) {
        $scope.specificationItems[name] = value;
        searchSku();//读取Sku
    }
    //判断某规格是否被选中
    $scope.isSelected = function (name, value) {
        if ($scope.specificationItems[name] == value) {
            return true;
        } else {
            return false;
        }
    }

    //加载默认的SKU
    $scope.loadSku = function () {
        $scope.sku = skuList[0];
        //深克隆
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //选择规格更新SKU
    //匹配两个对象
    matchObject = function (map1, map2) {
        //遍历JSON数组
        for (var k in map1) {
            if (map1[k] != map2[k]) {
                return false;
            }
        }
        for (var k in map2) {
            if (map2[k] != map1[k]) {
                return false;
            }
        }
        return true;
    }

    //查询用户选择当前的SKU
    searchSku = function () {
        for (var i = 0; i < skuList.length; i++) {
            if (matchObject(skuList[i].spec, $scope.specificationItems)) {
                $scope.sku = skuList[i];
                return;
            }
        }
        //如果没有对应的
        $scope.sku = {id: 0, title: '-----------', price: 0}
    }

    //添加商品到购物车
    $scope.addToCart = function () {
        // alert('skuid:' + $scope.sku.id)
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId=' + $scope.sku.id + '&num=' + $scope.num, {'withCredentials': true}).success(function (response) {
            if (response.success) {
                location.href = 'http://localhost:9107/cart.html'
            } else {
                alert(response.message)
            }
        });
    }
});
