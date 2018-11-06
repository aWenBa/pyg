package com.pyg.sellergoods.service;

import entity.PageResult;
import com.pyg.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    public List<TbBrand> findAll();


    //分页查询
    public PageResult findPage(int pageNum, int pageSize);

    public void add(TbBrand brand);

    //修改
    public TbBrand findOne(long id);

    public void update(TbBrand brand);

    //删除
    public void delete(long[] ids);

    //条件查询
    public PageResult findPage(TbBrand brand,int pageNum, int pageSize);

    /**
     * 品牌下拉框数据
     */
    List<Map> selectOptionList();
}
