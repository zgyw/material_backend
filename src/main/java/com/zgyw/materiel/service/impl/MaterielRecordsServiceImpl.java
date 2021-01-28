package com.zgyw.materiel.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zgyw.materiel.VO.MaterielRecordsVO;
import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.bean.MaterielRecords;
import com.zgyw.materiel.form.RecordsForm;
import com.zgyw.materiel.repository.MaterielLevelRepository;
import com.zgyw.materiel.repository.MaterielRecordsRepository;
import com.zgyw.materiel.service.ClassifyService;
import com.zgyw.materiel.service.MaterielLevelService;
import com.zgyw.materiel.service.MaterielRecordsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaterielRecordsServiceImpl implements MaterielRecordsService {
    @Autowired
    private MaterielRecordsRepository repository;
    @Autowired
    private MaterielLevelRepository levelRepository;
    @Autowired
    private ClassifyService classifyService;
    @Autowired
    private MaterielLevelService levelService;

    @Override
    @Transactional
    public List<MaterielRecords> putInOrder(Integer orderId, String materielIds, Integer type) {
        if (StringUtils.isNotEmpty(materielIds)) {
            Map<Integer, Classify> classifyMap = classifyService.getClassifyIK();
            List<Integer> ids = Arrays.asList(materielIds.split(",")).stream().map(e -> Integer.parseInt(e.trim())).collect(Collectors.toList());
            List<MaterielLevel> materielLevels = levelRepository.findByIdIn(ids);
            List<MaterielRecords> materielRecords = new ArrayList<>();
            for (MaterielLevel materielLevel : materielLevels) {
                MaterielRecords records = repository.findByCodeAndOrderId(materielLevel.getCode(), orderId);
                if (records == null) {
                    records = new MaterielRecords();
                    if (type == 1) {
                        records.setInNum(materielLevel.getQuantity());
                    } else {
                        records.setOutNum(materielLevel.getQuantity());
                    }
                } else {
                    if (type == 1) {
                        records.setInNum(records.getInNum() + materielLevel.getQuantity());
                    } else {
                        records.setOutNum(records.getOutNum() + materielLevel.getQuantity());
                    }
                }
                Classify classify = classifyMap.get(materielLevel.getClassifyId());
                records.setCode(materielLevel.getCode());
                if (classify != null) {
                    records.setName(classify.getName());
                }
                records.setModel(materielLevel.getModel());
                records.setPotting(materielLevel.getPotting());
                records.setBrand(materielLevel.getBrand());
                records.setPrice(materielLevel.getPrice());
                records.setQuantity(materielLevel.getQuantity());
                records.setType(type);
                records.setOrderId(orderId);
                records.setFactoryModel(materielLevel.getFactoryModel());
                records.setRemarks(materielLevel.getRemarks());
                records.setSupplier(materielLevel.getSupplier());
                materielRecords.add(records);
            }
            return repository.saveAll(materielRecords);
        }
        return null;
    }

    @Override
    public List<MaterielRecordsVO> findCurOrder(Integer orderId, String content) {
        List<MaterielRecords> materielRecords = repository.findByCodeContainingAndOrderIdOrModelContainingAndOrderIdOrPottingContainingAndOrderId(content, orderId, content, orderId, content, orderId);
        List<MaterielRecordsVO> voList = new ArrayList<>();
        Map<String, MaterielLevel> levelMap = levelService.getMateriel();
        Map<Integer, Classify> classifyIK = classifyService.getClassifyIK();
        for (MaterielRecords materielRecord : materielRecords) {
            MaterielRecordsVO vo = new MaterielRecordsVO();
            MaterielLevel level = levelMap.get(materielRecord.getCode());
            BeanUtils.copyProperties(materielRecord, vo);
            vo.setQuantity(0);
            if (level != null) {
                BeanUtil.copyProperties(level, vo, true, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
                Classify classify = classifyIK.get(level.getClassifyId());
                vo.setId(materielRecord.getId());
                vo.setName(classify.getName());
                vo.setNote(materielRecord.getNote());
            }
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public MaterielRecordsVO detail(Integer id) {
        Map<String, MaterielLevel> levelMap = levelService.getMateriel();
        MaterielRecords materielRecord = repository.findById(id).orElse(null);
        MaterielRecordsVO vo = new MaterielRecordsVO();
        BeanUtils.copyProperties(materielRecord, vo);
        MaterielLevel level = levelMap.get(materielRecord.getCode());
        Map<Integer, Classify> classifyIK = classifyService.getClassifyIK();
        vo.setQuantity(0);
        if (level != null) {
            BeanUtil.copyProperties(level, vo, true, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
            Classify classify = classifyIK.get(level.getClassifyId());
            vo.setId(materielRecord.getId());
            vo.setNote(materielRecord.getNote());
            vo.setName(classify.getName());
        }
        return vo;
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public MaterielRecords modify(MaterielRecords records) {
        MaterielRecords materiel = repository.findById(records.getId()).orElse(null);
        BeanUtils.copyProperties(records, materiel);
        return repository.save(materiel);
    }

    @Override
    public List<MaterielRecords> changeInNum(String materielNums) {
        Gson gson = new Gson();
        List<RecordsForm> recordsFormList = gson.fromJson(materielNums, new TypeToken<List<RecordsForm>>() {
        }.getType());
        List<MaterielRecords> recordsList = new ArrayList<>();
        for (RecordsForm recordsForm : recordsFormList) {
            MaterielRecords materielRecords = repository.findById(recordsForm.getId()).orElse(null);
            materielRecords.setInNum(recordsForm.getInNum());
            recordsList.add(materielRecords);
        }
        return repository.saveAll(recordsList);
    }

    @Override
    public List<MaterielRecords> changeOutNum(String materielNums) {
        Gson gson = new Gson();
        List<RecordsForm> recordsFormList = gson.fromJson(materielNums, new TypeToken<List<RecordsForm>>() {
        }.getType());
        List<MaterielRecords> recordsList = new ArrayList<>();
        for (RecordsForm recordsForm : recordsFormList) {
            MaterielRecords materielRecords = repository.findById(recordsForm.getId()).orElse(null);
            materielRecords.setOutNum(recordsForm.getOutNum());
            recordsList.add(materielRecords);
        }
        return repository.saveAll(recordsList);
    }
}
