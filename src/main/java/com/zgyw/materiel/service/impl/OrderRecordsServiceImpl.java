package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.bean.MaterielRecords;
import com.zgyw.materiel.bean.OrderRecords;
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
        return repository.findByStatusAndType(0,type);
    }

    @Override
    public Map<String,Object> pageList(Integer type, Integer status, String content, Pageable pageable) {
        Map map = new HashMap<>();
        List<OrderRecords> list = repository.findByStatusAndTypeAndNameContaining(status, type, content, pageable).getContent();
        Integer total = repository.countByStatusAndTypeAndNameContaining(status, type, content);
        map.put("orderRecords",list);
        map.put("total",total);
        return map;
    }

    @Override
    @Transactional
    public OrderRecords saveOrder(OrderRecordsForm form) {
        OrderRecords orderRecords = new OrderRecords();
        BeanUtils.copyProperties(form,orderRecords);
        orderRecords.setStatus(0);
        return repository.save(orderRecords);
    }

    @Override
    @Transactional
    public OrderRecords modifyOrder(OrderRecordsForm form) {
        OrderRecords orderRecords = repository.findById(form.getId()).orElse(null);
        BeanUtils.copyProperties(form,orderRecords);
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
        ExportUtil.downLoadExcelToWebsite(workbook,response,"出库订单模板");
    }

    @Override
    @Transactional
    public void importMateriel(MultipartFile file) {
        List<List<Object>> dataList = ImportUtil.checkFile(file, "商品编码", 9);
        Map<String, Classify> classifyMap = classifyService.getClassifySK();
        Map<String, MaterielLevel> materielMap = levelService.getMateriel();
        List<String> codeList = new ArrayList<>();
        List<MaterielLevel> levelList = new ArrayList<>();
        List<MaterielRecords> recordsList = new ArrayList<>();
        // 导入文件相当于创建一个出库订单
        OrderRecords records = new OrderRecords();
        records.setName(file.getOriginalFilename());
        records.setRemarks("导入出库订单"+new Date());
        records.setStatus(0);
        records.setType(2);
        OrderRecords save = repository.save(records);
        int num = -1;
        for (int i = 0; i < dataList.size(); i++) {
            String code = (String)dataList.get(i).get(0);
            if (StringUtils.isEmpty(code)) {
                repository.deleteById(save.getId());
                throw new MTException("物料编码不能为空!出现在第"+(i+2)+"行",900);
            }
            if (codeList.contains(code)) {
                repository.deleteById(save.getId());
                throw new MTException("相同编码请合并在一起!出现在第"+(i+2)+"行",900);
            }
            codeList.add(code);
            String classifyName = (String)dataList.get(i).get(1);
            if (StringUtils.isEmpty(classifyName)) {
                repository.deleteById(save.getId());
                throw new MTException("分类不能为空!出现在第"+(i+2)+"行",900);
            }
            Classify classify = classifyMap.get(classifyName);
            if (classify == null) {
                repository.deleteById(save.getId());
                throw new MTException("分类系统中还不存在!出现在第"+(i+2)+"行",900);
            }
            String model = (String)dataList.get(i).get(2);
            String potting = (String)dataList.get(i).get(3);
            String brand = (String)dataList.get(i).get(4);
            String price = (String)dataList.get(i).get(5);
            String outNum = (String)dataList.get(i).get(6);
            String factoryModel = (String)dataList.get(i).get(7);
            String remarks = (String)dataList.get(i).get(8);
            if (StringUtils.isEmpty(outNum)) {
                repository.deleteById(save.getId());
                throw new MTException("出库数量不能为空!出现在第"+(i+2)+"行",900);
            }
            MaterielLevel level = materielMap.get(code);
            if (level == null) {
                repository.deleteById(save.getId());
                throw new MTException("库存里没有物料"+code+"!出现在第"+(i+2)+"行",900);
            }
            int totalQ = level.getQuantity() - Integer.parseInt(outNum);
            MaterielRecords materielRecord = null;
            if (totalQ < 0) {
                materielRecord = new MaterielRecords(code,classifyName,model,potting,brand,price,0,level.getQuantity(),totalQ,2,factoryModel,save.getId(),remarks);
                recordsList.add(materielRecord);
                num = i;
                continue;
            }
            if (num == (-1)) {
                level.setQuantity(totalQ);
                levelList.add(level);
            }
        }
        if (num > (-1)) {
            recordsRepository.saveAll(recordsList);
            throw new MTException("物料库存不足!出现在第"+(num+2)+"行",900);
        } else {
            save.setType(1);
            save.setOutTime(new Date());
            repository.save(save);
            levelRepository.saveAll(levelList);
        }
    }

    @Override
    @Transactional
    public List<MaterielLevel> putInWare(Integer orderId) {
        OrderRecords order = repository.findById(orderId).orElse(null);
        List<MaterielRecords> materielRecords = recordsRepository.findByOrderId(orderId);
        Map<String, Integer> mapList = new HashMap();
        Map<String, MaterielRecords> mapList2 = new HashMap<>();
        // 相同商品编码的入库数量先统计一起
        for (MaterielRecords materielRecord : materielRecords) {
            String code = materielRecord.getCode();
            Integer inNum = materielRecord.getInNum();
            Integer totalN = inNum;
            if (mapList.containsKey(code)) {
                Integer num = mapList.get(code);
                totalN = totalN+num;
            }
            mapList.put(code,totalN);
            mapList2.put(code,materielRecord);
        }
        Map<String, MaterielLevel> levelMap = levelService.getMateriel();
        Map<String, Classify> classifySK = classifyService.getClassifySK();
        List<MaterielLevel> levelList = new ArrayList<>();
        for (String code : mapList.keySet()) {
            MaterielLevel level = levelMap.get(code);
            if (level != null) {
                int totalQ = level.getQuantity()+mapList.get(code);
                level.setQuantity(totalQ);
            } else {
                MaterielRecords result = mapList2.get(code);
                Classify classify = classifySK.get(result.getName());
                level = new MaterielLevel(code,result.getModel(),result.getPotting(),mapList.get(code),result.getPrice(),result.getBrand(),"物料第一次进库存",classify == null?null:classify.getId(),result.getFactoryModel());
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
        Map<String, Integer> mapList = new HashMap();
        // 相同商品编码的出库数量先统计一起
        for (MaterielRecords materielRecord : materielRecords) {
            Integer inNum = materielRecord.getInNum();
            String code = materielRecord.getCode();
            Integer totalN = inNum;
            if (mapList.containsKey(code)) {
                Integer num = mapList.get(code);
                totalN = totalN+num;
            }
            mapList.put(code,totalN);
        }
        Map<String, MaterielLevel> levelMap = levelService.getMateriel();
        List<MaterielLevel> levelList = new ArrayList<>();
        for (String code : mapList.keySet()) {
            MaterielLevel level = levelMap.get(code);
            if (level != null) {
                int totalQ = level.getQuantity()-mapList.get(code);
                if (totalQ < 0) {
                    log.error("库存不足");
                    throw new MTException("物料"+code+"库存不足!",900);
                }
                level.setQuantity(totalQ);
            } else {
                log.error("库存中不存在这个物料");
                throw new MTException("库存里没有物料"+code+"!",900);
            }
            levelList.add(level);
        }
        order.setOutTime(new Date());
        order.setStatus(1);
        repository.save(order);
        return levelRepository.saveAll(levelList);
    }
}
