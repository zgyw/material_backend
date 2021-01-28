package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.VO.ClassifyVO;
import com.zgyw.materiel.bean.*;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.repository.*;
import com.zgyw.materiel.service.ClassifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class ClassifyServiceImpl implements ClassifyService {

    @Autowired
    private ClassifyRepository repository;
    @Autowired
    private TGroupRepository groupRepository;
    @Autowired
    private MaterielLevelRepository levelRepository;
    @Autowired
    private MaterielRecordsRepository recordsRepository;
    @Autowired
    private OrderRecordsRepository orderRepository;

    @Override
    public List<ClassifyVO> findAll() {
        List<ClassifyVO> voList = new ArrayList<>();
        List<TGroup> tGroups = groupRepository.findAll();
        for (TGroup tGroup : tGroups) {
            ClassifyVO vo = new ClassifyVO();
            List<Classify> classifyList = repository.findByGroupId(tGroup.getId());
            vo.setId(tGroup.getId());
            vo.setName(tGroup.getName());
            vo.setChildren(classifyList);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public Map<Integer, Classify> getClassifyIK() {
        List<Classify> classifyList = repository.findAll();
        Map<Integer, Classify> classifyMap =
                classifyList.stream().collect(Collectors.toMap(new Function<Classify, Integer>() {
                    @Override
                    public Integer apply(Classify classify) {
                        return classify.getId();
                    }
                }, Function.identity(), (key1, key2) -> key1));
        return classifyMap;
    }

    @Override
    public Map<String, Classify> getClassifySK() {
        List<Classify> classifyList = repository.findAll();
        Map<String, Classify> classifyMap =
                classifyList.stream().collect(Collectors.toMap(new Function<Classify, String>() {
                    @Override
                    public String apply(Classify classify) {
                        return classify.getName();
                    }
                }, Function.identity(), (key1, key2) -> key1));
        return classifyMap;
    }

    @Override
    @Transactional
    public Classify add(Classify classify) {
        Classify result = repository.findByName(classify.getName());
        if (result != null) {
            throw new MTException(ResultEnum.NAME_EXIST);
        }
        return repository.save(classify);
    }

    @Override
    @Transactional
    public Classify modify(Classify classify) {
        Classify result = repository.findByName(classify.getName());
        if (result != null && result.getId() != classify.getId()) {
            throw new MTException(ResultEnum.NAME_EXIST);
        }
        return repository.save(classify);
    }

    @Override
    public Classify detail(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        List<MaterielLevel> levels = levelRepository.findByClassifyId(id);
        List<OrderRecords> orderRecords = orderRepository.findByStatus(0);
        List<Integer> orderIds = orderRecords.stream().map(e -> e.getId()).collect(Collectors.toList());
        Classify classify = repository.findById(id).orElse(null);
        List<MaterielRecords> materielRecords = recordsRepository.findByOrderIdInAndName(orderIds, classify.getName());
        if (levels.size() > 0 || materielRecords.size() > 0) {
            throw new MTException(ResultEnum.CLASSIFY_EXIST);
        }
        repository.delete(classify);
    }
}
