package com.authority.service.impl;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.authority.dao.WebQudviewMapper;
import com.authority.pojo.Criteria;
import com.authority.pojo.Tree;
import com.authority.pojo.TreeCommonMenu;
import com.authority.service.WebQudviewService;

@Service
public class WebQudviewServiceImp implements WebQudviewService {
	
	private static final Logger logger = LoggerFactory.getLogger(WebQudviewServiceImp.class);
	
	@Autowired
	private WebQudviewMapper webqudviewmapper;

	@Override
	public int countByExample(Criteria example) {
		int count = this.webqudviewmapper.countByExample(example);
		logger.debug("count: {}", count);
		return count;
	}

	@Override
	public List<HashMap<String, Object>> selectByDynamicSql(Criteria example) {
		return this.webqudviewmapper.selectByDynamicSql(example);
	}

	

}
