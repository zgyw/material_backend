package com.zgyw.materiel.VO;

import lombok.Data;

@Data
public class MaterielLevelVO {

    private Integer id;

    /** 物料编码 */
    private String code;

    /** 商品名称 */
    private String name;

    /** 物料型号 */
    private String model;

    /** 封装 */
    private String potting;

    /** 品牌 */
    private String brand;

    /** 价格 */
    private String price;

    /** 库存数量 */
    private Integer quantity;

    /** 供应商信息 */
    private String supplier;

    /** 备注 */
    private String remarks;

    /** 厂家型号 */
    private String factoryModel;

    private String photo;

    private Integer classifyId;

    private Integer groupId;

    private String note;
}
