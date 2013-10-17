package com.authority.dao;

import java.util.HashMap;
import java.util.List;

import com.authority.pojo.CommonModules;
import com.authority.pojo.Criteria;

public interface WebQudviewMapper {
	/**
	 * 根据条件查询记录总数
	 */
	int countByExample(Criteria example);
	
	/**
	 * 根据条件查询记录集
	 */
		
	List<HashMap<String, Object>> selectByDynamicSql(Criteria dynamicSql);	
	
	
}
