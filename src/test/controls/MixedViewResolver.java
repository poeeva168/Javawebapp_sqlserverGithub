package test.controls;

import java.util.Locale;
import java.util.Map;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/** 
* ˵���� ����ͼ������
* 
* @author  ���� 
* @version ����ʱ�䣺2011-8-19 ����09:41:09  
*/ 
public class MixedViewResolver implements ViewResolver{
	private Map<String,ViewResolver> resolvers;

	public void setResolvers(Map<String, ViewResolver> resolvers) {
		this.resolvers = resolvers;
	}
	
	public View resolveViewName(String viewName,Locale locale) throws Exception{
		int n=viewName.lastIndexOf(".");
		if(n!=-1){
			//ȡ����չ��
			String suffix=viewName.substring(n+1);
			//ȡ����Ӧ��ViewResolver
			ViewResolver resolver=resolvers.get(suffix);
			if(resolver==null){
				throw new RuntimeException("No ViewResolver for "+suffix);
			}
			return  resolver.resolveViewName(viewName, locale);
		}else{
			ViewResolver resolver=resolvers.get("jsp");
			return  resolver.resolveViewName(viewName, locale);
		}
	}
}
