package com.zgyw.materiel.service;

import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.form.MaterielLevelForm;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface MaterielLevelService {

    Map<String,Object> pageList(Integer classifyId, String content,Pageable pageable);

    MaterielLevel putInWare(MaterielLevelForm form, MultipartFile file);

    void exportTemplate(HttpServletResponse response);

    void importMateriel(MultipartFile file);
}
