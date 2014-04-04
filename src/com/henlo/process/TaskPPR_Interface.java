package com.henlo.process;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.alibaba.fastjson.JSON;
import com.alipay.config.AlipayConfig;
import com.alipay.sign.MD5;
import com.alipay.util.AlipayCore;
import com.alipay.util.httpClient.HttpProtocolHandler;
import com.alipay.util.httpClient.HttpRequest;
import com.alipay.util.httpClient.HttpResponse;
import com.alipay.util.httpClient.HttpResultType;
import com.authority.common.springmvc.DateConvertEditor;
import com.authority.common.utils.FileOperateUtil;
import com.authority.common.utils.PoiHelper;
import com.authority.common.utils.WebUtils;
import com.authority.pojo.Table;
import com.authority.service.impl.EmaxInterfaceServiceImpl;
import com.authority.web.controller.EmaxInterfaceController;
import com.sun.jndi.cosnaming.ExceptionMapper;



@Service
public class TaskPPR_Interface {

	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;
	
	@Value("${bosinterface.filesavepath}")
	private String filesavepath;
	
	@Autowired
	private EmaxInterfaceController emaxinterfacecontroller;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new DateConvertEditor());
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	private static final Logger logger = LoggerFactory.getLogger(TaskPPR_Interface.class);
	
	/**
	 * 发送请求，下载业务数据
	 */
	public void datadownload(){
		String query = "",strResult ="",ExceptionMsg="",update ="";
		try{
			query = "select * from BASE_Interface_Customer where  isactive='Y' ";
			List<Map<String,Object>> CusList = jdbcTemplate.queryForList(query);
			for (Map<String, Object> CusMap : CusList) {
				//检测是否执行计划：  0 未在执行   1 已经在执行但未执行完
				query = "select count(*) from " +
						"Base_Interface_Task a " +
						"where TaskName='datadownload' and Isactive='Y' and Status='0' and CusCode = :Code and " +
						"not exists(" +
						"	select 'x' from Base_Interface_Task b " +
						"	where a.TaskOrder > b.TaskOrder and b.Status='1' and b.Isactive='Y' and a.CusCode = b.CusCode " +
						")" ;
				
				int count = njdbcTemplate.queryForInt(query, CusMap);
				if(count==0)
					return ;
				else{
					//更新任务状态为 1 正在执行中
					update ="update Base_Interface_Task set Status='1' where TaskName='datadownload' and Isactive='Y' and CusCode = :Code ";
					njdbcTemplate.update(update, CusMap);
				}
				
				HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler.getInstance();
				HttpRequest request = new HttpRequest(HttpResultType.BYTES);
				//设置编码集
		        request.setCharset(AlipayConfig.input_charset);
		        HttpResponse response;
		        Map<String, String> paramMap = new HashMap<String, String>();
		        WebUtils web = new WebUtils();
		        String account = CusMap.get("Account").toString();
		        String password = CusMap.get("Password").toString();
		        String url = CusMap.get("Url").toString();
		        String webserver = CusMap.get("Webserver").toString();
		        String dim = CusMap.get("Dim").toString();
		        //获取条件参数
		        query = "select * from BASE_Interface_Condition where isactive='Y' and CusCode = :Code ";
		        List<Map<String,Object>> ConditionMap = njdbcTemplate.queryForList(query,CusMap);
		        for (Map<String, Object> map : ConditionMap) {
		        	paramMap.clear();
		        	String condition = map.get("CONTENT")==null?"":map.get("CONTENT").toString();
		        	String Type = map.get("TYPE").toString();
		        	
		        	paramMap.put("dim", dim);
		        	paramMap.put("type", Type.toUpperCase());
		        	paramMap.put("condition", condition);
		        	paramMap.put("account", account);
		        	paramMap.put("password", DigestUtils.md5Hex(password));
		        	Map<String, String> sPara = buildRequestPara(paramMap);
		        	
		        	request.setParameters(generatNameValuePair(sPara));
		        	request.setUrl(url+"/filedownload");
		        	response = httpProtocolHandler.execute(request,"","");
		        	if (response == null) {
			        	System.out.println("response is null");
			        	//发送Email,某个请求没有正确获取
			        	ExceptionMsg = ExceptionMsg +CusMap.get("Code").toString()+" "+ Type + "(远程服务请求失败)<br>" ;
			        }else{
			        	strResult = response.getStringResult();
			        	//字符串转JSON
			        	Map<String,Object> RepMap = JSON.parseObject(strResult);
			        	if((Boolean) RepMap.get("success")){
			        		//结果为真 读取文件路径,再次发送请求 下载文件
			        		String fileURL = webserver+RepMap.get("msg").toString();
			        		String saveURL = filesavepath;
			        		String filePath = FileOperateUtil.downloadfile(saveURL, fileURL);
			        		File NewFile = new File(filePath);
			        		if(NewFile.exists()){
			        			logger.info("文件下载:"+NewFile.getAbsolutePath().toString());
			        			//发送删除文件请求----------------------begin
			        			paramMap.clear();
			        			String FILENAME = NewFile.getName();
			        			paramMap.put("filename", FILENAME);
			        			paramMap.put("account", account);
			    	        	paramMap.put("password", DigestUtils.md5Hex(password));
			        			sPara.clear();
			        			sPara = buildRequestPara(paramMap);
			        			request.setParameters(generatNameValuePair(sPara));
			        			request.setUrl(url+"/filedelete");
			        			httpProtocolHandler.execute(request,"","");
			        			//--------------------------------------end
			        			//修改文件名
			        			filePath = NewFile.getParent()+File.separator+CusMap.get("Code")+"_"+NewFile.getName();
			        			File RenameFile = new File(filePath);
			        			NewFile.renameTo(RenameFile);			        			
			        			
			        		}else{
			        			//发送Email ， 文件生成失败        			
			        			ExceptionMsg = ExceptionMsg + fileURL+CusMap.get("Code").toString()+" "+"(文件下载失败)<br>";
			        		}
			        	}else{
			        	//	System.out.println("数据生成异常,请重新提交请求");
			        		ExceptionMsg = ExceptionMsg + CusMap.get("Code").toString()+" "+Type+"(远程数据生成异常,如多次发生,请联系对方管理员)<br>";
			        	}
			        }
				}
				
			}
				        
	    //    System.out.println("Response ：\n"+strResult);
	        
		}catch(Exception e){
			logger.error(e.toString());
			ExceptionMsg = ExceptionMsg + " 异常信息："+ e.toString()+"<br>";
		}finally{
			//执行完毕后更新状态为 0 待执行状态
	        update ="update Base_Interface_Task set Status='0' where TaskName='datadownload' and Isactive='Y' ";
			jdbcTemplate.update(update);
			
			//如有异常信息，发送Email 提醒
			if(!ExceptionMsg.trim().equals("")){
				query = "select * from BASE_Interface_Email where isactive='Y' ";
				List<Map<String, Object>> EmailMap = jdbcTemplate.queryForList(query);
				String title = "EMAX--BOS Interface <datadownload> Failed (Release)";
				WebUtils webutils = new WebUtils();
				try {
					for (Map<String, Object> map : EmailMap) {
						String address = map.get("MAIL").toString();
						webutils.execSend(address, title, ExceptionMsg);
					}
				} catch (Exception e) {
					logger.error(e.toString());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 业务单据数据处理,
	 * 轮询指定目录下的Excel，根据文件名称 执行相应的文件插入操作
	 */
	public void dataprocess(){
		File dir = new File(filesavepath);
		File[] arrFiles = dir.listFiles();
		String query = "",exceptionMsg="" ,update="";
		try{
			query = "select * from BASE_Interface_Customer where  isactive='Y' ";
			List<Map<String,Object>> CusList = jdbcTemplate.queryForList(query);
			for (Map<String, Object> CusMap : CusList) {
				query = "select count(*) from " +
						"Base_Interface_Task a " +
						"where TaskName='dataprocess' and Isactive='Y' and Status='0' and CusCode = :Code and " +
						"not exists(" +
						"	select 'x' from Base_Interface_Task b " +
						"	where a.TaskOrder > b.TaskOrder and b.Status='1' and b.Isactive='Y' and a.CusCode = b.CusCode " +
						")" ;
				
				int count = njdbcTemplate.queryForInt(query,CusMap);
				if(count==0)
					return ;
				else{
					//更新任务状态为 1 正在执行中
					update ="update Base_Interface_Task set Status='1' where TaskName='dataprocess' and Isactive='Y' and CusCode = :Code ";
					njdbcTemplate.update(update,CusMap);
				}
				for (File file : arrFiles) {
					if(!file.isFile())
						continue;
					String filePath = file.getAbsolutePath();
					String fileName = file.getName();
					query = "select * from BASE_Interface_Condition where isactive='Y' and CusCode = :Code";
					List<Map<String,Object>> Tablelist = njdbcTemplate.queryForList(query,CusMap);
					boolean exists = false  ; //文件是否存在
					boolean flag = true ; //文件后缀名格式
					boolean time_check = false ; //时间是否允许
					String TableName = "";
					//检测文件是否存在
					for (Map<String, Object> Tablemap : Tablelist) {
						TableName = Tablemap.get("TYPE").toString();
						if(fileName.toUpperCase().contains(CusMap.get("Code").toString().toUpperCase()+"_"+TableName.toUpperCase())){
							exists = true;
							break;
						}
					}
					//判断文件格式
					String fileType = StringUtils.substringAfterLast(file.getName(), ".");
					if(!fileType.toLowerCase().equals("xls")&&!fileType.toLowerCase().equals("xlsx")){
						flag = false ;
						exceptionMsg = exceptionMsg + file.getName()+"文件格式不正确(xls,xlsx)<br>";
						continue;
					}
					Calendar cal=Calendar.getInstance();
					cal.setTime(new Date());
					int nowminute = cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);
					WebUtils web = new WebUtils();
					String time = web.readValue("config/others/config.properties","Emax.Stock_Update_Time");
					String[] setime= time.split("-");
					String  starttime[] = setime[0].split(":");
					int startminute = Integer.parseInt(starttime[0])*60+Integer.parseInt(starttime[1]);
					String  endtime[] = setime[1].split(":");
					int endminute = Integer.parseInt(endtime[0])*60+Integer.parseInt(endtime[1]);
					
					if(exists&&flag){
						if(TableName.equalsIgnoreCase("V_FA_STORAGE")){ //库存更新程序 时间控制
							if(nowminute>=startminute&&nowminute<=endminute){
								time_check = true ;
							}else{
								//删除文件 为防止文件一直在下载 累加
								file.delete();
							}
						}else{ //其他数据更新  时间控制
							if(nowminute>endminute){
								time_check = true ;
							}else{
								//删除文件 为防止文件一直在下载 累加
								file.delete();
							}
						}
					}
					
					if(exists&&flag&&time_check){
						//读取文件字段映射表，检测是否合标准
						Map<String,Object> fieldMap = new HashMap<String, Object>();
						query = "select * from BASE_Interface_Mapping where isactive='Y' and type = :Type and CusCode = :Code ";
						Map<String,Object> ParamMapping = new HashMap<String, Object>();
						ParamMapping.put("Type", TableName);
						ParamMapping.put("Code", CusMap.get("Code").toString());
						List<Map<String,Object>> ListMapping = njdbcTemplate.queryForList(query, ParamMapping);
						for (Map<String, Object> map2 : ListMapping) {
							String Key = map2.get("localfield").toString().toUpperCase();
							String value = map2.get("targetfield").toString().toUpperCase();
							fieldMap.put(Key, value);
						}
						
						String result = emaxinterfacecontroller.MethodMapping(file, fieldMap, "admin");
						if(!result.equals(""))
							exceptionMsg = result;
					}
					
				}
			}
			
			
		}catch(Exception e){
			logger.error(e.toString());
			exceptionMsg = exceptionMsg+ e.toString();
		}finally{
			//更新任务状态为 0 执行完毕
			update ="update Base_Interface_Task set Status='0' where TaskName='dataprocess' and Isactive='Y' ";
			jdbcTemplate.update(update);
			
			//发送Email 
			if(!exceptionMsg.equals("")){
				query = "select * from BASE_Interface_Email where isactive='Y' ";
				List<Map<String, Object>> EmailMap = jdbcTemplate.queryForList(query);
				String title = "EMAX--BOS Interface <dataprocess> Failed (Release)";
				WebUtils webutils = new WebUtils();
				try {
					for (Map<String, Object> map : EmailMap) {
						String address = map.get("MAIL").toString();
						webutils.execSend(address, title, exceptionMsg);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error(e.toString());
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	/**
     * 生成要请求的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    private static Map<String, String> buildRequestPara(Map<String, String> sParaTemp) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
        //生成签名结果
        String mysign = buildRequestMysign(sPara);

        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type", AlipayConfig.sign_type);

        return sPara;
    }
    
    /**
     * 生成签名结果
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
	public static String buildRequestMysign(Map<String, String> sPara) {
    	String prestr = AlipayCore.createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String mysign = "";
        if(AlipayConfig.sign_type.equals("MD5") ) {
        	mysign = MD5.sign(prestr, AlipayConfig.key, AlipayConfig.input_charset);
        }
        //mysign ="gur8bebpnew403u7lnan1jtfux9smtva";
        return mysign;
    }
	
	/**
     * MAP类型数组转换成NameValuePair类型
     * @param properties  MAP类型数组
     * @return NameValuePair类型数组
     */
    private static NameValuePair[] generatNameValuePair(Map<String, String> properties) {
        NameValuePair[] nameValuePair = new NameValuePair[properties.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            nameValuePair[i++] = new NameValuePair(entry.getKey(), entry.getValue());
        }

        return nameValuePair;
    }
	

}
