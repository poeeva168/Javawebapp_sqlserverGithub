package com.authority.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.authority.common.jackjson.JackJson;
import com.authority.common.springmvc.DateConvertEditor;
import com.authority.common.springmvc.SpringContextHolder;
import com.authority.dao.DataSourceDao;

import com.authority.pojo.BaseUsers;
import com.authority.pojo.Criteria;
import com.authority.pojo.ExceptionReturn;
import com.authority.pojo.ExtGridReturn;
import com.authority.pojo.ExtPager;
import com.authority.pojo.ExtReturn;
import com.authority.pojo.Tree;
import com.authority.service.CommonModulesService;
import com.authority.service.WebQudviewService;
import com.authority.web.interseptor.WebConstants;


@Controller
@RequestMapping("/qudview")
public class WebQudviewController  {
	private static final Logger logger = LoggerFactory.getLogger(WebQudviewController.class);
	
	@Autowired
	private WebQudviewService webqudviewservice;
	
	@Autowired
	private CommonModulesService commonmodulesservice;	
	
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;
	
	@Resource(name="datasourcedao")
	private DataSourceDao datasourcedao;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new DateConvertEditor());
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	/**
	 * index
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String webqudview() {
		return "user/webqudview";
	}
	
	/**
	 * 查找所有的消息
	 */
	@RequestMapping(value="/all", method = RequestMethod.POST)
	@ResponseBody
	public Object all(ExtPager pager, HttpSession session, HttpServletRequest request) {
		Criteria criteria = new Criteria();
		// 设置分页信息
		if (pager.getLimit() != null && pager.getStart() != null) {
			criteria.setOracleEnd(pager.getLimit() + pager.getStart());
			criteria.setOracleStart(pager.getStart());
		}
		// 排序信息
		if (StringUtils.isNotBlank(pager.getDir()) && StringUtils.isNotBlank(pager.getSort())) {
			criteria.setOrderByClause(pager.getSort() + " " + pager.getDir());
		}
		
		String yxgsm=request.getParameter("yxgsm");
		if (StringUtils.isNotBlank(yxgsm)) {
			criteria.put("yxgsm", yxgsm);
		}
		
		List<HashMap<String, Object>> list = this.webqudviewservice.selectByDynamicSql(criteria);
		int total = this.webqudviewservice.countByExample(criteria);
		logger.debug("total:{}", total);
		return new ExtGridReturn(total, list);
	}
	
	
	
	/**
	 * 查找系统的所有菜单
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value="/allmodules",method = RequestMethod.POST)	
	public void all(PrintWriter writer, HttpSession session, HttpServletRequest request) throws IOException {
		Criteria criteria = new Criteria();
		criteria.put("table", "WEB_QUDVIEW_TREE");
		criteria.put("isDisplay", "1");
		
		/*String modulename=request.getParameter("modulename");
		if(StringUtils.isNotBlank(modulename)){
			String sql="select * from web_qudview where MAIC_QUANC like '%'||?||'%'";		
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, modulename);
			for (Map<String,Object> map : list) {				
				
			}	
		
		}*/		
		
		Tree tree = this.commonmodulesservice.selectAllModules(criteria);
		String json = JackJson.fromObjectToJson(tree.getChildren());
		// 加入check
		writer.write(json.replaceAll("\"leaf", "\"checked\":false,\"leaf"));
		writer.flush();
		writer.close();
	}

	@RequestMapping(value="/child_list")
	@ResponseBody
	public Object child_list(ExtPager pager, HttpSession session, HttpServletRequest request){
		try {
			
	//		System.out.println("DataSourceDao:"+datasourcedao.getJdbcTemplate().queryForInt("select count(*) from dual")); 
			
			/*ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:config/spring/spring-common.xml");			
			DataSourceDao ds_xml =applicationContext.getBean("datasourcedao",DataSourceDao.class);
			System.out.println("DataSourceDao:"+ds_xml.getJdbcTemplate().queryForInt("select count(*) from dual")); 
			ConfigurableApplicationContext appContext = new FileSystemXmlApplicationContext(applicationContext);
			appContext.refresh();*/
			
			
			/*DataSourceDao ds_context =SpringContextHolder.getBean("datasourcedao");
			System.out.println("DataSourceDao:"+ds_context.getJdbcTemplate().queryForInt("select count(*) from dual")); 
			*/
			
			
			String sssjdm=request.getParameter("sssjdm");
			Map<String,Object> paramMap=new HashMap<String, Object>();
			
			String sql="select * from ("+
				"select ID, YXGSM AS DM, YXGS_QUANC AS MC, YXGS_JIANC AS JC,'00' SSSJDM,'WEB_QUDYXGS' TB  from WEB_QUDYXGS "+
				"union all  "+
				"select ID, FENGSM, FENGS_QUANC, FENGS_JIANC, SSYXGSM,'WEB_QUDFGS' TB from WEB_QUDFGS "+
				"union all "+
				"select ID, PIANQM, PIANQ_QUANC, PIANQ_JIANC, SSFGSM,'WEB_QUDPQ' TB from WEB_QUDPQ "+
				"union all "+
				"select ID, MAICM, MAIC_QUANC, MAIC_JIANC, SSPQM,'WEB_QUDMC' TB from WEB_QUDMC "+
				") A where 1=1 ";
			String sql_ct="";
			if(StringUtils.isNotBlank(sssjdm)){
				sql=sql+" and sssjdm ='"+sssjdm+"'";			
				sql_ct="select count(*) from ("+sql+")";
			}
			
			// 排序信息
			if (StringUtils.isNotBlank(pager.getDir()) && StringUtils.isNotBlank(pager.getSort())) {
				sql=sql+" order by "+pager.getSort().replaceAll("_","")+" "+pager.getDir();				
			}
			
			// 设置分页信息
			if (pager.getLimit() != null && pager.getStart() != null) {
				String OracleEnd=String.valueOf(pager.getLimit() + pager.getStart());
				String OracleStart=String.valueOf(pager.getStart());
				
				String Oracle_Pagination_Head="select y.* from ( select z.*, rownum as oracleStart from (";
				String Oracle_Pagination_Tail=") z where rownum <= to_number(:OracleEnd) ) y where y.oracleStart > to_number(:OracleStart)";

				sql=Oracle_Pagination_Head+sql+Oracle_Pagination_Tail;
				
				paramMap.put("OracleStart", OracleStart);
				paramMap.put("OracleEnd", OracleEnd);
				
			}			
						
			//获取List 
			List list=njdbcTemplate.queryForList(sql, paramMap);
			
			int total=njdbcTemplate.getJdbcOperations().queryForInt(sql_ct) ;
			logger.debug("total:{}", total);
			return new ExtGridReturn(total, list);			
			
		} catch (Exception e) {
			return new ExceptionReturn(e);
		}		
	}
	
	@RequestMapping(value="/child_save")
	@ResponseBody
	public Object child_save(ExtPager pager, HttpSession session, HttpServletRequest request){
		try{
			String id=request.getParameter("id");
			String mc=request.getParameter("mc");
			String jc=request.getParameter("jc");
			String dm=request.getParameter("dm");
			String tb=request.getParameter("tb");
			String sssjdm=request.getParameter("sssjdm");
			
			String sql="",sql_start="",sql_middle="",sql_end="";
			
			Map<String,Object> paramMap=new HashMap<String, Object>();
			paramMap.put("id", id);
			paramMap.put("mc", mc);
			paramMap.put("jc", jc);
			paramMap.put("dm", dm);
			paramMap.put("sssjdm", sssjdm);			
			
			if(StringUtils.isNotBlank(id)&&StringUtils.isNotBlank(tb)){
				sql_middle="(select :dm,:mc,:jc,:sssjdm from dual) ";
				if(tb.equals("WEB_QUDYXGS")){
					sql_start="update WEB_QUDYXGS a set (YXGSM,YXGS_QUANC,YXGS_JIANC,SSSJDM) ";
					sql_end=" where a.id=:id and not exists(" +
							"select 'x' from WEB_QUDYXGS b " +
							"where  a.id!=b.id and b.YXGSM=:dm)";
				}
				else if(tb.equals("WEB_QUDFGS")){
					sql_start="update WEB_QUDFGS a set (FENGSM,FENGS_QUANC,FENGS_JIANC,SSYXGSM) ";
					sql_end=" where a.id=:id and not exists(" +
							"select 'x' from WEB_QUDFGS b " +
							"where  a.id!=b.id and FENGSM=:dm)";
				}
				else if(tb.equals("WEB_QUDPQ")){
					sql_start="update WEB_QUDPQ a set (PIANQM,PIANQ_QUANC,PIANQ_JIANC,SSFGSM) ";
					sql_end=" where a.id=:id and not exists(" +
							"select 'x' from WEB_QUDPQ b " +
							"where  a.id!=b.id and PIANQM=:dm)";
				}
				else if(tb.equals("WEB_QUDMC")){
					sql_start="update WEB_QUDMC a set (MAICM,MAIC_QUANC,MAIC_JIANC,SSPQM) ";
					sql_end=" where a.id=:id and not exists(" +
							"select 'x' from WEB_QUDMC b " +
							"where  a.id!=b.id and MAICM=:dm)";
				}
				
				sql=sql_start+"="+sql_middle+sql_end;
				
			}
			
			if(njdbcTemplate.update(sql, paramMap)>0){
				//int i=1/0; 抛出异常，数据回滚需要执行在  @service 模式下。 
				return new ExtReturn(true, "更新成功");
			}
			else
				return new ExtReturn(false, "更新失败");
		
		} catch (Exception e) {
			return new ExceptionReturn(e);
		}
	}
	
	@RequestMapping(value="/child_del")
	@ResponseBody
	public Object child_del(ExtPager pager, HttpSession session, HttpServletRequest request){
		try {
			
			
			
			
			return null;
		} catch (Exception e) {
			return new ExceptionReturn(e);
		}

	}
	
	@RequestMapping(value="/Report_maic_count")
	@ResponseBody
	public void Report_maic_count(ExtPager pager, HttpSession session, HttpServletRequest request,HttpServletResponse response) throws IOException{

			PrintWriter writer = response.getWriter();
		try {
			
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("yxgsm", request.getParameter("param"));
			// JasperPrint jasperPrint = new
			// JasperPrintWithConnection(reportFilePath, params,
			// con).getJasperPrint();

			String JASPER_FILE_NAME = request.getSession().getServletContext()
					.getRealPath("/WEB-INF/reports/Report_maic_count.jasper");
			// JasperReport jasperReport =
			// (JasperReport)JRLoader.loadObject(JASPER_FILE_NAME);
			File reportFile = new File(JASPER_FILE_NAME);
			InputStream in = new FileInputStream(reportFile);
			Connection conn = jdbcTemplate.getDataSource().getConnection();
			JasperPrint print = JasperFillManager.fillReport(in, params,conn);
			conn.close();
			// 使用JRHtmlExproter导出Html格式
			JRHtmlExporter exporter = new JRHtmlExporter();
			
			//设置图片文件存放路径，此路径为服务器上的绝对路径 			
		//	System.out.println(request.getSession().getServletContext().getRealPath("/"));
			
			String imageDIR = request.getSession().getServletContext().getRealPath("/")+"/resources/report_temp_file/";
			exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, imageDIR);
			
			//设置图片请求URI 
			
			String imageURI =  request.getContextPath() + "/resources/report_temp_file/"; 
			exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "../servlets/image?image="); 
			
			//设置导出图片到图片存放路径 
			exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.TRUE); 
			exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
			
			//设置导出对象 
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
			
			//设置导出方法 
			exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, response.getWriter());
			
			//设置HTTP Head 
			response.setContentType("text/html");
			request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE,print);

			// 导出
			exporter.exportReport();
			
		} catch (Exception e) {
			e.printStackTrace();
			writer.write("{success:false,errors:{msg:'打开失败'}}");
			writer.close();
			return;
		}
		// writer.write("{success:false,errors:{msg:'保存失败'}}");
		writer.close();

	}	
		
}
