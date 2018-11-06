package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //2.获取商家ID
        String sellerName = item.getSeller();
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(sellerName);
            List<TbOrderItem> orderItemList = new ArrayList<>();
            //根据tbItem  num   --------> TbOrderItem
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        } else {
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchItem(cart.getOrderItemList(), itemId);
            //5.1. 如果没有，新增购物车明细
            if (orderItem == null) {
                //根据商品sku和数量创建tbOrderItem对象
                TbOrderItem tbOrderItem = createOrderItem(item, num);
                //新增购物车明细
                cart.getOrderItemList().add(tbOrderItem);
            } else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);//更改数量
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));//小计
                //商品数量为0
                if(orderItem.getNum() <= 0){
                    //将对应商品移除
                    cart.getOrderItemList().remove(orderItem);
                }

                //购物车中的商品为0
                if(cart.getOrderItemList().size() == 0){
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中获取购车的数据" + username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis中存入数据" + username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    //登陆后合并购物车
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 判断明细列表中是否存在该商品
     *
     * @param orderItemList : 明细列表
     * @param itemId        : 要添加的商品id
     * @return
     */
    private TbOrderItem searchItem(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().equals(itemId)) {
                return orderItem;
            }
        }
        return null;
    }


    //根据tbItem  num   --------> TbOrderItem

    /**
     * 根据商品sku和数量创建tbOrderItem对象
     *
     * @param item : sku
     * @param num  : 数量
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setTitle(item.getTitle());//标题
        orderItem.setPrice(item.getPrice());//价格
        orderItem.setItemId(item.getId());//sku
        orderItem.setGoodsId(item.getGoodsId());//spu
        orderItem.setPicPath(item.getImage());//图片
        orderItem.setSellerId(item.getSellerId());//商家id
        orderItem.setNum(num);//数量
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * num));//小计
        return orderItem;
    }

    //根据商家ID判断购物车列表中是否存在该商家的购物车
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }


}
