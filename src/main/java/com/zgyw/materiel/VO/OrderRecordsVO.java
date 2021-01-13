package com.zgyw.materiel.VO;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.zgyw.materiel.bean.MaterielRecords;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderRecordsVO {
    private Integer id;

    private String name;

    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date inTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date outTime;

    private Integer status;

    private Integer type;

    private List<MaterielRecords> materielRecords;
}
