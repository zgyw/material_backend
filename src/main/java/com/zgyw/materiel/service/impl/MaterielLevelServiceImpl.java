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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
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
        Map<Integer, Classify> classifyMap = classifyService.getClassifyIK();
        List<Integer> classifyIds = new ArrayList<>();
        if (classifyId != null) {
            classifyIds.add(classifyId);
        } else {
            List<Classify> classifyList = classifyRepository.findAll();
            classifyIds = classifyList.stream().map(e -> e.getId()).collect(Collectors.toList());
        }
        materielLevels = repository.findByClassifyIdInAndCodeContainingOrClassifyIdInAndModelContainingOrClassifyIdInAndPottingContaining(classifyIds, content, classifyIds, content, classifyIds, content, pageable).getContent();
        total = repository.countByClassifyIdInAndCodeContainingOrClassifyIdInAndModelContainingOrClassifyIdInAndPottingContaining(classifyIds,content,classifyIds,content,classifyIds,content);
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
        String photo = "";
        try {
            if (file != null) {
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                String fileName = form.getCode() + suffix;
                InputStream inputStream = file.getInputStream();
                photo = fileSystemService.uploadFile("/materiel/" + fileName, inputStream);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new MTException(ResultEnum.FAIL);
        }
        MaterielLevel level = repository.findByCode(form.getCode());
        //如果编码存在代表数量累加
        if (level != null) {
            Integer totalQuantity = form.getQuantity()+level.getQuantity();
            level.setQuantity(totalQuantity);
            try {
                if (StringUtils.isNotEmpty(level.getPhoto())){
                    fileSystemService.deleteFile(level.getPhoto());
                }
            } catch (Exception e) {
                log.error(getClass().getName(),e);
                throw new MTException(ResultEnum.FAIL);
            }
            if (StringUtils.isNotEmpty(photo)) {
                level.setPhoto(photo);
            }
            List<MaterielRecords> list = recordsRepository.findByCode(level.getCode());
            List<MaterielRecords> recordsList = new ArrayList<>();
            for (MaterielRecords materielRecords : list) {
                materielRecords.setQuantity(totalQuantity);
                materielRecords.setInNum(materielRecords.getInNum()+form.getQuantity());
                recordsList.add(materielRecords);
            }
            recordsRepository.saveAll(recordsList);
            return repository.save(level);
        }
        //编码不存在新增物料
        MaterielLevel materielLevel = new MaterielLevel();
        MaterielRecords records = new MaterielRecords();
        BeanUtils.copyProperties(form,materielLevel);
        BeanUtils.copyProperties(form,records);
        if (StringUtils.isNotEmpty(photo)) {
            materielLevel.setPhoto(photo);
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
        ExportUtil.downLoadExcelToWebsite(workbook,response,"入库订单模板");
    }

    @Override
    @Transactional
    public void importMateriel(MultipartFile file) {
        List<List<Object>> dataList = ImportUtil.checkFile(file, "物料编码", 11);
        String fileName = file.getOriginalFilename().split(".")[0];
        OrderRecords orderRecords = orderRecordsRepository.findByTypeAndName(1, fileName);
        if (orderRecords != null) {
            throw new MTException(ResultEnum.NAME_EXIST);
        }
        Map<String, Classify> classifyMap = classifyService.getClassifySK();
        Map<String, MaterielLevel> materielMap = getMateriel();
        // 导入的文件相当于创建一个入库订单
        OrderRecords records = new OrderRecords();
        records.setName(fileName);
        records.setRemarks("导入入库订单");
        records.setInTime(new Date());
        records.setStatus(0);
        records.setType(1);
        OrderRecords save = orderRecordsRepository.save(records);
        List<MaterielRecords> recordsList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            String code = (String)dataList.get(i).get(0);
            String classifyName = (String)dataList.get(i).get(1);
            String potting = (String)dataList.get(i).get(2);
            String factoryModel = (String)dataList.get(i).get(3);
            String quantity = (String)dataList.get(i).get(4);
            String model = (String)dataList.get(i).get(5);
            String brand = (String)dataList.get(i).get(6);
            String supplier = (String)dataList.get(i).get(7);
            String website = (String)dataList.get(i).get(8);
            String price = (String)dataList.get(i).get(9);
            String remarks = (String)dataList.get(i).get(10);
            if (StringUtils.isEmpty(code)) {
                throw new MTException("物料编码不能为空!出现在第"+(i+2)+"行",900);
            }
            if (StringUtils.isEmpty(classifyName)) {
                throw new MTException("分类不能为空!出现在第"+(i+2)+"行",900);
            }
            if (StringUtils.isEmpty(quantity)) {
                throw new MTException("数量不能为空!出现在第"+(i+2)+"行",900);
            }
            Classify classify = classifyMap.get(classifyName);
            if (classify == null) {
                throw new MTException("分类系统中还不存在!出现在第"+(i+2)+"行",900);
            }
            //新增加的订单只是创建不直接进库存
            MaterielLevel level = materielMap.get(code);
            Integer totalQ = 0;
            if (level != null) {
                totalQ = level.getQuantity();
            }
            MaterielRecords materielRecords = new MaterielRecords(code, classifyName, model, potting, brand, price, Integer.parseInt(quantity), 0, totalQ, 1, factoryModel, save.getId(), remarks, supplier,website);
            recordsList.add(materielRecords);
        }
        recordsRepository.saveAll(recordsList);
    }

    @Override
    public MaterielLevel detail(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public MaterielLevel modify(MaterielLevelForm form, MultipartFile file) {
        MaterielLevel materielLevel = repository.findByCode(form.getCode());
        List<MaterielRecords> materielRecordsList = recordsRepository.findByCode(form.getCode());
        try {
            if (file != null) {
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                String fileName = form.getCode() + suffix;
                if (StringUtils.isNotEmpty(materielLevel.getPhoto())) {
                    fileSystemService.deleteFile(materielLevel.getPhoto());
                }
                InputStream inputStream = file.getInputStream();
                String photo = fileSystemService.uploadFile("/materiel/" + fileName, inputStream);
                if (StringUtils.isNotEmpty(photo)) {
                    materielLevel.setPhoto(photo);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new MTException(ResultEnum.FAIL);
        }
        BeanUtils.copyProperties(form,materielLevel);
        Map<Integer, Classify> classifyMap = classifyService.getClassifyIK();
        Classify classify = classifyMap.get(form.getClassifyId());
        List<MaterielRecords> list = new ArrayList<>();
        for (MaterielRecords materielRecords : materielRecordsList) {
            materielRecords.setName(classify.getName());
            materielRecords.setModel(form.getModel());
            materielRecords.setPotting(form.getPotting());
            materielRecords.setBrand(form.getBrand());
            materielRecords.setPrice(form.getPrice());
            materielRecords.setQuantity(form.getQuantity());
            list.add(materielRecords);
        }
        recordsRepository.saveAll(list);
        return repository.save(materielLevel);
    }

    @Override
    public byte[] getPhoto(Integer id, HttpServletResponse response) {
        MaterielLevel materielLevel = repository.findById(id).orElse(null);
        byte[] bytes = new byte[0];
        try {
            File file = fileSystemService.downloadFile(materielLevel.getPhoto());
            FileInputStream inputStream = new FileInputStream(file);
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes,0,inputStream.available());
            inputStream.close();
            file.delete();
        } catch (Exception e) {
            log.error(getClass().getName(),e);
            throw new MTException(ResultEnum.FAIL);
        }
        return bytes;
    }

    @Override
    @Transactional
    public void delete(String code) {
        List<MaterielRecords> recordsList = recordsRepository.findByCode(code);
        MaterielLevel materielLevel = repository.findByCode(code);
        try {
            if (StringUtils.isNotEmpty(materielLevel.getPhoto())) {
                fileSystemService.deleteFile(materielLevel.getPhoto());
            }
        } catch (Exception e) {
            log.error(getClass().getName(),e);
            throw new MTException(ResultEnum.FAIL);
        }
        recordsRepository.deleteInBatch(recordsList);
        repository.delete(materielLevel);
    }

    @Override
    public Map<String, MaterielLevel> getMateriel() {
        List<MaterielLevel> list = repository.findAll();
        Map<String, MaterielLevel> materielMap =
                list.stream().collect(Collectors.toMap(new Function<MaterielLevel, String>() {
                    @Override
                    public String apply(MaterielLevel materielLevel) {
                        return materielLevel.getCode();
                    }
                }, Function.identity(), (key1, key2) -> key1));
        return materielMap;
    }
}
