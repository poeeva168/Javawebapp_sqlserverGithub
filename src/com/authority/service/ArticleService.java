package com.authority.service;

import java.util.List;  

import com.authority.pojo.Article;  
import com.authority.pojo.Category;  
  
public interface ArticleService {  
  
    public Article getArticle(String category, int id);  
  
    public Article getArticleSummary(String category, int id);  
  
    public List<Category> loadCategories();  
}
