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
    private Double price;

    /** 入库数量 */
    private Integer inNum;

    /** 出库数量 */
    private Integer outNum;

    /** 库存量 */
    private Integer quantity;

    /** 类型：1入库2出库*/
    private Integer type;

    /** 订单id */
    private Integer orderId;

    public MaterielRecords() {
    }

    public MaterielRecords(String code, String name, String model, String potting, String brand, Double price, Integer inNum, Integer outNum, Integer quantity, Integer type, Integer orderId) {
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
        this.orderId = orderId;
    }
}
