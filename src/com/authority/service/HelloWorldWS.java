package com.authority.service;

import javax.jws.WebService;

@WebService(name="HelloWorldWS",targetNamespace="http://www.henlo.net/HelloWorldWS")
public interface HelloWorldWS {	
	
	public String SayHello(String name);
	
}
