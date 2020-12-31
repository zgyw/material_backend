package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.bean.MaterielRecords;
import com.zgyw.materiel.repository.MaterielLevelRepository;
import com.zgyw.materiel.repository.MaterielRecordsRepository;
import com.zgyw.materiel.service.ClassifyService;
import com.zgyw.materiel.service.MaterielRecordsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MaterielRecordsServiceImpl implements MaterielRecordsService {
    @Autowired
    private MaterielRecordsRepository repository;
    @Autowired
    private MaterielLevelRepository levelRepository;
    @Autowired
    private ClassifyService classifyService;
    
    @Override
    public List<MaterielRecords> putInOrder(Integer orderId, String materielIds,Integer type) {
        if (StringUtils.isNotEmpty(materielIds)) {
            Map<Integer, Classify> classifyMap = classifyService.getClassify();
            List<Integer> ids = Arrays.asList(materielIds.split(",")).stream().map(e -> Integer.parseInt(e.trim())).collect(Collectors.toList());
            List<MaterielLevel> materielLevels = levelRepository.findByIdIn(ids);
            List<MaterielRecords> materielRecords = new ArrayList<>();
            for (MaterielLevel materielLevel : materielLevels) {
                MaterielRecords records = new MaterielRecords();
                Classify classify = classifyMap.get(materielLevel.getClassifyId());
                records.setCode(materielLevel.getCode());
                if (classify != null) {
                    records.setName(classify.getName());
                }
                records.setModel(materielLevel.getModel());
                records.setPotting(materielLevel.getPotting());
                records.setBrand(materielLevel.getBrand());
                records.setPrice(materielLevel.getPrice());
                records.setInNum(materielLevel.getQuantity());
                records.setQuantity(materielLevel.getQuantity());
                records.setType(type);
                records.setOrderId(orderId);
                materielRecords.add(records);
            }
            return repository.saveAll(materielRecords);
        }
        return null;
    }

    @Override
    public List<MaterielRecords> findCurOrder(Integer orderId) {
        return repository.findByOrderId(orderId);
    }
}
