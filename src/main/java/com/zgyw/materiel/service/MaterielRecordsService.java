package com.zgyw.materiel.service;

import com.zgyw.materiel.VO.MaterielRecordsVO;
import com.zgyw.materiel.bean.MaterielRecords;

import java.util.List;

public interface MaterielRecordsService {

    List<MaterielRecords> putInOrder (Integer orderId,String materielIds,Integer type);

    List<MaterielRecordsVO> findCurOrder(Integer orderId, String content);

    MaterielRecordsVO detail (Integer id);

    void delete(Integer id);

    MaterielRecords modify(MaterielRecords records);

    List<MaterielRecords> changeInNum(String materielNums);

    List<MaterielRecords> changeOutNum(String materielNums);
}
