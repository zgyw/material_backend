package com.zgyw.materiel.repository;

import com.zgyw.materiel.bean.OrderRecords;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRecordsRepository extends JpaRepository<OrderRecords,Integer> {

    List<OrderRecords> findByStatusAndType(Integer status,Integer type);

    Page<OrderRecords> findByStatusAndTypeAndNameContaining(Integer status, Integer type, String name, Pageable pageable);

    Integer countByStatusAndTypeAndNameContaining(Integer status, Integer type, String name);

    OrderRecords findByTypeAndName(Integer type,String name);

}
