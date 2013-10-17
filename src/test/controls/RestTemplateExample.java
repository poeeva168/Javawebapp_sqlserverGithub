package test.controls;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.authority.service.impl.ArticleClient;
import com.authority.common.springmvc.SpringContextHolder;
import com.authority.pojo.Article;
import com.authority.pojo.Category;


public class RestTemplateExample {
	
	
	public static void main(String[] args) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:config/spring/spring-common.xml");
		ArticleClient articleClient = applicationContext.getBean("articleClient", ArticleClient.class);
		
		
		//get operate
		Article article = articleClient.getArticle("fun", 1);
		System.out.println("Article: " + article.getBody());

		List<Category> categories = articleClient.getCategories();
		for (Category category : categories) {
			System.out.println("Category: " + category.getName());
		}

		//delete operate
		//articleClient.delCategories();

		//post operate
		//List<Category> categories = articleClient.postCategories();
				
//		ArticleClient articleClient_=(ArticleClient)SpringContextHolder.getBean("articleClient");	
//		Article article_ = articleClient_.getArticle("fun", 1);
//		System.out.println("Article: " + article_.getBody());
		
	}
}
