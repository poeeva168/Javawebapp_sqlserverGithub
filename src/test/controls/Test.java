package test.controls;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int id = Integer.parseInt("12"+"13");
		
		System.out.println(id);
		
		
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
