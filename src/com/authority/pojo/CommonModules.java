package com.authority.pojo;

import java.io.Serializable;


/**
 * 系统模块表
 */
public class CommonModules implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 模块ID
	 */
	private String moduleId;

	/**
	 * 模块名称
	 */
	private String moduleName;

	/**
	 * 模块URL
	 */
	private String moduleUrl;

	/**
	 * 父模块ID
	 */
	private String parentId;

	/**
	 * 叶子节点(0:树枝节点;1:叶子节点)
	 */
	private String leaf;

	/**
	 * 展开状态(1:展开;0:收缩)
	 */
	private String expanded;

	/**
	 * 显示顺序
	 */
	private String displayIndex;

	/**
	 * 是否显示 0:否 1:是
	 */
	private String isDisplay;


	/**
	 * 图标或者样式
	 */
	private String iconCss;

	
	
	
	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleUrl() {
		return moduleUrl;
	}

	public void setModuleUrl(String moduleUrl) {
		this.moduleUrl = moduleUrl;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getLeaf() {
		return leaf;
	}

	public void setLeaf(String leaf) {
		this.leaf = leaf;
	}

	public String getExpanded() {
		return expanded;
	}

	public void setExpanded(String expanded) {
		this.expanded = expanded;
	}

	public String getDisplayIndex() {
		return displayIndex;
	}

	public void setDisplayIndex(String displayIndex) {
		this.displayIndex = displayIndex;
	}

	public String getIsDisplay() {
		return isDisplay;
	}

	public void setIsDisplay(String isDisplay) {
		this.isDisplay = isDisplay;
	}
	
	public String getIconCss() {
		return iconCss;
	}

	public void setIconCss(String iconCss) {
		this.iconCss = iconCss;
	}

		
}