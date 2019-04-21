package com.jeesite.modules.dg.upload.oss;

import com.jeesite.modules.file.entity.FileEntity;
import com.jeesite.modules.file.entity.FileUpload;


public interface OssUploadService {

    public boolean fileExists(FileEntity fileEntity);
    public String getFileUrl(FileUpload fileUpload);
    public String uploadFile(FileEntity fileEntity);
}
