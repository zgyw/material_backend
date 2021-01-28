package com.zgyw.materiel.VO;


import lombok.Data;

import java.util.List;

@Data
public class ClassifyVO {

    private Integer id;

    private String name;

    private List children;
}
