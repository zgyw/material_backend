package com.zgyw.materiel.form;


import lombok.Data;

@Data
public class OrderRecordsForm {

    private Integer id;

    /** 订单名称 */
    private String name;

    /** 订单描述 */
    private String remarks;

    /** 类型：1入库2出库 */
    private Integer type;
}
