package com.zgyw.materiel.repository;

import com.zgyw.materiel.bean.MaterielLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterielLevelRepository extends JpaRepository<MaterielLevel,Integer> {

    Page<MaterielLevel> findByClassifyIdInAndCodeContainingOrClassifyIdInAndModelContainingOrClassifyIdInAndPottingContaining(List<Integer> classifyId1, String code, List<Integer> classifyId2, String model, List<Integer> classifyId3, String potting, Pageable pageable);
    Integer countByClassifyIdInAndCodeContainingOrClassifyIdInAndModelContainingOrClassifyIdInAndPottingContaining(List<Integer> classifyId1,String code,List<Integer> classifyId2,String model,List<Integer> classifyId3,String potting);

    MaterielLevel findByCode(String code);

    List<MaterielLevel> findByIdIn(List<Integer> ids);

    List<MaterielLevel> findByClassifyId(Integer classifyId);

    List<MaterielLevel> findByClassifyIdIn(List<Integer> classifyId);
}
