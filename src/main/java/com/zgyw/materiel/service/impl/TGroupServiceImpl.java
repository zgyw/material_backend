package com.zgyw.materiel.service.impl;


import com.zgyw.materiel.bean.*;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.repository.*;
import com.zgyw.materiel.service.TGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TGroupServiceImpl implements TGroupService {
    @Autowired
    private TGroupRepository repository;
    @Autowired
    private ClassifyRepository classifyRepository;
    @Autowired
    private MaterielRecordsRepository recordsRepository;
    @Autowired
    private MaterielLevelRepository levelRepository;
    @Autowired
    private OrderRecordsRepository orderRepository;

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
        List<Classify> list = classifyRepository.findByGroupId(id);
        List<Integer> classifyIds = list.stream().map(e -> e.getId()).collect(Collectors.toList());
        List<String> names = list.stream().map(e -> e.getName()).collect(Collectors.toList());
        List<MaterielLevel> materielLevels = levelRepository.findByClassifyIdIn(classifyIds);
        List<OrderRecords> orderRecords = orderRepository.findByStatus(0);
        List<Integer> orderIds = orderRecords.stream().map(e -> e.getId()).collect(Collectors.toList());
        List<MaterielRecords> materielRecords = recordsRepository.findByOrderIdInAndNameIn(orderIds, names);
        if (materielLevels.size() > 0 || materielRecords.size() > 0) {
            throw new MTException(ResultEnum.CLASSIFY_EXIST);
        }
        classifyRepository.deleteInBatch(list);
        repository.deleteById(id);
    }

    @Override
    public List<Classify> getClassify(Integer groupId) {
        return classifyRepository.findByGroupId(groupId);
    }
}
