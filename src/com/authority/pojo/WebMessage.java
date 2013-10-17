package com.authority.pojo;

import java.io.Serializable;
import java.util.Date;



import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.authority.common.jackjson.CustomDateTimeDeserializer;
import com.authority.common.jackjson.CustomDateTimeSerializer;

public class WebMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String head;
	private String body;
	private Date begin_date;
	private Date end_date;
	private String mxdx;
	private Date release_time;
	private String release_ip;
	private String release_per;
	private String isdisplay;
	private String isread;
	
	
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHead() {
		return head;
	}
	public void setHead(String head) {
		this.head = head;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public Date getBegin_date() {
		return begin_date;
	}
	
	@JsonDeserialize(using = CustomDateTimeDeserializer.class)
	public void setBegin_date(Date begin_date) {
		this.begin_date = begin_date;
	}
	
	
	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public Date getEnd_date() {
		return end_date;
	}
	
	@JsonDeserialize(using = CustomDateTimeDeserializer.class)
	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}
	public String getMxdx() {
		return mxdx;
	}
	public void setMxdx(String mxdx) {
		this.mxdx = mxdx;
	}
	
	@JsonSerialize(using = CustomDateTimeSerializer.class)
	public Date getRelease_time() {
		return release_time;
	}
	@JsonDeserialize(using = CustomDateTimeDeserializer.class)
	public void setRelease_time(Date release_time) {
		this.release_time = release_time;
	}
	
	public String getRelease_ip() {
		return release_ip;
	}
	public void setRelease_ip(String release_ip) {
		this.release_ip = release_ip;
	}
	public String getRelease_per() {
		return release_per;
	}
	public void setRelease_per(String release_per) {
		this.release_per = release_per;
	}
	public String getIsdisplay() {
		return isdisplay;
	}
	public void setIsdisplay(String isdisplay) {
		this.isdisplay = isdisplay;
	}
	public String getIsread() {
		return isread;
	}
	public void setIsread(String isread) {
		this.isread = isread;
	}
	
	
	
	
	
	
	
	

}
