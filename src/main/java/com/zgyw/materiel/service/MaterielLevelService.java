package com.zgyw.materiel.service;

import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.form.MaterielLevelForm;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface MaterielLevelService {

    Map<String,Object> pageList(Integer classifyId, String content,Pageable pageable);

    MaterielLevel putInWare(MaterielLevelForm form, MultipartFile file);

    void exportTemplate(HttpServletResponse response);

    void importMateriel(MultipartFile file);

    MaterielLevel detail(Integer id);

    MaterielLevel modify(MaterielLevelForm form, MultipartFile file);

    byte[] getPhoto(Integer id,HttpServletResponse response);

    void delete(String code);

    Map<String, MaterielLevel> getMateriel();
}
