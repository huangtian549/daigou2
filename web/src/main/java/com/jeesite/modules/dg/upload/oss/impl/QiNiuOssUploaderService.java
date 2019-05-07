package com.jeesite.modules.dg.upload.oss.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.jeesite.modules.dg.upload.oss.OssUploadService;
import com.jeesite.modules.file.entity.FileEntity;
import com.jeesite.modules.file.entity.FileUpload;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;

@Service("Qiniuyun")
public class QiNiuOssUploaderService implements OssUploadService {
    private static final Log log = LogFactory.getLog(QiNiuOssUploaderService.class);

//    @Value("${qiniu.file.accessKey}")
    private String accessKey = "5we4SuM8VVUz1nesGzySNPQYnwVEsMnoBGNOELXE";
//    @Value("${qiniu.file.secretKey}")
    private String secretKey = "zAUtDcIo3z0c6HtYqbmRMETxBPIwqALdHQmTLzUr";
//    @Value("${qiniu.file.bucket}")
    private String bucketName = "daigou";
//    @Value("${qiniu.file.domainOfBucket}")
    private String domainName = "http://ppndeld0s.bkt.clouddn.com";

    private Configuration cfg;
    private Auth auth;

    public QiNiuOssUploaderService() {
        cfg = new Configuration(Zone.zone1());
    }

    private void init() {

        auth = Auth.create(accessKey, secretKey);
    }

    @Override
    public boolean fileExists(FileEntity fileEntity) {
        init();

        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            String key = fileEntity.getFileId();
            FileInfo fileInfo = bucketManager.stat(bucketName, key);
            if (fileInfo != null) {
                return true;
            } else {
                return false;
            }

        } catch (QiniuException ex) {
            log.error(ex.toString());
        }
        return true;
    }

    @Override
    public String getFileUrl(FileUpload fileUpload) {
        init();
        String fileName = fileUpload.getFileEntity().getFileId();
        String fileUrl = String.format("%s/%s", domainName, fileName);

        init();
        long expirInSeconds = 3600; ////1小时，可以自定义链接过期时间
        fileUrl = auth.privateDownloadUrl(fileUrl, expirInSeconds);
        return fileUrl;
    }

    @Override
    public String uploadFile(FileEntity fileEntity) {
    	log.info("log: go into QiNiuOssUpload.uploadFile");
        init();
        UploadManager uploadManager = new UploadManager(cfg);
        String result = "";
        String token = auth.uploadToken(bucketName);
        try {
            String key = fileEntity.getFileId();
            Response response = uploadManager.put(fileEntity.getFileRealPath(), key, token);
            DefaultPutRet defaultPutRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            result = defaultPutRet.key;
        } catch (QiniuException ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
        log.info("log: go out QiNiuOssUpload.uploadFile");
        return result;
    }


}
