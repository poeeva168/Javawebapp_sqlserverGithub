package com.authority.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.authority.pojo.Criteria;
import com.authority.pojo.WebMessage;

public interface WebMessageService {

	int countByExample(Criteria example);
	
	WebMessage selectByPrimaryKey(String id);
	
	List<WebMessage> selectByExample(Criteria example);
	List<Object[]> selectforexcel(Criteria example);
	List selectlist(Criteria example);
	List<HashMap<String, Object>> selectByDynamicSql(Criteria example);
	/**
	 * 根据主键删除
	 * 
	 * @param example
	 * @return 00：失败，01：成功 ,其他情况
	 */
	String deleteByPrimaryKey(Criteria example);
	String deleteByPrimaryKey_batch(List<String> list);
	/**
	 * 保存消息
	 * 
	 * @param example
	 * @return 00：失败，01：成功 ,其他情况
	 */
	String saveMes(Criteria example);

	/**
	 * 保存用户自己更新的信息
	 * 
	 * @param user
	 * @return 00：失败，01：成功 ,其他情况
	 */
	String updateByPrimaryKeySelective(WebMessage mes);
	
	void Excel_pro(List<WebMessage> list,String[] col_id,String[] col_name,String fileName,HttpServletResponse response);
	
}
