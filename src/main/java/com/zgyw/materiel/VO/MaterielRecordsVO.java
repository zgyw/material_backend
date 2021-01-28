package com.zgyw.materiel.VO;


import lombok.Data;

@Data
public class MaterielRecordsVO {
    private Integer id;

    /**
     * 商品编码
     */
    private String code;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品型号
     */
    private String model;

    /**
     * 封装
     */
    private String potting;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 单价
     */
    private String price;

    /**
     * 入库数量
     */
    private Integer inNum;

    /**
     * 出库数量
     */
    private Integer outNum;

    /**
     * 库存量
     */
    private Integer quantity;

    /**
     * 类型：1入库2出库
     */
    private Integer type;

    /**
     * 厂家型号
     */
    private String factoryModel;

    /**
     * 订单id
     */
    private Integer orderId;

    /**
     * 描述(规格)
     */
    private String remarks;

    /**
     * 供应商信息
     */
    private String supplier;

    /**
     * 备注描述
     */
    private String note;

    /**
     * 图片
     */
    private String photo;
}
