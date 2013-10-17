package com.authority.service;

import com.authority.pojo.BaseUserRole;
import com.authority.pojo.Criteria;

import java.util.List;

public interface BaseUserRoleService {
	int countByExample(Criteria example);

	BaseUserRole selectByPrimaryKey(String userRoleId);

	List<BaseUserRole> selectByExample(Criteria example);

}