package test.controls;

import java.util.Date;

/**
 * <b>function:</b>
 * @author hoojo
 * @createDate 2011-6-3 下午06:40:07
 * @file Message.java
 * @package com.hoo.entity
 * @project DWRComet
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 * @version 1.0
 */
public class Message {
    private int id;
    private String msg;
    private Date time;
    
    //getter、setter
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
    
}