package com.linyi.cropseed.trace.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.dto.CartBatchOperateDTO;
import com.linyi.cropseed.trace.dto.CartItemDTO;
import com.linyi.cropseed.trace.entity.Cart;
import com.linyi.cropseed.trace.vo.CartItemVO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<Cart> {

    List<CartItemVO> listCartItems(Long userId);

    CartItemVO addItem(Long userId, CartItemDTO dto);

    CartItemVO updateQuantity(Long userId, Long cartId, Integer quantity);

    CartItemVO updateSelected(Long userId, Long cartId, Boolean selected);

    void batchUpdateSelected(Long userId, CartBatchOperateDTO dto);

    void removeItem(Long userId, Long cartId);

    void removeItems(Long userId, List<Long> cartIds);

    void clearCart(Long userId);
}
