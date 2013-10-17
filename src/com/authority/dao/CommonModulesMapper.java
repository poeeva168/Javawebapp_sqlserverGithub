package com.authority.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.authority.pojo.CommonModules;
import com.authority.pojo.Criteria;

public interface CommonModulesMapper {
	
	/**
	 * 根据条件查询记录集
	 */
	List<CommonModules> selectByExample(Criteria example);

	
}