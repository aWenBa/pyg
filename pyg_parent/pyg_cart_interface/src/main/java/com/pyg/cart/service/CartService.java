package com.pyg.cart.service;

import com.pyg.pojogroup.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到已经存在的购物车列表中
     *
     * @param cartList : 已经存在的购物车列表
     * @param itemId   : 要添加商品的sku 的id
     * @param num      : 要添加的数量
     * @return :  添加商品后的购物车列表
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 从redis中查询购物车
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis
     */
    public void saveCartListToRedis(String username, List<Cart> cartList);

    /**
     * 合并购物车
     */
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);
}
