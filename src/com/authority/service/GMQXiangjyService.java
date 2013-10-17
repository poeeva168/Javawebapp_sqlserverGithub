package com.authority.service;

import java.util.List;
import java.util.Map;

public interface GMQXiangjyService {

	int query_count(Map<String,Object> param);
	
	List<Map<String, Object>> query_list(Map<String,Object> param);
	
	String save(List list);
}
