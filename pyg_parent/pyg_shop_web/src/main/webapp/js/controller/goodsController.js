//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        var id = $location.search()['id'];
        if (id == null) {
            return;
        }

        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //向富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction)
                //超链接‘修改’触发时，图片回显
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //回显商品规格属性
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems)
                // entity.goods.isEnableSpec = "1" 错的
                // $scope.entity.goods.isEnableSpec = "1"
                //SKU列表规格转换
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec)
                }
            }
        );
    }

    // $scope.checkAttributeValue = function (specName, optionName) {
    //     var items = $scope.entity.goodsDesc.specificationItems
    //     var object = $scope.searchObjectByKey(items, "attributeName", specName)
    //     if (object == null) {
    //         return false;
    //     } else {
    //         if (object.attributeName.indexOf(optionName) >= 0) {
    //             return true
    //         } else {
    //             return true
    //         }
    //     }
    // }

    //判断该规格选项在勾选结果中是否存在，如果存在，返回true，复选框是勾选状态 如果不存在，返回false
    $scope.isChecked = function (name, value) {
        var specItems = $scope.entity.goodsDesc.specificationItems;//勾选结果
        //[{"attributeValue":["移动4G","联通3G"],"attributeName":"网络"},{"attributeValue":["32G","64G"],"attributeName":"机身内存"}]
        //判断当前规格在勾选结果中是否存在，如果存在返回对象，如果不存在返回null
        var object = $scope.searchObjectByKey(specItems, 'attributeName', name);
        if (object != null) {//说明规格在勾选结果中存在，这个时候才会返回true
            if (object.attributeValue.indexOf(value) >= 0) {//说明该选项被选中
                return true;
            } else {
                return false;
            }
        } else {//当前规格没有任何选项被选中，所有返回false
            return false;
        }
    }

    //定义页面实体结构
    $scope.entity = {
        // goods: {isEnableSpec: "1"},
        goods: {},
        goodsDesc: {itemImages: [], specificationItems: []},
        itemList: []
    }

    // $scope.image_entity = {};

    //清空文件名
    $scope.empty = function () {
        document.getElementById("file").value = "";
    }


    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            $scope.entity.goodsDesc.introduction = editor.html()
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    location.href = "goods.html"
                    //重新查询
                    // $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    };

    //添加图片列表
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity)

    }

    //列表移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //上传图片
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            if (response.success) {

                //如果上传成功，取出url,设置文件地址
                $scope.image_entity.url = response.message
            } else {
                alert(response.message)
            }
        })
    }

    //下拉框，读取一级分类
    //用来检测变量 1 被检测的变量 2function(变量新值，变量的旧值)
    $scope.selectItemCat1List = function () {

        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response
            }
        )
    }


    //下拉框，读取二级分类
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        if (newValue != undefined) {
            //newValue是选择后的第一级分类id 查询第二级
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List = response
                }
            )
        }
    })

    //下拉框，读取三级分类
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        if (newValue != undefined) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List = response
                }
            )
        }
    })

    //三级分类选择后  读取模板ID
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        if (newValue != undefined) {
            itemCatService.findOne(newValue).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId = response.typeId; //更新模板ID
                }
            )
        }
    })

    //模板ID选择后  更新品牌列表
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        if (newValue != undefined) {
            typeTemplateService.findOne(newValue).success(
                function (response) {
                    $scope.typeTemplate = response;//获取类型模板
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表
                }
            );
        }
    });

    //模板ID选择后  更新模板对象
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        if (newValue != undefined) {
            typeTemplateService.findOne(newValue).success(
                function (response) {
                    $scope.typeTemplate = response;//获取类型模板
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表
                    //删除扩展属性相关
                }
            );
            //查询规格列表
            typeTemplateService.findSpecList(newValue).success(
                function (response) {
                    $scope.specList = response;
                }
            );
        }
    });


    //勾选规格选项，生成结果  1 规格名称  2 规格选项
    $scope.updateSpecAttribute = function ($event, name, value) {
        var object = $scope.searchObjectByKey(
            $scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        if (object != null) {
            if ($event.target.checked) {
                object.attributeValue.push(value);
            } else {//取消勾选
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);//移除选项
                //如果选项都取消了，将此条记录移除
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {
            $scope.entity.goodsDesc.specificationItems.push(
                {"attributeName": name, "attributeValue": [value]});
        }
    }


    //生成SKU列表
    $scope.createItemList = function () {
        var specItems = $scope.entity.goodsDesc.specificationItems;

        $scope.entity.itemList = [{
            spec: {},
            price: 0,
            num: 99999,
            status: 1,
            isDefault: 0
        }];

        //根据勾选结果去生成列表
        //[{"attributeName":"网络","attributeValue":["移动4G",'联通3G']}，{"attributeName":"机身内存","attributeValue":["32G","64g"]}]
        for (var i = 0; i < specItems.length; i++) {
            //上一次生成的列表和本次的规格生成新的列表

            $scope.entity.itemList = addColumn($scope.entity.itemList, specItems[i].attributeName, specItems[i].attributeValue);
        }

    }

    //上一次生成的列表和本次的规格生成新的列表  (上次的列表，规格名称，本次的规格数组)
    addColumn = function (list, name, values) {
        var newList = [];

        for (var i = 0; i < list.length; i++) {//循环上次的列表
            var oldRow = list[i];//列表中原来的一条数据

            for (var j = 0; j < values.length; j++) {//循环本次的规格
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[name] = values[j];//为一条SKU中spec字段赋值，key（规格名称）-value（规格选项）
                newList.push(newRow);
            }

        }
        return newList;
    }

    //显示状态
    $scope.status = ['未审核', '已审核', '审核未通过', '关闭']

    //显示分类
    $scope.itemCatList = [];//商品分类列表
    //加载商品分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        )
    }


});
