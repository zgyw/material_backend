package com.zgyw.materiel.repository;

import com.zgyw.materiel.bean.MaterielRecords;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterielRecordsRepository extends JpaRepository<MaterielRecords,Integer> {

    List<MaterielRecords> findByOrderId(Integer orderId);

    List<MaterielRecords> findByCode(String code);

    MaterielRecords findByCodeAndOrderId(String code,Integer orderId);

    Page<MaterielRecords> findByCodeContainingAndOrderIdOrModelContainingAndOrderIdOrPottingContainingAndOrderId(String code, Integer id1, String model, Integer id2, String potting, Integer id3, Pageable pageable);

    Integer countByCodeContainingAndOrderIdOrModelContainingAndOrderIdOrPottingContainingAndOrderId(String code, Integer id1, String model, Integer id2, String potting, Integer id3);
}
