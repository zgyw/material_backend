package com.zgyw.materiel.service;

import com.zgyw.materiel.bean.MaterielRecords;

import java.util.List;

public interface MaterielRecordsService {

    List<MaterielRecords> putInOrder (Integer orderId,String materielIds,Integer type);

    List<MaterielRecords> findCurOrder(Integer orderId);
}
