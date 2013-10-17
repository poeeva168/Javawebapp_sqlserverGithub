package com.authority.common.utils;

public class StringprocessHelper {
	
	public static String String_html(String str){
		
		int start=str.indexOf(">");
		int end  =str.indexOf("<", start);
		if(start>-1&&end>-1)
			return str.substring(start+1, end);
		else
			return str;
	}
}
