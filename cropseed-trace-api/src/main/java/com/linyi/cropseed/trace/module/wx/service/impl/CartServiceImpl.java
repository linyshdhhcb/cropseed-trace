package com.linyi.cropseed.trace.module.wx.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.module.wx.model.dto.CartBatchOperateDTO;
import com.linyi.cropseed.trace.module.wx.model.dto.CartItemDTO;
import com.linyi.cropseed.trace.module.wx.model.entity.Cart;
import com.linyi.cropseed.trace.module.seed.model.entity.SeedInfo;
import com.linyi.cropseed.trace.module.wx.mapper.CartMapper;
import com.linyi.cropseed.trace.module.seed.mapper.SeedInfoMapper;
import com.linyi.cropseed.trace.module.wx.service.CartService;
import com.linyi.cropseed.trace.module.wx.model.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final CartMapper cartMapper;
    private final SeedInfoMapper seedInfoMapper;

    @Override
    public List<CartItemVO> listCartItems(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        if (CollUtil.isEmpty(carts)) {
            return new ArrayList<>();
        }
        return carts.stream().map(this::buildCartItemVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemVO addItem(Long userId, CartItemDTO dto) {
        Cart existing = cartMapper.selectByUserIdAndSeedId(userId, dto.getSeedId());
        if (existing != null) {
            int newQuantity = existing.getQuantity() + dto.getQuantity();
            existing.setQuantity(newQuantity);
            if (dto.getSelected() != null) {
                existing.setSelected(Boolean.TRUE.equals(dto.getSelected()) ? 1 : 0);
            }
            this.updateById(existing);
            return buildCartItemVO(existing);
        }

        SeedInfo seedInfo = seedInfoMapper.selectById(dto.getSeedId());
        if (seedInfo == null) {
            throw new BusinessException(ResultCode.SEED_NOT_EXIST);
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setSeedId(dto.getSeedId());
        cart.setQuantity(dto.getQuantity());
        cart.setSelected(Boolean.FALSE.equals(dto.getSelected()) ? 0 : 1);
        this.save(cart);
        return buildCartItemVO(cart, seedInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemVO updateQuantity(Long userId, Long cartId, Integer quantity) {
        Cart cart = getUserCart(userId, cartId);
        if (quantity == null || quantity <= 0) {
            this.removeById(cartId);
            return null;
        }
        cart.setQuantity(quantity);
        this.updateById(cart);
        return buildCartItemVO(cart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemVO updateSelected(Long userId, Long cartId, Boolean selected) {
        Cart cart = getUserCart(userId, cartId);
        cart.setSelected(Boolean.TRUE.equals(selected) ? 1 : 0);
        this.updateById(cart);
        return buildCartItemVO(cart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSelected(Long userId, CartBatchOperateDTO dto) {
        if (CollUtil.isEmpty(dto.getCartIds())) {
            return;
        }
        for (Long cartId : dto.getCartIds()) {
            Cart cart = getUserCart(userId, cartId);
            cart.setSelected(Boolean.TRUE.equals(dto.getSelected()) ? 1 : 0);
            this.updateById(cart);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeItem(Long userId, Long cartId) {
        getUserCart(userId, cartId);
        this.removeById(cartId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeItems(Long userId, List<Long> cartIds) {
        if (CollUtil.isEmpty(cartIds)) {
            return;
        }
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .in(Cart::getId, cartIds);
        this.remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        this.remove(wrapper);
    }

    private Cart getUserCart(Long userId, Long cartId) {
        Cart cart = this.getById(cartId);
        if (cart == null || !Objects.equals(cart.getUserId(), userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "购物车不存在");
        }
        return cart;
    }

    private CartItemVO buildCartItemVO(Cart cart) {
        SeedInfo seedInfo = seedInfoMapper.selectById(cart.getSeedId());
        return buildCartItemVO(cart, seedInfo);
    }

    private CartItemVO buildCartItemVO(Cart cart, SeedInfo seedInfo) {
        if (seedInfo == null) {
            throw new BusinessException(ResultCode.SEED_NOT_EXIST);
        }
        CartItemVO vo = new CartItemVO();
        vo.setCartId(cart.getId());
        vo.setSeedId(seedInfo.getId());
        vo.setSeedName(seedInfo.getSeedName());
        vo.setImageUrl(seedInfo.getImageUrl());
        vo.setUnitPrice(seedInfo.getUnitPrice());
        vo.setQuantity(cart.getQuantity());
        vo.setSelected(cart.getSelected() != null && cart.getSelected() == 1);
        vo.setTotalAmount(seedInfo.getUnitPrice().multiply(new BigDecimal(cart.getQuantity())));
        return vo;
    }
}
