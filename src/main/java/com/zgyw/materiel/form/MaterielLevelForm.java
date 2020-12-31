package com.zgyw.materiel.form;

import lombok.Data;

@Data
public class MaterielLevelForm {

    private Integer id;

    /** 物料编码 */
    private String code;

    /** 分类id */
    private Integer classifyId;

    /** 封装 */
    private String potting;

    /** 库存数量 */
    private Integer quantity;

    /** 物料型号 */
    private String model;

    /** 订单编号 */
    private Integer orderId;

    /** 品牌 */
    private String brand;

    /** 供应商 */
    private String supplier;

    /** 网址 */
    private String website;

    /** 价格 */
    private Double price;

    /** 备注 */
    private String remarks;
}
