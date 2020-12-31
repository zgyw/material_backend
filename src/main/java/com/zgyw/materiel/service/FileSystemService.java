package com.zgyw.materiel.service;

import java.io.File;
import java.io.InputStream;

public interface FileSystemService {
    String uploadFile(String targetPath, InputStream inputStream) throws Exception;

    File downloadFile(String targetPath) throws Exception;

    boolean deleteFile(String targetPath) throws Exception;

    boolean deleteFiles(String path) throws Exception;
}
