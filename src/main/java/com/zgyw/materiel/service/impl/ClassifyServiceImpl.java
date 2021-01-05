package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.repository.ClassifyRepository;
import com.zgyw.materiel.service.ClassifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class ClassifyServiceImpl implements ClassifyService {

    @Autowired
    private ClassifyRepository repository;

    @Override
    public List<Classify> findAll() {
        return repository.findAll();
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
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
