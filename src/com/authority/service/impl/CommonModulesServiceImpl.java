package com.authority.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.authority.dao.CommonModulesMapper;
import com.authority.pojo.CommonModules;
import com.authority.pojo.Criteria;
import com.authority.pojo.Tree;
import com.authority.pojo.TreeCommonMenu;
import com.authority.service.CommonModulesService;

@Service
public class CommonModulesServiceImpl implements CommonModulesService {

	@Autowired
	private CommonModulesMapper commonmodulesmapper;
	
	private static final Logger logger = LoggerFactory.getLogger(CommonModulesServiceImpl.class);

	@Override
	public Tree selectAllModules(Criteria example) {
		
		
		List<CommonModules> list = this.commonmodulesmapper.selectByExample(example);
		
		TreeCommonMenu menu = new TreeCommonMenu(list);
		return menu.getTreeJson();
	}

	
	
	
	
}
