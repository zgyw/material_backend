package com.zgyw.materiel.service;

import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.bean.OrderRecords;
import com.zgyw.materiel.form.OrderRecordsForm;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface OrderRecordsService {

    List<OrderRecords> findByType(Integer type);

    Map<String,Object> pageList(Integer type, Integer status, String content, Pageable pageable);

    OrderRecords saveOrder(OrderRecordsForm form);

    OrderRecords modifyOrder(OrderRecordsForm form);

    OrderRecords detail(Integer id);

    void delete(Integer id);

    void exportTemplate(HttpServletResponse response);

    void importMateriel(MultipartFile file);

    List<MaterielLevel> putInWare(Integer orderId);

    List<MaterielLevel> checkOutWare(Integer orderId);
}
