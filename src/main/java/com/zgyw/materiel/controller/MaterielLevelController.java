package com.zgyw.materiel.controller;


import com.zgyw.materiel.VO.ResultVO;
import com.zgyw.materiel.bean.MaterielLevel;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.form.MaterielLevelForm;
import com.zgyw.materiel.service.MaterielLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;


@RestController
public class MaterielLevelController {
    @Autowired
    private MaterielLevelService service;

    @GetMapping("/materielLevel/pageList")
    public ResultVO pageList(@RequestParam(value = "page", defaultValue = "0") Integer page,
                             @RequestParam(value = "size", defaultValue = "10") Integer size,
                             @RequestParam(name = "classifyId",required = false)Integer classifyId,
                             @RequestParam(name = "content",required = false,defaultValue = "")String content) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Map<String, Object> result = service.pageList(classifyId, content, pageRequest);
        return ResultVO.success(result);
    }

    @PostMapping("/materielLevel/putInWare")
    public ResultVO putInWare(@Valid MaterielLevelForm form, MultipartFile file, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new MTException(ResultEnum.PARAM_ERROR);
        }
        MaterielLevel result = service.putInWare(form, file);
        return ResultVO.success(result);
    }

    @GetMapping("/materielLevel/exportTemplate")
    public void exportTemplate(HttpServletResponse response) {
        service.exportTemplate(response);
    }

    @PostMapping("/materielLevel/importMateriel")
    public ResultVO importMateriel(MultipartFile file) {
        service.importMateriel(file);
        return ResultVO.success();
    }
}
