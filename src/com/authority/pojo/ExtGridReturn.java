package com.authority.pojo;

import java.util.List;
import java.util.Map;

/**
 * Ext Grid返回对象
 * 
 * @author chenxin
 * @date 2011-3-10 下午09:43:35
 */
public class ExtGridReturn {

	/**
	 * 总共条数
	 */
	private int results;
	/**
	 * 所有数据
	 */
	private List<?> rows;
	
	/**
	 * 所有的列信息
	 */    
    private Map<String,Object> metaData;
    
	public ExtGridReturn() {
	}

	public ExtGridReturn(List<?> rows) {
		this.rows = rows;
	}
	
	public ExtGridReturn(int results, List<?> rows) {
		this.results = results;
		this.rows = rows;
	}
	
	public ExtGridReturn(int results, List<?> rows,Map<String,Object> metaData) {
		this.results = results;
		this.rows = rows;
		this.metaData=metaData;
	}

	public int getResults() {
		return results;
	}

	public void setResults(int results) {
		this.results = results;
	}

	public List<?> getRows() {
		return rows;
	}

	public void setRows(List<?> rows) {
		this.rows = rows;
	}

	public Map<String, Object> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}

	
}
