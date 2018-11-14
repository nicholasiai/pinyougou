package com.pinyougou.shop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import util.FastDFSClient;



/**
 * 上传文件控制层
 * @author IAI
 *
 */
@RestController
public class UploadController {
	
	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;
	
	@RequestMapping("/upload")
	public Result upload(MultipartFile file ) {
		//获取文件拓展名
		String filename = file.getOriginalFilename();
		String extName = filename.substring(filename.lastIndexOf(".")+1);
												 
		try {
//			创建FastDFSC客户端
			FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
			
			//执行文件上传
			String path = client.uploadFile(file.getBytes(),extName);
			
			//拼接返回的路径提取完整的url
			String url = FILE_SERVER_URL+path;
			return new Result(true,url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(true,"上传失败");
		}
		
		
	}
}
