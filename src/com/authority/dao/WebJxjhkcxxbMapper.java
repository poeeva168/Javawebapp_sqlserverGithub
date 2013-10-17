package com.authority.dao;

import com.authority.pojo.Criteria;
import com.authority.pojo.WebJxjhkcxxb;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface WebJxjhkcxxbMapper {
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

	/**
	 * 保存记录,不管记录里面的属性是否为空
	 */
	int insert(WebJxjhkcxxb record);

	/**
	 * 保存属性不为空的记录
	 */
	int insertSelective(WebJxjhkcxxb record);

	/**
	 * 根据条件查询记录集
	 */
	List<WebJxjhkcxxb> selectByExample(Criteria example);

	/**
	 * 根据主键查询记录
	 */
	WebJxjhkcxxb selectByPrimaryKey(String userId);

	/**
	 * 根据条件更新属性不为空的记录
	 */
	int updateByExampleSelective(@Param("record") WebJxjhkcxxb record, @Param("condition") Map<String, Object> condition);

	/**
	 * 根据条件更新记录
	 */
	int updateByExample(@Param("record") WebJxjhkcxxb record, @Param("condition") Map<String, Object> condition);

	/**
	 * 根据主键更新属性不为空的记录
	 */
	int updateByPrimaryKeySelective(WebJxjhkcxxb record);

	/**
	 * 根据主键更新记录
	 */
	int updateByPrimaryKey(WebJxjhkcxxb record);
}