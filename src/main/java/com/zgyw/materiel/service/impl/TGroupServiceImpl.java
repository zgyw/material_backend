package com.zgyw.materiel.service.impl;


import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.bean.TGroup;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.repository.ClassifyRepository;
import com.zgyw.materiel.repository.TGroupRepository;
import com.zgyw.materiel.service.TGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TGroupServiceImpl implements TGroupService {
    @Autowired
    private TGroupRepository repository;
    @Autowired
    private ClassifyRepository classifyRepository;

    @Override
    @Transactional
    public TGroup add(TGroup tGroup) {
        TGroup group = repository.findByName(tGroup.getName());
        if (group != null) {
            throw new MTException(ResultEnum.NAME_EXIST);
        }
        return repository.save(tGroup);
    }

    @Override
    public List<TGroup> findAll() {
        return repository.findAll();
    }

    @Override
    public TGroup detail(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public TGroup modify(TGroup tGroup) {
        TGroup group = repository.findByName(tGroup.getName());
        if (group != null && group.getId() != tGroup.getId()) {
            throw new MTException(ResultEnum.NAME_EXIST);
        }
        return repository.save(tGroup);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public List<Classify> getClassify(Integer groupId) {
        return classifyRepository.findByGroupId(groupId);
    }
}
