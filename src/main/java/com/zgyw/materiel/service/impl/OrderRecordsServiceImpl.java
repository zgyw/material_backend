package com.zgyw.materiel.service.impl;

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
    private OrderRecordsRepository orderRecordsRepository;
    @Autowired
    private MaterielLevelRepository levelRepository;

    @Override
    public List<OrderRecords> findByType(Integer type) {
        return repository.findByStatusAndType(0,type);
    }

    @Override
    public Map<String,Object> pageList(Integer type, Integer status, String content, Pageable pageable) {
        Map map = new HashMap<>();
        List<OrderRecords> list = repository.findByStatusAndTypeAndName(status, type, content, pageable).getContent();
        Integer total = repository.countByStatusAndTypeAndName(status, type, content);
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
    public void importMateriel(MultipartFile file) {
        List<List<Object>> dataList = ImportUtil.checkFile(file, "商品编码", 7);
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
        OrderRecords save = orderRecordsRepository.save(records);
        int num = -1;
        for (int i = 0; i < dataList.size(); i++) {
            String code = (String)dataList.get(i).get(0);
            if (StringUtils.isEmpty(code)) {
                orderRecordsRepository.deleteById(save.getId());
                throw new MTException("物料编码不能为空!出现在第"+(i+2)+"行",900);
            }
            if (codeList.contains(code)) {
                orderRecordsRepository.deleteById(save.getId());
                throw new MTException("相同编码请合并在一起!出现在第"+(i+2)+"行",900);
            }
            codeList.add(code);
            String classifyName = (String)dataList.get(i).get(1);
            if (StringUtils.isEmpty(classifyName)) {
                orderRecordsRepository.deleteById(save.getId());
                throw new MTException("分类不能为空!出现在第"+(i+2)+"行",900);
            }
            Classify classify = classifyMap.get(classifyName);
            if (classify == null) {
                orderRecordsRepository.deleteById(save.getId());
                throw new MTException("分类系统中还不存在!出现在第"+(i+2)+"行",900);
            }
            String model = (String)dataList.get(i).get(2);
            if (StringUtils.isEmpty(model)) {
                orderRecordsRepository.deleteById(save.getId());
                throw new MTException("型号不能为空!出现在第"+(i+2)+"行",900);
            }
            String potting = (String)dataList.get(i).get(3);
            if (StringUtils.isEmpty(potting)) {
                orderRecordsRepository.deleteById(save.getId());
                throw new MTException("封装不能为空!出现在第"+(i+2)+"行",900);
            }
            String brand = (String)dataList.get(i).get(4);
            String price = (String)dataList.get(i).get(5);
            String outNum = (String)dataList.get(i).get(6);
            if (StringUtils.isEmpty(outNum)) {
                orderRecordsRepository.deleteById(save.getId());
                throw new MTException("出库数量不能为空!出现在第"+(i+2)+"行",900);
            }
            MaterielLevel level = materielMap.get(code);
            if (level == null) {
                orderRecordsRepository.deleteById(save.getId());
                throw new MTException("库存里没有物料"+code+"!出现在第"+(i+2)+"行",900);
            }
            int totalQ = level.getQuantity() - Integer.parseInt(outNum);
            MaterielRecords materielRecord = null;
            if (totalQ < 0) {
                materielRecord = new MaterielRecords(code,classifyName,model,potting,brand,StringUtils.isEmpty(price)?null:Double.valueOf(price),0,level.getQuantity(),totalQ,2,save.getId());
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
            orderRecordsRepository.save(save);
            levelRepository.saveAll(levelList);
        }
    }

    @Override
    @Transactional
    public List<MaterielLevel> putInWare(Integer orderId) {
        return null;
    }

    @Override
    @Transactional
    public List<MaterielLevel> checkOutWare(Integer orderId) {
        return null;
    }
}
