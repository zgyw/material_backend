package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.repository.ClassifyRepository;
import com.zgyw.materiel.service.ClassifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Map<Integer, Classify> getClassify() {
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
}
