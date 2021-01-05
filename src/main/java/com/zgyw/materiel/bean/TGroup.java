package com.zgyw.materiel.bean;


import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "t_group")
@Data
public class TGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
}
