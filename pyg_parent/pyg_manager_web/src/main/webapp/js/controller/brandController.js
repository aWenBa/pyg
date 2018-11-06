app.controller('brandController', function ($scope, $controller, brandService) {
    $controller('baseController', {$scope: $scope});
    //            $scope.findAll = function () {
//                brandService.findAll().success(
//                    function (response) {
//                        $scope.list = response
//                    }
//                )
//            }


    //保存

    $scope.save = function () {
        var methodObject;
        if ($scope.entity.id != null) {
            methodObject = brandService.update($scope.entity);
        } else {
            methodObject = brandService.add($scope.entity);
        }
        methodObject.success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }

            }
        );
    }


    //回显
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;

            }
        )
    }

    //保存 和增加代码重复，修改增加代码即可

    //删除

    //2.根据选中的删除
    $scope.dele = function () {
        brandService.dele($scope.selectIds).success(
            function (data) {
                if (data.success) {
                    $scope.reloadList();
                }
            }
        )

    }

    //条件查询
    $scope.searchEntity = {};
    $scope.search = function (page, rows) {
        brandService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.paginationConf.totalItems = response.total;
                $scope.list = response.rows;
            }
        )
    }


})

