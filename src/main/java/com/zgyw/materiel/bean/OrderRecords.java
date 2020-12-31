package com.zgyw.materiel.bean;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "order_records")
@Data
public class OrderRecords {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 订单名称 */
    private String name;

    /** 订单描述 */
    private String remarks;

    /** 创建时间 */
    private Date createTime;

    /** 入库时间 */
    private Date inTime;

    /** 出库时间 */
    private Date outTime;

    /** 状态：0创建1完成 */
    private Integer status;

    /** 类型：1入库2出库 */
    private Integer type;
}
