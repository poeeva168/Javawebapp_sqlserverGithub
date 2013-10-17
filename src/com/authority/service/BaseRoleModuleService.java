package com.authority.service;

import com.authority.pojo.BaseRoleModule;
import com.authority.pojo.Criteria;

import java.util.List;

public interface BaseRoleModuleService {
	int countByExample(Criteria example);

	BaseRoleModule selectByPrimaryKey(String roleModuleId);

	List<BaseRoleModule> selectByExample(Criteria example);
}