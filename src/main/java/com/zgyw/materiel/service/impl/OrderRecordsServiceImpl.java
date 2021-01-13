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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            OrderRecordsVO orderRecordsVO = new OrderRecordsVO();
            BeanUtils.copyProperties(orderRecords,orderRecordsVO);
            orderRecordsVO.setMaterielRecords(recordsList);
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
        ExportUtil.downLoadExcelToWebsite(workbook, response, "出库订单模板");
    }

    @Override
    public void importMateriel(MultipartFile file) {
        List<List<Object>> dataList = ImportUtil.checkFile(file, "商品编码", 9);
        String fileName = file.getOriginalFilename().substring(0,file.getOriginalFilename().lastIndexOf("."));
        OrderRecords orderRecords = repository.findByTypeAndName(2, fileName);
        if (orderRecords != null) {
            throw new MTException(ResultEnum.NAME_EXIST);
        }
        Map<String, Classify> classifyMap = classifyService.getClassifySK();
        Map<String, MaterielLevel> materielMap = levelService.getMateriel();
        List<MaterielRecords> list = recordsRepository.findAll();
        Map<String, MaterielRecords> recordsMap =
                list.stream().collect(Collectors.toMap(new Function<MaterielRecords, String>() {
                    @Override
                    public String apply(MaterielRecords records) {
                        return records.getCode();
                    }
                }, Function.identity(), (key1, key2) -> key1));
        List<String> codeList = new ArrayList<>();
        List<MaterielLevel> levelList = new ArrayList<>();
        // 导入文件相当于创建一个出库订单
        OrderRecords records = new OrderRecords();
        records.setName(fileName);
        records.setRemarks("导入出库订单");
        records.setStatus(0);
        records.setType(2);
        OrderRecords save = repository.save(records);
        List<MaterielRecords> recordsList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            String code = (String) dataList.get(i).get(0);
            if (StringUtils.isEmpty(code)) {
                repository.deleteById(save.getId());
                throw new MTException("物料编码不能为空!出现在第" + (i + 2) + "行", 900);
            }
            if (codeList.contains(code)) {
                repository.deleteById(save.getId());
                throw new MTException("相同编码请合并在一起!出现在第" + (i + 2) + "行", 900);
            }
            codeList.add(code);
            String classifyName = (String) dataList.get(i).get(1);
            if (StringUtils.isEmpty(classifyName)) {
                repository.deleteById(save.getId());
                throw new MTException("分类不能为空!出现在第" + (i + 2) + "行", 900);
            }
            Classify classify = classifyMap.get(classifyName);
            if (classify == null) {
                repository.deleteById(save.getId());
                throw new MTException("分类系统中还不存在!出现在第" + (i + 2) + "行", 900);
            }
            String outNum = (String) dataList.get(i).get(6);
            if (StringUtils.isEmpty(outNum)) {
                repository.deleteById(save.getId());
                throw new MTException("出库数量不能为空!出现在第" + (i + 2) + "行", 900);
            }
            MaterielLevel level = materielMap.get(code);
            if (level == null) {
                repository.deleteById(save.getId());
                throw new MTException("库存里没有物料" + code + "!出现在第" + (i + 2) + "行", 900);
            }
            int totalQ = level.getQuantity() - Integer.parseInt(outNum);
            if (totalQ < 0) {
                // 当有一个物料库存不足时，该订单变成未出库订单，所有物料不能出库
                saveByError(dataList,level.getQuantity(),save.getId());
                throw new MTException("物料库存不足,不能出库!!!", 900);
            }
            MaterielRecords materielRecords = recordsMap.get(code);
            materielRecords.setQuantity(totalQ);
            recordsList.add(materielRecords);
            level.setQuantity(totalQ);
            levelList.add(level);
        }
        save.setType(1);
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
        List<MaterielRecords> recordsList = new ArrayList<>();
        for (MaterielRecords materielRecord : materielRecords) {
            MaterielLevel level = levelMap.get(materielRecord.getCode());
            if (level != null) {
                int totalQ = level.getQuantity() + materielRecord.getInNum();
                level.setQuantity(totalQ);
                materielRecord.setQuantity(totalQ);
            } else {
                Classify classify = classifySK.get(materielRecord.getName());
                level =
                new MaterielLevel(materielRecord.getCode(),materielRecord.getModel(),materielRecord.getPotting(),materielRecord.getInNum(),materielRecord.getPrice(),materielRecord.getBrand(),materielRecord.getSupplier(),materielRecord.getWebsite(),materielRecord.getRemarks(),classify == null?null:classify.getId(),materielRecord.getFactoryModel());
                materielRecord.setQuantity(materielRecord.getInNum());
            }
            levelList.add(level);
            recordsList.add(materielRecord);
        }
        order.setInTime(new Date());
        order.setStatus(1);
        repository.save(order);
        recordsRepository.saveAll(recordsList);
        return levelRepository.saveAll(levelList);
    }

    @Override
    @Transactional
    public List<MaterielLevel> checkOutWare(Integer orderId) {
        OrderRecords order = repository.findById(orderId).orElse(null);
        List<MaterielRecords> materielRecords = recordsRepository.findByOrderId(orderId);
        Map<String, MaterielLevel> levelMap = levelService.getMateriel();
        List<MaterielRecords> recordsList = new ArrayList<>();
        List<MaterielLevel> levelList = new ArrayList<>();
        for (MaterielRecords materielRecord : materielRecords) {
            MaterielLevel level = levelMap.get(materielRecord.getCode());
            if (level != null) {
                int totalQ = level.getQuantity() - materielRecord.getOutNum();
                if (totalQ < 0) {
                    log.error("库存不足");
                    throw new MTException("物料库存不足,不予出库!!!", 900);
                }
                level.setQuantity(totalQ);
                materielRecord.setQuantity(totalQ);
                recordsList.add(materielRecord);
            } else {
                log.error("库存中不存在这个物料");
                throw new MTException("库存里没有物料" + materielRecord.getCode() + "!", 901);
            }
            levelList.add(level);
        }
        order.setOutTime(new Date());
        order.setStatus(1);
        repository.save(order);
        recordsRepository.saveAll(recordsList);
        return levelRepository.saveAll(levelList);
    }

    public void saveByError(List<List<Object>> dataList,Integer totalQ,Integer orderId) {
        List<MaterielRecords> recordsList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            String code = (String) dataList.get(i).get(0);
            String classifyName = (String) dataList.get(i).get(1);
            String model = (String) dataList.get(i).get(2);
            String potting = (String) dataList.get(i).get(3);
            String brand = (String) dataList.get(i).get(4);
            String price = (String) dataList.get(i).get(5);
            String outNum = (String) dataList.get(i).get(6);
            String factoryModel = (String) dataList.get(i).get(7);
            String remarks = (String) dataList.get(i).get(8);
            MaterielRecords materielRecord = new MaterielRecords(code, classifyName, model, potting, brand, price, 0,Integer.parseInt(outNum), totalQ, 2, factoryModel, orderId, remarks,null,null);
            recordsList.add(materielRecord);
        }
        recordsRepository.saveAll(recordsList);
    }
}
