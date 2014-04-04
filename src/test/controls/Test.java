package test.controls;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String local="/";
		local = local==null||local.equals("")?"空":local;
		System.out.println("local:"+local);
		
		SimpleDateFormat xlssdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String xlstime =  xlssdf.format(new Date());
		
		System.out.println("xlstime:"+xlstime);
		
		
		SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
		String source ="2013-12-7";
		try {
			String str = format_date.format(format_date.parse(source));
			System.out.println("str:"+str);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		int id = Integer.parseInt("12"+"13");
		
		System.out.println(id);
		
		String str="579101-842110411-09-555555",searchChar=",";
		
		int idx = StringUtils.indexOf(str, searchChar);
		
		str = StringUtils.substring(str, idx+1);
		
		System.out.println("idx:"+idx+" str:"+str);
		
		
		// TODO Auto-generated method stub
		String html="Tomcat服务器监控1.3版 for Win,Linux(原创)";		
		
		int start=html.indexOf(">");
		int end  =html.indexOf("<", start);
		System.out.println("1:"+start);
		System.out.println("2:"+end);
		if(start>-1&&end>-1)
			html=html.substring(start+1, end);
		
		System.out.println(html);
		
		Evaluator eval = new Evaluator(); 
		
        try {
			System.out.println(eval.evaluate("2*3-5/(3-1)"));		
	        System.out.println(eval.evaluate("7 / 2")); 
	        System.out.println(eval.evaluate("7 % 2")); 
	        System.out.println(eval.evaluate("((4 + 3) * -2) * 3")); 
	        System.out.println(eval.evaluate("((4 + 3) * -2) * 3 + sqrt(30)")); 
	        System.out.println(eval.evaluate("((4 + 3) * -2) * 3 + sin(45)")); 
        
        } catch (EvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

}
