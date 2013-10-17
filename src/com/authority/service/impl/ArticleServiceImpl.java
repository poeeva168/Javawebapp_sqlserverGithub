package com.authority.service.impl;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.authority.pojo.Article;
import com.authority.pojo.Category;
import com.authority.service.ArticleService;



@Service("articleService")
public class ArticleServiceImpl implements ArticleService {

	@Override
	public Article getArticle(String category, int id) {
		return new Article(1, "My Article", "Steven Haines", new Date(), "A facinating article",
				"Wow, aren't you enjoying this article?");

	}

	@Override
	public Article getArticleSummary(String category, int id) {
		return new Article(1, "My Article", "Steven Haines", new Date(), "A facinating article");
	}

	public List<Category> loadCategories() {
		List<Category> categories = new ArrayList<Category>();
		categories.add(new Category("fun"));
		categories.add(new Category("work"));
		return categories;
	}

}

