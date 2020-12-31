package com.zgyw.materiel.repository;

import com.zgyw.materiel.bean.MaterielRecords;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterielRecordsRepository extends JpaRepository<MaterielRecords,Integer> {

    List<MaterielRecords> findByOrderId(Integer orderId);
}
