package com.zgyw.materiel.bean;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "materiel_level")
@Data
public class MaterielLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 物料编码 */
    private String code;

    /** 物料型号 */
    private String model;

    /** 封装 */
    private String potting;

    /** 库存数量 */
    private Integer quantity;

    /** 价格 */
    private String price;

    /** 品牌 */
    private String brand;

    /** 供应商信息 */
    private String supplier;

    /** 描述规格 */
    private String remarks;

    /** 图片 */
    private String photo;

    /** 厂家型号 */
    private String factoryModel;

    /** 分类id */
    private Integer classifyId;

    /** 备注描述 */
    private String note;

    public MaterielLevel() {
    }

    public MaterielLevel(String code, String model, String potting, Integer quantity, String price, String brand, String supplier, String remarks, Integer classifyId, String factoryModel, String note) {
        this.code = code;
        this.model = model;
        this.potting = potting;
        this.quantity = quantity;
        this.price = price;
        this.brand = brand;
        this.supplier = supplier;
        this.remarks = remarks;
        this.classifyId = classifyId;
        this.factoryModel = factoryModel;
        this.note = note;
    }

    public MaterielLevel(String code, String model, String potting, Integer quantity, String price, String brand, String remarks, Integer classifyId, String factoryModel, String note) {
        this.code = code;
        this.model = model;
        this.potting = potting;
        this.quantity = quantity;
        this.price = price;
        this.brand = brand;
        this.remarks = remarks;
        this.classifyId = classifyId;
        this.factoryModel = factoryModel;
        this.note = note;
    }
}
