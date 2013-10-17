package test.controls;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.stereotype.Controller;
import test.controls.Testpojo;

@Controller
public class Request_control extends DataSourceDao {
	
	@RequestMapping(value="/HelloWorld")
	public ModelAndView helloWorld(HttpServletRequest request,HttpServletResponse response){
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("HelloWorld");
		mav.addObject("Message", "Hello World!");
		
	
		Testpojo.instancece.setUsername("test_username");
		Testpojo.instancece.setPassword("test_password");
		
		mav.addObject("Testpojo", Testpojo.instancece);
		
		System.out.println("helloWorld");
		return mav;
	}
	
	@RequestMapping("/form_action/{pojo}") 
	public ModelAndView form_action(@ModelAttribute("form_model") Testpojo _instance,@PathVariable String pojo, HttpServletRequest request, HttpServletResponse response){
		
		ModelAndView mav = new ModelAndView();
		
		System.out.println("_instance.getUsername():"+_instance.getUsername());
		
		mav.setViewName("HelloWorld");
		mav.addObject("Message", "Hello World!");
		
		mav.addObject("Testpojo", _instance);
		
		return mav;
		
	}
	
	@RequestMapping("/form_action_requestbody") //未成功 
	public ModelAndView form_action(@RequestBody Testpojo _instance,HttpServletRequest request, HttpServletResponse response){
		
		ModelAndView mav = new ModelAndView();
		
		System.out.println("_instance.getUsername():"+_instance.getUsername());
		
		mav.setViewName("HelloWorld");
		mav.addObject("Message", "Hello World!");
		
		mav.addObject("Testpojo", _instance);
		
		return mav;
		
	}
	
	@RequestMapping("/form_action_pojo") 
	public ModelAndView form_action_pojo( Testpojo _instance,HttpServletRequest request, HttpServletResponse response){
		
		ModelAndView mav = new ModelAndView();
		
		System.out.println("_instance.getUsername():"+_instance.getUsername());
		
		mav.setViewName("HelloWorld");
		mav.addObject("Message", "Hello World!");
		
		mav.addObject("Testpojo", _instance);
		
		return mav;
		
	}
	
}
