package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.VO.OrderRecordsVO;
import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.bean.MaterielRecords;
import com.zgyw.materiel.bean.OrderRecords;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.form.OrderRecordsForm;
import com.zgyw.materiel.repository.MaterielLevelRepository;
import com.zgyw.materiel.repository.MaterielRecordsRepository;
import com.zgyw.materiel.repository.OrderRecordsRepository;
import com.zgyw.materiel.service.ClassifyService;
import com.zgyw.materiel.service.MaterielLevelService;
import com.zgyw.materiel.service.OrderRecordsService;
import com.zgyw.materiel.util.ExportUtil;
import com.zgyw.materiel.util.ImportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
public class OrderRecordsServiceImpl implements OrderRecordsService {
    @Autowired
    private OrderRecordsRepository repository;
    @Autowired
    private MaterielRecordsRepository recordsRepository;
    @Autowired
    private ClassifyService classifyService;
    @Autowired
    private MaterielLevelService levelService;
    @Autowired
    private MaterielLevelRepository levelRepository;

    @Override
    public List<OrderRecords> findByType(Integer type) {
        return repository.findByStatusAndType(0, type);
    }

    @Override
    public Map<String, Object> pageList(Integer type, Integer status, String content, Pageable pageable) {
        Map map = new HashMap<>();
        List<OrderRecords> list = repository.findByStatusAndTypeAndNameContaining(status, type, content, pageable).getContent();
        Integer total = repository.countByStatusAndTypeAndNameContaining(status, type, content);
        List<OrderRecordsVO> voList = new ArrayList<>();
        for (OrderRecords orderRecords : list) {
            List<MaterielRecords> recordsList = recordsRepository.findByOrderId(orderRecords.getId());
            List<MaterielRecords> recordsVOS = new ArrayList<>();
            for (MaterielRecords materielRecord : recordsList) {
                MaterielLevel level = levelRepository.findByCode(materielRecord.getCode());
                materielRecord.setPotting(level.getPotting());
                materielRecord.setModel(level.getModel());
                materielRecord.setBrand(level.getBrand());
                materielRecord.setFactoryModel(level.getFactoryModel());
                materielRecord.setPrice(level.getPrice());
                materielRecord.setSupplier(level.getSupplier());
                materielRecord.setRemarks(level.getRemarks());
                recordsVOS.add(materielRecord);
            }
            OrderRecordsVO orderRecordsVO = new OrderRecordsVO();
            BeanUtils.copyProperties(orderRecords, orderRecordsVO);
            orderRecordsVO.setMaterielRecords(recordsVOS);
            voList.add(orderRecordsVO);
        }
        map.put("orderRecords", voList);
        map.put("total", total);
        return map;
    }

    @Override
    @Transactional
    public OrderRecords saveOrder(OrderRecordsForm form) {
        OrderRecords records = repository.findByTypeAndName(form.getType(), form.getName());
        if (records != null) {
            throw new MTException(ResultEnum.NAME_EXIST);
        }
        OrderRecords orderRecords = new OrderRecords();
        BeanUtils.copyProperties(form, orderRecords);
        orderRecords.setStatus(0);
        return repository.save(orderRecords);
    }

    @Override
    @Transactional
    public OrderRecords modifyOrder(OrderRecordsForm form) {
        OrderRecords records = repository.findByTypeAndName(form.getType(), form.getName());
        if (records != null && records.getId() != form.getId()) {
            throw new MTException(ResultEnum.NAME_EXIST);
        }
        OrderRecords orderRecords = repository.findById(form.getId()).orElse(null);
        BeanUtils.copyProperties(form, orderRecords);
        return repository.save(orderRecords);
    }

    @Override
    public OrderRecords detail(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        List<MaterielRecords> list = recordsRepository.findByOrderId(id);
        recordsRepository.deleteInBatch(list);
        repository.deleteById(id);
    }

    @Override
    public void exportTemplate(HttpServletResponse response) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("template/checkout.xlsx");
        SXSSFWorkbook workbook = ExportUtil.getWorkbook(0, inputStream);
        ExportUtil.downLoadExcelToWebsite(workbook, response, "??????????????????");
    }

    @Override
    public void importMateriel(MultipartFile file) {
        List<List<Object>> dataList = ImportUtil.checkFile(file, "????????????", 11);
        String fileName = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));
        OrderRecords orderRecords = repository.findByTypeAndName(2, fileName);
        if (orderRecords != null) {
            throw new MTException(ResultEnum.NAME_EXIST);
        }
        Map<String, MaterielLevel> materielMap = levelService.getMateriel();
        Map<String, Classify> classifyMap = classifyService.getClassifySK();
        List<String> codeList = new ArrayList<>();
        List<MaterielLevel> levelList = new ArrayList<>();
        // ?????????????????????????????????????????????
        OrderRecords records = new OrderRecords();
        records.setName(fileName);
        records.setRemarks("??????????????????");
        records.setStatus(0);
        records.setType(2);
        OrderRecords save = repository.save(records);
        List<MaterielRecords> recordsList = new ArrayList<>();
        Integer num = -2;
        for (int i = 0; i < dataList.size(); i++) {
            String code = (String) dataList.get(i).get(0);
            if (StringUtils.isEmpty(code)) {
                repository.deleteById(save.getId());
                throw new MTException("????????????????????????!????????????" + (i + 2) + "???", 900);
            }
            MaterielLevel level = materielMap.get(code.trim());
            if (level == null) {
                // ????????????????????????????????????????????????????????????????????????????????????????????????
                num = i;
            }
            if (codeList.contains(code)) {
                repository.deleteById(save.getId());
                throw new MTException("??????????????????????????????!????????????" + (i + 2) + "???", 900);
            }
            codeList.add(code);
            if (dataList.get(i).size() < 2) {
                repository.deleteById(save.getId());
                throw new MTException("??????????????????!????????????" + (i + 2) + "???", 900);
            }
            String classifyName = (String) dataList.get(i).get(1);
            if (StringUtils.isEmpty(classifyName)) {
                repository.deleteById(save.getId());
                throw new MTException("??????????????????!????????????" + (i + 2) + "???", 900);
            }
            Classify classify = classifyMap.get(classifyName.trim());
            if (classify == null) {
                repository.deleteById(save.getId());
                throw new MTException("???????????????????????????!????????????" + (i + 2) + "???", 900);
            }
            if (dataList.get(i).size() < 10) {
                repository.deleteById(save.getId());
                throw new MTException("????????????????????????!????????????" + (i + 2) + "???", 900);
            }
            String outNum = (String) dataList.get(i).get(9);
            if (StringUtils.isEmpty(outNum)) {
                repository.deleteById(save.getId());
                throw new MTException("????????????????????????!????????????" + (i + 2) + "???", 900);
            }
            if (num < 0) {
                int totalQ = level.getQuantity() - Integer.parseInt(outNum);
                if (totalQ < 0) {
                    // ?????????????????????????????????????????????????????????????????????????????????????????????
                    saveByError(dataList, level.getQuantity(), save.getId());
                    throw new MTException("??????????????????,????????????!!!", 900);
                }
                MaterielRecords record = getRecord(dataList, i, totalQ, save.getId());
                recordsList.add(record);
                level.setQuantity(totalQ);
                level.setNote(record.getNote());
                levelList.add(level);
            }
        }
        if (num >= 0) {
            saveByError(dataList, 0, save.getId());
            throw new MTException("???????????????????????????????????????!", 900);
        }
        save.setStatus(1);
        save.setOutTime(new Date());
        repository.save(save);
        levelRepository.saveAll(levelList);
        recordsRepository.saveAll(recordsList);
    }

    @Override
    @Transactional
    public List<MaterielLevel> putInWare(Integer orderId) {
        OrderRecords order = repository.findById(orderId).orElse(null);
        List<MaterielRecords> materielRecords = recordsRepository.findByOrderId(orderId);
        Map<String, MaterielLevel> levelMap = levelService.getMateriel();
        Map<String, Classify> classifySK = classifyService.getClassifySK();
        List<MaterielLevel> levelList = new ArrayList<>();
        for (MaterielRecords materielRecord : materielRecords) {
            MaterielLevel level = levelMap.get(materielRecord.getCode());
            Classify classify = classifySK.get(materielRecord.getName());
            if (level != null) {
                int totalQ = level.getQuantity() + materielRecord.getInNum();
                level.setQuantity(totalQ);
                level.setNote(materielRecord.getNote());
                level.setClassifyId(classify.getId());
                level.setRemarks(materielRecord.getRemarks());
                level.setSupplier(materielRecord.getSupplier());
                level.setPotting(materielRecord.getPotting());
                level.setModel(materielRecord.getModel());
                level.setBrand(materielRecord.getBrand());
                level.setFactoryModel(materielRecord.getFactoryModel());
                level.setPrice(materielRecord.getPrice());
            } else {
                level =
                        new MaterielLevel(materielRecord.getCode(), materielRecord.getModel(), materielRecord.getPotting(), materielRecord.getInNum(), materielRecord.getPrice(), materielRecord.getBrand(), materielRecord.getSupplier(), materielRecord.getRemarks(), classify == null ? null : classify.getId(), materielRecord.getFactoryModel(), materielRecord.getNote());
            }
            levelList.add(level);
        }
        order.setInTime(new Date());
        order.setStatus(1);
        repository.save(order);
        return levelRepository.saveAll(levelList);
    }

    @Override
    @Transactional
    public List<MaterielLevel> checkOutWare(Integer orderId) {
        OrderRecords order = repository.findById(orderId).orElse(null);
        List<MaterielRecords> materielRecords = recordsRepository.findByOrderId(orderId);
        Map<String, MaterielLevel> levelMap = levelService.getMateriel();
        List<MaterielLevel> levelList = new ArrayList<>();
        for (MaterielRecords materielRecord : materielRecords) {
            MaterielLevel level = levelMap.get(materielRecord.getCode());
            if (level != null) {
                int totalQ = level.getQuantity() - materielRecord.getOutNum();
                if (totalQ < 0) {
                    log.error("????????????");
                    throw new MTException("??????????????????,????????????!!!", 900);
                }
                level.setQuantity(totalQ);
                levelList.add(level);
            } else {
                log.error("??????????????????????????????");
                throw new MTException("?????????????????????" + materielRecord.getCode() + "!", 901);
            }
        }
        order.setOutTime(new Date());
        order.setStatus(1);
        repository.save(order);
        return levelRepository.saveAll(levelList);
    }

    public void saveByError(List<List<Object>> dataList, Integer totalQ, Integer orderId) {
        List<MaterielRecords> recordsList = new ArrayList<>();
        Map<String, MaterielLevel> materielMap = levelService.getMateriel();
        for (int i = 0; i < dataList.size(); i++) {
//            MaterielLevel materielLevel = materielMap.get(dataList.get(i).get(0));
//            if (materielLevel != null) {
//                totalQ = materielLevel.getQuantity();
//            }
            MaterielRecords materielRecords = getRecord(dataList, i, totalQ, orderId);
            recordsList.add(materielRecords);
        }
        recordsRepository.saveAll(recordsList);
    }

    public static MaterielRecords getRecord(List<List<Object>> dataList, Integer i, Integer totalQ, Integer orderId) {
        String code = (String) dataList.get(i).get(0);
        String classifyName = (String) dataList.get(i).get(1);
        String potting = (String) dataList.get(i).get(2);
        String model = (String) dataList.get(i).get(3);
        String brand = (String) dataList.get(i).get(4);
        String factoryModel = (String) dataList.get(i).get(5);
        String price = (String) dataList.get(i).get(6);
        String supplier = (String) dataList.get(i).get(7);
        String remarks = (String) dataList.get(i).get(8);
        String outNum = (String) dataList.get(i).get(9);
        String desc;
        try {
            desc = (String) dataList.get(i).get(10);
        } catch (Exception e) {
            desc = null;
        }
        MaterielRecords materielRecord = new MaterielRecords(code, classifyName, model, potting, brand, price, 0, Integer.parseInt(outNum), totalQ, 2, factoryModel, orderId, remarks, supplier, desc);
        return materielRecord;
    }
}
