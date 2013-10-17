package com.authority.pojo;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.authority.common.jackjson.CustomDateTimeDeserializer;

/**
 * File.java 
 * 
 *  文件信息类
 * 
 * Copyright (c) 2008 by MTA.
 * 
 * @author 
 * @version 1.0
 */
public class MyFile implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 节点id */
	private String id;

	/** 节点名称 */
	private String text;

	/** 是否是叶子节点 */
	private boolean leaf;

	/** 文件名 */
	private String fileName;

	/** 文件尺寸 */
	private String fileSize;

	/** 文件最后修改时间 */
	private Date lastModifyDate;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	@JsonDeserialize(using = CustomDateTimeDeserializer.class)
	public Date getLastModifyDate() {
		return lastModifyDate;
	}

	@JsonDeserialize(using = CustomDateTimeDeserializer.class)
	public void setLastModifyDate(Date lastModifyDate) {
		this.lastModifyDate = lastModifyDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
