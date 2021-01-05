package com.zgyw.materiel.repository;

import com.zgyw.materiel.bean.TGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TGroupRepository extends JpaRepository<TGroup,Integer> {

    TGroup findByName(String name);
}
