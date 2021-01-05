package com.zgyw.materiel.service;

import com.zgyw.materiel.bean.MaterielRecords;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface MaterielRecordsService {

    List<MaterielRecords> putInOrder (Integer orderId,String materielIds,Integer type);

    Map<String,Object> findCurOrder(Integer orderId, String content, Pageable pageable);

    MaterielRecords detail (Integer id);

    void delete(Integer id);

    MaterielRecords modify(MaterielRecords records);
}
