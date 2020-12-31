package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.bean.MaterielRecords;
import com.zgyw.materiel.bean.OrderRecords;
import com.zgyw.materiel.repository.MaterielRecordsRepository;
import com.zgyw.materiel.repository.OrderRecordsRepository;
import com.zgyw.materiel.service.OrderRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderRecordsServiceImpl implements OrderRecordsService {
    @Autowired
    private OrderRecordsRepository repository;
    @Autowired
    private MaterielRecordsRepository recordsRepository;

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

    @Override
    public OrderRecords detail(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        List<MaterielRecords> list = recordsRepository.findByOrderId(id);
        recordsRepository.deleteInBatch(list);
        repository.deleteById(id);
    }
}
