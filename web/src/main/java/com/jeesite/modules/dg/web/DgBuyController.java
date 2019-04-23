/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 */
package com.jeesite.modules.dg.web;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.utils.excel.ExcelExport;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.dg.entity.DgBuy;
import com.jeesite.modules.dg.service.DgBuyService;
import com.jeesite.modules.dg.utils.ToolUtil;
import com.jeesite.modules.file.entity.FileEntity;
import com.jeesite.modules.file.entity.FileUpload;
import com.jeesite.modules.file.service.FileUploadService;
import com.jeesite.modules.file.service.support.FileUploadServiceExtendSupport;
import com.jeesite.modules.sys.entity.EmpUser;

/**
 * dg_buyController
 * @author eric
 * @version 2019-02-26
 */
@Controller
@RequestMapping(value = "${adminPath}/dg/dgBuy")
public class DgBuyController extends BaseController {

	@Autowired
	private DgBuyService dgBuyService;
	
	@Autowired
	private FileUploadServiceExtendSupport fileUploadServiceExtend;
	
	/**
	 * 获取数据
	 */
	@ModelAttribute
	public DgBuy get(String ids, boolean isNewRecord) {
		return dgBuyService.get(ids, isNewRecord);
	}
	
	/**
	 * 查询列表
	 */
	@RequiresPermissions("dg:dgBuy:view")
	@RequestMapping(value = {"list", ""})
	public String list(DgBuy dgBuy, Model model) {
		model.addAttribute("dgBuy", dgBuy);
		return "modules/dg/dgBuyList";
	}
	
	/**
	 * 查询列表数据
	 */
	@RequiresPermissions("dg:dgBuy:view")
	@RequestMapping(value = "listData")
	@ResponseBody
	public Page<DgBuy> listData(DgBuy dgBuy, HttpServletRequest request, HttpServletResponse response) {
		dgBuy.setPage(new Page<>(request, response));
		Page<DgBuy> page = dgBuyService.findPage(dgBuy);
		return page;
	}


	/**
	 * 导出用户数据
	 */
	@RequiresPermissions("dg:dgBuy:view")
	@RequestMapping(value = "exportData")
	public void exportData(DgBuy dgBuy, HttpServletRequest request, HttpServletResponse response) {
		dgBuy.setPage(new Page<>(request, response));
		Page<DgBuy> page = dgBuyService.findPage(dgBuy);
		List<DgBuy>list = page.getList();
		String fileName = "用户数据" + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";
		try(ExcelExport ee = new ExcelExport("用户数据", DgBuy.class)){
			ee.setDataList(list).write(response, fileName);
		}
	}
	
	
	@RequestMapping(value = "processPic")
	public void processPic(HttpServletRequest request, HttpServletResponse response) {
		Page<DgBuy> page = new Page<>(request, response);
		page.setPageSize(100);
		for (int i = 1; i < 30; i++) {
			page.setPageNo(i);
			DgBuy dgBuy = new DgBuy();
			dgBuy.setPage(page);
			Page<DgBuy> page2 = dgBuyService.findPage(dgBuy );
			List<DgBuy>list = page2.getList();
			for (DgBuy dgBuy2 : list) {
				try {
					processOne(dgBuy2);
				} catch (Exception e) {
					System.out.println("process error:" + dgBuy2.getId());
				}
			}
		}
	}
	
	private void processOne(DgBuy dgBuy) {
		if (dgBuy != null) {
			Pattern pattern = Pattern.compile("201[0-9]{5}");
			String path =  dgBuy.getPic(); 
			System.out.println();
			String[] pathArray = path.split(",");
			for (int i = 0; i < pathArray.length; i++) {
				if (pathArray[i].length() == 0) {
					continue;
				}
				Matcher matcher = pattern.matcher(pathArray[i]);
				String dateString = "";
				while(matcher.find()){
					dateString = matcher.group();
				}
				if (dateString.length() == 0) {
					continue;
				}
				File destFile = new File("/Users/liunaikun/Downloads/aa"); //new File("/root/userfiles/fileupload/" + dateString);
				File file = new File("/Users/liunaikun/Downloads/t1553083025925_41929.jpeg");//new File("/home" + pathArray[i]);
				if (file.exists()) {
					try {
						FileUtils.copyFileToDirectory(file, destFile);
						String md5 = Md5Utils.md5File(file);
						Long fileSize = file.length();
						String qiniuId = uploadToQiNiu(md5, dateString, fileSize, file.getName());
						dgBuy.setRemark1(qiniuId);
						dgBuyService.save(dgBuy);
						System.out.println("copy finished:" + pathArray[i]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else {
					System.out.println("path not exist:" + file.getAbsolutePath());
				}
				
			}
		}
		
	}
	
	
	private String uploadToQiNiu(String md5, String dateString, Long fileSize, String fileName) {
		FileEntity fileEntity = new FileEntity();
		fileEntity.setFileContentType("image/jpeg");
		fileEntity.setFileExtension("jpeg");
		fileEntity.setFileId(ToolUtil.getNum19());
		fileEntity.setFileMd5(md5);
		fileEntity.setFilePath(dateString);
		fileEntity.setFileSize(fileSize);
		fileEntity.setStatus("1");
		
		fileUploadServiceExtend.uploadFile(fileEntity);
		
		FileUpload fileUpload = new FileUpload();
		fileUpload.setFileEntity(fileEntity);
		fileUpload.setFileType("image");
		fileUpload.setFileName(fileName);
		fileUploadServiceExtend.saveUploadFile(fileUpload );
		
		String fileUrl = fileUploadServiceExtend.getFileUrl(fileUpload);
		if (fileUrl != null && fileUrl.length() > 0) {
			String qiniuId = fileUrl.split("\\?")[0].replace("http://ppndeld0s.bkt.clouddn.com/", "");
			return qiniuId;
		}else {
			return "";
		}
		
	}
	
	/**
	 * 查看编辑表单
	 */
	@RequiresPermissions("dg:dgBuy:view")
	@RequestMapping(value = "form")
	public String form(DgBuy dgBuy, Model model) {
		model.addAttribute("dgBuy", dgBuy);
		return "modules/dg/dgBuyForm";
	}

	/**
	 * 保存dg_buy
	 */
	@RequiresPermissions("dg:dgBuy:edit")
	@PostMapping(value = "save")
	@ResponseBody
	public String save(@Validated DgBuy dgBuy) {
		if(StringUtil.isBlank(dgBuy.getIds())) {
			dgBuy.setPurchasedate(new java.sql.Date(new java.util.Date().getTime()));
		}
		dgBuyService.save(dgBuy);
		return renderResult(Global.TRUE, text("保存购买记录成功！"));
	}
	
	/**
	 * 删除dg_buy
	 */
	@RequiresPermissions("dg:dgBuy:edit")
	@RequestMapping(value = "delete")
	@ResponseBody
	public String delete(DgBuy dgBuy) {
		dgBuyService.delete(dgBuy);
		return renderResult(Global.TRUE, text("删除购买记录成功！"));
	}
	
}