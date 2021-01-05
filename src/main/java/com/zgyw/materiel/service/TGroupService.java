package com.zgyw.materiel.service;

import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.bean.TGroup;

import java.util.List;

public interface TGroupService {

    TGroup add (TGroup tGroup);

    List<TGroup> findAll();

    TGroup detail(Integer id);

    TGroup modify(TGroup tGroup);

    void delete(Integer id);

    List<Classify> getClassify(Integer groupId);
}
