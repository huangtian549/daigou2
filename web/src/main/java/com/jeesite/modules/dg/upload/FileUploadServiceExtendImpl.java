package com.jeesite.modules.dg.upload;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.jeesite.common.io.FileUtils;
import com.jeesite.modules.dg.upload.oss.OssUploadService;
import com.jeesite.modules.file.entity.FileEntity;
import com.jeesite.modules.file.entity.FileUpload;
import com.jeesite.modules.file.service.support.FileUploadServiceExtendSupport;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

@Service
public class FileUploadServiceExtendImpl extends FileUploadServiceExtendSupport {

    private static final Log log = LogFactory.getLog(FileUploadServiceExtendImpl.class);
    @Autowired
    private ApplicationContext applicationContext;

//    @Value("${qiniu.file.ossService}")
    private String ossServiceName = "Qiniuyun";

    private OssUploadService getOssUploadService(){
        return (OssUploadService)applicationContext.getBean(ossServiceName);
    }

    /**
     * 读取字节输入流内容
     *
     * @param is
     * @return
     */
    private byte[] readInputStream(InputStream is) {
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        byte[] buff = new byte[1024 * 2];
        int len = 0;
        try {
            while ((len = is.read(buff)) != -1) {
                writer.write(buff, 0, len);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toByteArray();
    }
    @Override
    public String downFile(FileUpload fileUpload, HttpServletRequest request, HttpServletResponse response) {
        OkHttpClient client = new OkHttpClient();
        String url = this.getFileUrl(fileUpload);
        Request req = new Request.Builder().url(url).build();
        String ret = "";
        try {
            okhttp3.Response resp = client.newCall(req).execute();
            if (resp.isSuccessful()) {
                ResponseBody body = resp.body();
                String[] fileName = fileUpload.getFileName().split("\\.");
                String preffix = "";
                String suffix = "";
                if (fileName.length == 2) {
                    preffix = fileName[0];
                    suffix = "." + fileName[1];
                } else {
                    preffix = fileUpload.getFileName();
                }

                File f = File.createTempFile(preffix, suffix);
                InputStream is = body.byteStream();
                byte[] data = readInputStream(is);
                FileOutputStream out = new FileOutputStream(f);
                out.write(data);
                out.close();
                ret = FileUtils.downFile(f, request, response);
            }
        } catch (IOException ex) {
            log.error(ex);
        }
        return ret;
    }

    public FileUploadServiceExtendImpl() {
        super();
    }

    @Override
    public void saveUploadFile(FileUpload fileUpload) {
        super.saveUploadFile(fileUpload);
    }

    @Override
    public void uploadFile(FileEntity fileEntity) {
    	log.info("log: go into FileUloadServiceExtendImpl.upload");
        //super.uploadFile(fileEntity);
        getOssUploadService().uploadFile(fileEntity);
        log.info("log: go out FileUloadServiceExtendImpl.upload");
    }

    @Override
    public boolean fileExists(FileEntity fileEntity) {
    	log.info("log: go into FileUloadServiceExtendImpl.fileExists");
        return getOssUploadService().fileExists(fileEntity);
        
    }

    @Override
    public String getFileUrl(FileUpload fileUpload) {
    	log.info("log: go into FileUloadServiceExtendImpl.getFileUrl");
        return getOssUploadService().getFileUrl(fileUpload);
    }
}
