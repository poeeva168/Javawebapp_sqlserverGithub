package com.authority.service;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.authority.pojo.Criteria;
import com.authority.pojo.Tree;
import com.authority.pojo.WebMessage;

public interface WebFilemanagerService {

	
	/** 查询记录集 
	 * @throws IOException 
	 * @throws FileNotFoundException */
	List listFiles(String rootPath, String folder, boolean onlyDirectory) throws FileNotFoundException, IOException;
	
	
}
