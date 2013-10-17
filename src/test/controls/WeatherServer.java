package test.controls;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import test.controls.HttpRequester;
import test.controls.HttpRespons;

public class WeatherServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//发送HTTP请求,返回信息
		/*HttpRequester req=new HttpRequester();
		String url="http://webservice.webxml.com.cn/WebServices/WeatherWS.asmx/getWeather";
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("theCityCode", "2106");
		map.put("theUserID", "");
		
		Map<String,String> map_key=new HashMap<String, String>();
		map_key.put("charset", "utf-8");
	//	map_key.put("content-type", "xml");
		
		try {
			HttpRespons Content=req.sendPost(url,map,map_key);

		//	System.out.println(java.net.URLDecoder.decode(Content.getContent()));	
			
			System.out.println(new String(Content.getContent().getBytes("gbk"),"utf-8"));
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		
		 //构造HttpClient的实例  
		  HttpClient httpClient = new HttpClient();  
		  //创建GET方法的实例  
		  String cityname = null;
		  try {
			  cityname = URLEncoder.encode("宁波", "utf-8");
		  	} 
		  catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
			  e1.printStackTrace();
		   }
		  
		  GetMethod getMethod = new GetMethod("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx/getWeatherbyCityName?theCityName="+cityname);  
		  //使用系统提供的默认的恢复策略  
		  getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,  
		    new DefaultHttpMethodRetryHandler());  
		  try {  
		   //执行getMethod  
		   int statusCode = httpClient.executeMethod(getMethod);  
		   if (statusCode != HttpStatus.SC_OK) {  
		    System.err.println("Method failed: "  
		      + getMethod.getStatusLine());  
		   }  
		   //读取内容   
		   byte[] responseBody = getMethod.getResponseBody();  
		   //处理内容  
		   System.out.println(new String(responseBody,"utf-8"));  
		   
		   
		   Document doc;
		   DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); //构建一个新的文档解析器工厂实例
		   dbf.setNamespaceAware(true); //设置当前解析器采用的命名空间为当前使用
		   DocumentBuilder db = dbf.newDocumentBuilder(); //构建一个从XML转换成DOM的文档实例
		   InputStream is =new ByteArrayInputStream(new String(responseBody,"utf-8").getBytes());
		   
		   doc = db.parse(is);        //把XML文档转换成DOM类型文档
		   NodeList nl = doc.getElementsByTagName("string");  //采用DOM文档模型解析文档源
		   String[] sb =new String[nl.getLength()];
		   for (int count = 0; count < nl.getLength(); count++) {
		     Node n = nl.item(count);
		     if(n.getFirstChild().getNodeValue().equals("查询结果为空！")) {
		      sb[count] = new String("#") ;
		      break ;
		     }
		     sb[count]=n.getFirstChild().getNodeValue();
		     
		     
		   }
		   is.close();
		   
		   
		   
		  } catch (HttpException e) {  
		   //发生致命的异常，可能是协议不对或者返回的内容有问题  
		   System.out.println("Please check your provided http address!");  
		   e.printStackTrace();  
		  } catch (IOException e) {  
		   //发生网络异常  
		   e.printStackTrace();  
		  } catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  } catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {  
		   //释放连接  
		   getMethod.releaseConnection();  
		  }
		  
		  
	}

}
