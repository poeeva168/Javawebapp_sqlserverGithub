package com.authority.service;

import com.authority.pojo.CommonModules;
import com.authority.pojo.Criteria;
import com.authority.pojo.Tree;

import java.util.HashMap;
import java.util.List;

public interface CommonModulesService {

	/**
	 * 加载用户所有模块
	 * @param example
	 * @return
	 */
	Tree selectAllModules(Criteria example);
	
}