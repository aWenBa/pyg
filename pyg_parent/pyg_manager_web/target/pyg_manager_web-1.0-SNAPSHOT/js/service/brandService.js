app.service('brandService', function ($http) {

    //查询所有
    this.findAll = function () {
        return $http.get('../brand/findAll.do')
    }


    //分页查询
    this.findPage = function (page, rows) {
        return $http.get('../brand/findPage.do?page=' + page + '&rows=' + rows)
    }


    //增加
    this.add = function (entity) {
        return $http.post('../brand/add.do', entity)
    }

    //修改
    this.update = function (entity) {
        return $http.post('../brand/update.do', entity)
    }

    //回显
    this.findOne = function (id) {
        return $http.get('../brand/findOne.do?id=' + id)
    }

    //删除
    this.dele = function (entity) {
       return $http.get('../brand/delete.do?ids=' + entity)
    }

    //条件查询
    this.search = function (page, rows, entity) {
        return $http.post('../brand/search.do?page=' + page + '&rows=' + rows, entity);
    }


    //下拉列表数据
    this.selectOptionList=function(){
        return $http.get('../brand/selectOptionList.do');
    }

});