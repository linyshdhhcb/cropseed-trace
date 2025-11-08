package com.linyi.cropseed.trace.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 购物车更新 DTO
 */
@Data
public class CartUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer quantity;

    private Boolean selected;
}
