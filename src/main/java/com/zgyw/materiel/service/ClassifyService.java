package com.zgyw.materiel.service;

import com.zgyw.materiel.VO.ClassifyVO;
import com.zgyw.materiel.bean.Classify;

import java.util.List;
import java.util.Map;

public interface ClassifyService {

    List<ClassifyVO> findAll ();

    Map<Integer, Classify> getClassifyIK();

    Map<String, Classify> getClassifySK();

    Classify add(Classify classify);

    Classify modify(Classify classify);

    Classify detail(Integer id);

    void delete(Integer id);
}
