package com.zgyw.materiel.repository;

import com.zgyw.materiel.bean.MaterielLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterielLevelRepository extends JpaRepository<MaterielLevel,Integer> {

    Page<MaterielLevel> findByClassifyIdAndCodeOrClassifyIdAndModelOrClassifyIdAndPotting(Integer classifyId1, String code, Integer classifyId2, String model, Integer classifyId3, String potting, Pageable pageable);
    Integer countByClassifyIdAndCodeOrClassifyIdAndModelOrClassifyIdAndPotting(Integer classifyId1,String code,Integer classifyId2,String model,Integer classifyId3,String potting);

    Page<MaterielLevel> findByCodeOrModelOrPotting(String code,String model,String potting,Pageable pageable);
    Integer countByCodeOrModelOrPotting(String code,String model,String potting);

    MaterielLevel findByCode(String code);

    List<MaterielLevel> findByIdIn(List<Integer> ids);
}
