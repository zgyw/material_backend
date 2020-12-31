package com.zgyw.materiel.service.impl;

import com.zgyw.materiel.VO.MaterielLevelVO;
import com.zgyw.materiel.bean.Classify;
import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.bean.MaterielRecords;
import com.zgyw.materiel.bean.OrderRecords;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.form.MaterielLevelForm;
import com.zgyw.materiel.repository.ClassifyRepository;
import com.zgyw.materiel.repository.MaterielLevelRepository;
import com.zgyw.materiel.repository.MaterielRecordsRepository;
import com.zgyw.materiel.repository.OrderRecordsRepository;
import com.zgyw.materiel.service.ClassifyService;
import com.zgyw.materiel.service.FileSystemService;
import com.zgyw.materiel.service.MaterielLevelService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MaterielLevelServiceImpl implements MaterielLevelService {
    @Autowired
    private MaterielLevelRepository repository;
    @Autowired
    private ClassifyRepository classifyRepository;
    @Autowired
    private FileSystemService fileSystemService;
    @Autowired
    private MaterielRecordsRepository recordsRepository;
    @Autowired
    private OrderRecordsRepository orderRecordsRepository;
    @Autowired
    private ClassifyService classifyService;

    @Override
    public Map<String, Object> pageList(Integer classifyId, String content, Pageable pageable) {
        Map<String,Object> map = new HashMap<>();
        List<MaterielLevel> materielLevels;
        Integer total;
        Map<Integer, Classify> classifyMap = classifyService.getClassify();
        if (classifyId != null) {
            materielLevels = repository.findByClassifyIdAndCodeOrClassifyIdAndModelOrClassifyIdAndPotting(classifyId, content, classifyId, content, classifyId, content, pageable).getContent();
            total = repository.countByClassifyIdAndCodeOrClassifyIdAndModelOrClassifyIdAndPotting(classifyId,content,classifyId,content,classifyId,content);
        } else {
            materielLevels = repository.findByCodeOrModelOrPotting(content,content,content,pageable).getContent();
            total = repository.countByCodeOrModelOrPotting(content,content,content);
        }
        List<MaterielLevelVO> voList = new ArrayList<>();
        for (MaterielLevel materielLevel : materielLevels) {
            MaterielLevelVO levelVO = new MaterielLevelVO();
            BeanUtils.copyProperties(materielLevel,levelVO);
            Classify classify = classifyMap.get(materielLevel.getClassifyId());
            if (classify != null) {
                levelVO.setName(classify.getName());
            }
            voList.add(levelVO);
        }
        map.put("materielLevels",voList);
        map.put("total",total);
        return map;
    }

    @Override
    @Transactional
    public MaterielLevel putInWare(MaterielLevelForm form, MultipartFile file) {
        MaterielLevel level = repository.findByCode(form.getCode());
        //如果编码存在代表数量累加
        if (level != null) {
            Integer totalQuantity = form.getQuantity()+level.getQuantity();
            level.setQuantity(totalQuantity);
            return repository.save(level);
        }
        MaterielLevel materielLevel = new MaterielLevel();
        MaterielRecords records = new MaterielRecords();
        BeanUtils.copyProperties(form,materielLevel);
        BeanUtils.copyProperties(form,records);
        try {
            if (file != null) {
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                String fileName = form.getCode() + suffix;
                InputStream inputStream = file.getInputStream();
                String photo = fileSystemService.uploadFile("/materiel" + fileName, inputStream);
                if (StringUtils.isNotEmpty(photo)) {
                    materielLevel.setPhoto(photo);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new MTException(ResultEnum.FAIL);
        }
        Classify classify = classifyRepository.findById(form.getClassifyId()).orElse(null);
        records.setName(classify.getName());
        records.setInNum(form.getQuantity());
        records.setType(1);
        recordsRepository.save(records);
        return repository.save(materielLevel);
    }

    @Override
    public void exportTemplate(HttpServletResponse response) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("template/materiel.xlsx");
        SXSSFWorkbook workbook = ExportUtil.getWorkbook(0, inputStream);
        ExportUtil.downLoadExcelToWebsite(workbook,response,"物料模板");
    }

    @Override
    public void importMateriel(MultipartFile file) {
        List<String> firstRow = ImportUtil.getFirstRowContent(file);
        List<List<Object>> dataList = ExportUtil.read(file);
        if (dataList.size() == 0) {
            throw new MTException(ResultEnum.FILE_EMPTY);
        }
        if (firstRow.size() == 0 || !"物料编码".equals(firstRow.get(0)) || firstRow.size() != 10) {
            throw new MTException(ResultEnum.FILE_ERROR);
        }
        List<Classify> classifyList = classifyRepository.findAll();
        Map<String, Classify> classifyMap =
                classifyList.stream().collect(Collectors.toMap(new Function<Classify, String>() {
                    @Override
                    public String apply(Classify classify) {
                        return classify.getName();
                    }
                }, Function.identity(), (key1, key2) -> key1));
        List<MaterielLevel> materielLevelList = repository.findAll();
        Map<String, MaterielLevel> materielLevelMap =
                materielLevelList.stream().collect(Collectors.toMap(new Function<MaterielLevel, String>() {
                    @Override
                    public String apply(MaterielLevel materielLevel) {
                        return materielLevel.getCode();
                    }
                }, Function.identity(), (key1, key2) -> key1));
        // 导入的文件相当于新增一个订单
        OrderRecords records = new OrderRecords();
        records.setName(file.getOriginalFilename()+new Date());
        records.setRemarks("导入入库订单"+new Date());
        records.setInTime(new Date());
        records.setStatus(1);
        records.setType(1);
        OrderRecords save = orderRecordsRepository.save(records);

        List<MaterielLevel> materielLevels = new ArrayList<>();
        List<MaterielRecords> materielRecords = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            String code = (String)dataList.get(i).get(0);
            String classifyName = (String)dataList.get(i).get(1);
            String potting = (String)dataList.get(i).get(2);
            String quantity = (String)dataList.get(i).get(3);
            String model = (String)dataList.get(i).get(4);
            String brand = (String)dataList.get(i).get(5);
            String supplier = (String)dataList.get(i).get(6);
            String website = (String)dataList.get(i).get(7);
            String price = (String)dataList.get(i).get(8);
            String remarks = (String)dataList.get(i).get(9);
            if (StringUtils.isEmpty(code)) {
                throw new MTException("物料编码不能为空!",900);
            }
            if (StringUtils.isEmpty(classifyName)) {
                throw new MTException("分类不能为空!",900);
            }
            if (StringUtils.isEmpty(potting)) {
                throw new MTException("封装不能为空!",900);
            }
            if (StringUtils.isEmpty(quantity)) {
                throw new MTException("数量不能为空!",900);
            }
            if (StringUtils.isEmpty(model)) {
                throw new MTException("型号不能为空!",900);
            }
            Classify classify = classifyMap.get(classifyName);
            if (classify == null) {
                throw new MTException("分类系统中还不存在!",900);
            }
            //如果编码存在代表数量累加
            MaterielLevel level = materielLevelMap.get(code);
            if (level == null) {
                MaterielLevel materielLevel = new MaterielLevel(code,model,potting,Integer.parseInt(quantity),StringUtils.isEmpty(price)?null:Double.valueOf(price),brand,supplier,website,remarks,classify.getId());
                materielLevels.add(materielLevel);
                MaterielRecords materielRecord = new MaterielRecords(code,classifyName,model,potting,brand,StringUtils.isEmpty(price)?null:Double.valueOf(price),Integer.parseInt(quantity),0,Integer.parseInt(quantity),1,save.getId());
                materielRecords.add(materielRecord);
            } else {
                Integer totalQuantity = Integer.parseInt(quantity)+level.getQuantity();
                level.setQuantity(totalQuantity);
                materielLevels.add(level);
                MaterielRecords materielRecord = new MaterielRecords(code,classifyName,model,potting,brand,StringUtils.isEmpty(price)?null:Double.valueOf(price),Integer.parseInt(quantity),0,totalQuantity,1,save.getId());
                materielRecords.add(materielRecord);
            }
        }
        repository.saveAll(materielLevels);
        recordsRepository.saveAll(materielRecords);
    }
}
