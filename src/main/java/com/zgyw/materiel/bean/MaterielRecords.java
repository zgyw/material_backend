package com.zgyw.materiel.bean;


import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "materiel_records")
@Data
public class MaterielRecords {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 商品编码 */
    private String code;

    /** 商品名称 */
    private String name;

    /** 商品型号 */
    private String model;

    /** 封装 */
    private String potting;

    /** 品牌 */
    private String brand;

    /** 单价 */
    private String price;

    /** 入库数量 */
    private Integer inNum;

    /** 出库数量 */
    private Integer outNum;

    /** 库存量 */
    private Integer quantity;

    /** 类型：1入库2出库*/
    private Integer type;

    /** 厂家型号 */
    private String factoryModel;

    /** 订单id */
    private Integer orderId;

    /** 描述(规格) */
    private String remarks;

    /** 供应商信息 */
    private String supplier;

    /** 备注描述 */
    private String note;

    public MaterielRecords() {
    }

    public MaterielRecords(String code, String name, String model, String potting, String brand, String price, Integer inNum, Integer outNum, Integer quantity, Integer type, String factoryModel, Integer orderId, String remarks, String supplier, String note) {
        this.code = code;
        this.name = name;
        this.model = model;
        this.potting = potting;
        this.brand = brand;
        this.price = price;
        this.inNum = inNum;
        this.outNum = outNum;
        this.quantity = quantity;
        this.type = type;
        this.factoryModel = factoryModel;
        this.orderId = orderId;
        this.remarks = remarks;
        this.supplier = supplier;
        this.note = note;
    }
}
