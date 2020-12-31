package com.zgyw.materiel.service;

import com.zgyw.materiel.bean.OrderRecords;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface OrderRecordsService {

    List<OrderRecords> findByType(Integer type);

    Map<String,Object> pageList(Integer type, Integer status, String content, Pageable pageable);

    OrderRecords saveOrder(String name,String remarks,Integer type);

    OrderRecords detail(Integer id);

    void delete(Integer id);
}
