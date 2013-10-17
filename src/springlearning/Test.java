package springlearning;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {

	@Resource(name="HelloIOC")
	private  HelloIOCImp helloiocimp;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:config/spring/spring-learning.xml");
		HelloIOC helloioc = applicationContext.getBean("HelloIOC", HelloIOC.class);
		helloioc.hello();
		
		/*Test test = applicationContext.getBean("Test", Test.class);
		test.helloiocimp.hello();*/
	}

}
