package com.zgyw.materiel.bean;


import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "t_user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String loginName;

    private String realName;

    private String passWord;

    private Integer roleId;
}
