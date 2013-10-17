package com.authority.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.authority.pojo.Criteria;
import com.authority.pojo.Tree;
import com.authority.pojo.WebMessage;

public interface WebQudviewService {

	/** 查询记录集数 */
	int countByExample(Criteria example);
	
	/** 查询记录集 */
	List<HashMap<String, Object>> selectByDynamicSql(Criteria example);
	
	
}
