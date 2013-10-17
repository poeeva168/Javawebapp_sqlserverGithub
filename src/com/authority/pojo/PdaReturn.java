package com.authority.pojo;

public class PdaReturn {
	
	private String status;
	
	private String message;
	
	
	public PdaReturn(){
		
		
	}
	
	public PdaReturn(String status,String message){
		this.status=status;
		this.message=message;
	}	
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
