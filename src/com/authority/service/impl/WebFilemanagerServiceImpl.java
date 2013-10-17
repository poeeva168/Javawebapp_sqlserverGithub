package com.authority.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.authority.pojo.MyFile;
import com.authority.service.WebFilemanagerService;

@Service
public class WebFilemanagerServiceImpl implements WebFilemanagerService {
	
	private static final Logger logger = LoggerFactory.getLogger(WebFilemanagerServiceImpl.class);
	
	/**
	 * 2008-12-18-下午04:52:19
	 * 
	 * 功能:获得指定路径下大所有文件和文件夹信息，把数据封装到nodes返回
	 * 
	 * @param folder
	 *            当前要访问的文件夹目录名称
	 * @param onlyDirectory
	 *            null:获得所有信息，true:只获得文件夹,false:只获得文件信息
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List listFiles(String rootPath, String folder, boolean onlyDirectory) throws FileNotFoundException, IOException{
		List filelist = new ArrayList();
		File[] arrFiles = new File(rootPath + folder).listFiles();
		
		MyFile nd = null;
		if (arrFiles != null) {
			for (File f : arrFiles) {
				String id = f.getAbsolutePath();
				nd = new MyFile();
				nd.setId(id.substring(rootPath.length()));
				nd.setText(f.getName());
				nd.setLeaf(f.isFile());
				nd.setFileName(f.getName());
				if (f.isFile()) {					
					FileInputStream fis=new FileInputStream(f);
					int  size_ = fis.available();
					long size  = f.length();
					fis.close();
					if (size > 1048576) {
						nd.setFileSize((float)Math.round((100*size / 1048576f))/100 + " MB");
					}
					else if(size >1024){
						nd.setFileSize((float)Math.round((100*size / 1024f))/100 + " KB");
					}else {
						nd.setFileSize(size + " bytes");
					}					
					
				} else {
					nd.setFileSize("0 bytes");
				}
				nd.setLastModifyDate(new Date(f.lastModified()));
				if (onlyDirectory && !f.isDirectory()) {
					continue;
				}
				filelist.add(nd);
				f=null;
			}
		}
		
		return filelist;
	}

	
}
