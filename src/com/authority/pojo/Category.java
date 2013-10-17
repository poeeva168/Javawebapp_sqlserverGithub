package com.authority.pojo;

import com.thoughtworks.xstream.annotations.XStreamAlias;  

@XStreamAlias("category")  
public class Category {  
    private String name;  
  
    public Category() {  
    }  
  
    public Category(String name) {  
        this.name = name;  
    }  
  
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
}
