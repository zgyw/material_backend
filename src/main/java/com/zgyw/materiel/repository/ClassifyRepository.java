package com.zgyw.materiel.repository;

import com.zgyw.materiel.bean.Classify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassifyRepository extends JpaRepository<Classify,Integer> {

    List<Classify> findByGroupId(Integer groupId);

    Classify findByName(String name);
}
