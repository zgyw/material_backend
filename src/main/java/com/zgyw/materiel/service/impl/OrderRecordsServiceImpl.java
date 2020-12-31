package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.bean.OrderRecords;
import com.zgyw.materiel.repository.OrderRecordsRepository;
import com.zgyw.materiel.service.OrderRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderRecordsServiceImpl implements OrderRecordsService {
    @Autowired
    private OrderRecordsRepository repository;

    @Override
    public List<OrderRecords> findByType(Integer type) {
        return repository.findByStatusAndType(0,type);
    }

    @Override
    public Map<String,Object> pageList(Integer type, Integer status, String content, Pageable pageable) {
        Map map = new HashMap<>();
        List<OrderRecords> list = repository.findByStatusAndTypeAndName(status, type, content, pageable).getContent();
        Integer total = repository.countByStatusAndTypeAndName(status, type, content);
        map.put("orderRecords",list);
        map.put("total",total);
        return map;
    }

    @Override
    public OrderRecords saveOrder(String name, String remarks, Integer type) {
        OrderRecords orderRecords = new OrderRecords();
        orderRecords.setName(name);
        orderRecords.setRemarks(remarks);
        orderRecords.setType(type);
        orderRecords.setStatus(0);
        return repository.save(orderRecords);
    }
}
