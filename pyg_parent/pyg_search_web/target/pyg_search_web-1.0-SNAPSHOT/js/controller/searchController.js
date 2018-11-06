app.controller('searchController', function ($scope, $location, searchService) {

    //创建搜索对象
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 20,
        'sortField': '',
        'sort': ''
    };


    //判断关键字中是否包含品牌字段
    $scope.keywordsContainsBrand = function () {

        //包含  true   不包含  false
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;
            }
        }

        return false;
    }

    //加载首页查询字符串
    $scope.loadkeywords = function () {
        // alert(11111 + $location.search()["keywords"])
        $scope.searchMap.keywords = $location.search()["keywords"];
        // alert($scope.searchMap.keywords)
        if ($scope.searchMap.keywords != null) {
            $scope.search();
        }

    }

    //设置排序规则
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    //添加搜索条件
    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value
        } else {
            $scope.searchMap.spec[key] = value
        }
        $scope.search()
    }

    //撤销面包屑搜索条件
    $scope.removeSearchItem = function (key) {
        if (key == "category" || key == "brand" || key == 'price') {//如果是分类或品牌
            $scope.searchMap[key] = "";
        } else {//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        $scope.search()
    }


    //搜索
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo)
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//搜索返回的结果
                //调用
                buildPageLabel();
            }
        );
    }

    //构建分页标签
    buildPageLabel = function () {
        //新增分页栏属性
        $scope.pageLabel = []
        //得到最后页码 也就是总页码
        var maxPageNo = $scope.resultMap.totalPages;
        //起始页码
        var firstPage = 1
        //截至页码
        var lastPage = maxPageNo;
        $scope.firstDot = true;//前面有点
        $scope.lastDot = true;//后面有点
        //如果查询总页数大于5，显示部分页码
        if ($scope.resultMap.totalPages > 5) {
            //如果当前页码小于等于3，则显示截至页码为5
            if ($scope.searchMap.pageNo <= 3) {
                lastPage = 5;
                $scope.firstDot = false;//前面没点
            } else if ($scope.searchMap.pageNo >= maxPageNo - 2) {
                //如果当前页码大于总页码-2，只显示后5页
                firstPage = maxPageNo - 4
                $scope.lastDot = false;//后面没点
            } else {
                //显示以当前页为中心的5页
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            $scope.firstDot = false;//前面无点
            $scope.lastDot = false;//后边无点
        }

        //循环产生页码标签
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
            // alert(i)
        }
    }

    //根据页码查询
    $scope.queryByPage = function (pageNo) {
        //页码验证
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //判断当前页为第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }

    //判断当前页是否未最后一页
    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }
});