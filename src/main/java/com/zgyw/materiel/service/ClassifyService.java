package com.zgyw.materiel.service;

import com.zgyw.materiel.bean.Classify;

import java.util.List;
import java.util.Map;

public interface ClassifyService {

    List<Classify> findAll ();

    Map<Integer, Classify> getClassify();
}
