package com.authority.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.authority.pojo.Criteria;
import com.authority.pojo.WebMessage;

public interface WebMessageMapper {
	/**
	 * 根据条件查询记录总数
	 */
	int countByExample(Criteria example);

	/**
	 * 根据条件删除记录
	 */
	int deleteByExample(Criteria example);

	/**
	 * 根据主键删除记录
	 */
	int deleteByPrimaryKey(String userId);
	
	int deleteByPrimaryKey_batch(List<String> list);

	/**
	 * 保存记录,不管记录里面的属性是否为空
	 */
	int insert(WebMessage record);

	/**
	 * 保存属性不为空的记录
	 */
	int insertSelective(WebMessage record);

	/**
	 * 根据条件查询记录集
	 */
	List<WebMessage> selectByExample(Criteria example);

	List<Object[]> selectforexcel(Criteria example);
	
	List selectlist(Criteria example);
	
	List<HashMap<String, Object>> selectByDynamicSql(Criteria dynamicSql);
	/**
	 * 根据主键查询记录
	 */
	WebMessage selectByPrimaryKey(String userId);

	/**
	 * 根据条件更新属性不为空的记录
	 */
	int updateByExampleSelective(@Param("record") WebMessage record, @Param("condition") Map<String, Object> condition);

	/**
	 * 根据条件更新记录
	 */
	int updateByExample(@Param("record") WebMessage record, @Param("condition") Map<String, Object> condition);

	/**
	 * 根据主键更新属性不为空的记录
	 */
	int updateByPrimaryKeySelective(WebMessage record);

	/**
	 * 根据主键更新记录
	 */
	int updateByPrimaryKey(WebMessage record);
}
