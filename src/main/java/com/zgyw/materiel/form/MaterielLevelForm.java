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

    /** 供应商信息 */
    private String supplier;

    /** 备注 */
    private String note;

    /** 价格 */
    private String price;

    /** 描述(规格) */
    private String remarks;

    /** 厂家型号 */
    private String factoryModel;


}
