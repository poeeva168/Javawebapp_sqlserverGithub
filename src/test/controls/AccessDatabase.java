package test.controls;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.authority.dao.DataSourceDao;;


public class AccessDatabase {
	private static DataSourceDao ds;
	private static ApplicationContext ctx;
	
	public AccessDatabase(){
		
	};
	
	public AccessDatabase(ApplicationContext ctx){
		ds = ctx.getBean(DataSourceDao.class); 
	};
	
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:config/spring/spring-common.xml");
		AccessDatabase ad = new AccessDatabase(ctx);
		
		String sql = "select SHANGPDM,SHANGPMC,YANSMX,CHICMX from TEMP_PRODUCT_CHANGE  where 1=1 ";
		JdbcTemplate jt_oracle_henlo= ds.getJt_oracle_henlo();
		
		List<Map<String,Object>> list = jt_oracle_henlo.queryForList(sql);
		
		for (Map<String, Object> map : list) {
			String SHANGPDM = map.get("SHANGPDM").toString();
			String SHANGPMC = map.get("SHANGPMC").toString();
			String YANSMX = map.get("YANSMX").toString();
			String CHICMX = map.get("CHICMX").toString();
			
			String[] list_yansmx = YANSMX.split(",");
			String[] list_chicmx = CHICMX.split(",");
			
			for (int i = 0; i < list_yansmx.length; i++) {
				String list_yansmx_child = list_yansmx[i];
				list_yansmx_child = StringUtils.substringBefore(list_yansmx_child, "[");
				
				for (int j = 0; j < list_chicmx.length; j++) {
					
					String list_chicmx_child = list_chicmx[j];
					list_chicmx_child = StringUtils.substringBefore(list_chicmx_child, "[");
					
					String tiaom = SHANGPDM+list_yansmx_child+list_chicmx_child;
					String miaos = SHANGPMC;
					String kuanh = SHANGPDM;
					String yans = list_yansmx_child;
					String chic = list_chicmx_child;
					
					String insert = "insert into TEMP_PRODUCT_END select '"+tiaom+"','"+miaos+"','"+kuanh+"','"+yans+"','"+chic+"' from dual ";
					jt_oracle_henlo.update(insert);					
				}
			}
		}
		
		
		System.out.println("--------------------------END---------------------------");
		
		
		/*JdbcTemplate jt_access_henlo = ds.getJt_access_henlo();
		
		String sql="select count(*) as ct from table_test";
		int i = jt_access_henlo.queryForInt(sql);
		System.out.println("==========================================");
		System.out.println("Access count:"+i);
		
		String insert = "insert into table_test(id,name) values(2,3)";
		jt_access_henlo.execute(insert);*/
		/*Connection con;
		Statement sql; //声明Statement对象
		ResultSet rs;
		
		try {
			
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			con=DriverManager.getConnection("jdbc:odbc:henlo","","");
			sql=con.createStatement();
			String insert = "insert into table_test(id,name) values(2,3)";
			sql.execute(insert);
			sql.close();
			con.close();			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		
	}
	

}
