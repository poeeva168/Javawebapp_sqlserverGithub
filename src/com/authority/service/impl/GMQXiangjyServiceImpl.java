package com.authority.service.impl;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.authority.service.GMQXiangjyService;

@Service
public class GMQXiangjyServiceImpl implements GMQXiangjyService {

	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;
	
	@Override
	public int query_count(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Map<String, Object>> query_list(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String save(List list) {
		String query="",update ="",insert = "",delete="",B_PO_BOXNO_ID_="",procedure="";
		int result = 0;
		String msg ="";
		String IS_CHANG ="N";
		int TOT_QTY=0,TOT_LINES=0;
		try{
			// ============更新 配货发货箱 =========BEGIN
			for (Object object : list) {
				Map<String,Object> list_child = JSON.parseObject(object.toString());
				String BOXNO = list_child.get("BOXNO")==null?"":list_child.get("BOXNO").toString();
				String B_PO_BOXNO_ID  = list_child.get("B_PO_BOXNO_ID")==null?"":list_child.get("B_PO_BOXNO_ID").toString();
				if(B_PO_BOXNO_ID!=null&&!B_PO_BOXNO_ID.equals(""))
					B_PO_BOXNO_ID_=B_PO_BOXNO_ID;
				
				String M_PRODUCT_ALIAS_NO = list_child.get("M_PRODUCT_ALIAS_NO")==null?"":list_child.get("M_PRODUCT_ALIAS_NO").toString();
				String M_PRODUCTALIAS_ID  = list_child.get("M_PRODUCTALIAS_ID")==null?"":list_child.get("M_PRODUCTALIAS_ID").toString();
				String QTY_QR =list_child.get("QTY_QR")==null?"0":list_child.get("QTY_QR").toString();
				String QTY = list_child.get("QTY")==null?"0":list_child.get("QTY").toString();
				Map<String,Object> paramMap = new HashMap<String, Object>();
				
				TOT_QTY = TOT_QTY + Integer.parseInt(QTY_QR);
				TOT_LINES++;
				
				if(!QTY_QR.equals(QTY))
					IS_CHANG="Y";
				
				if(QTY.toString().equals("0")&&!QTY_QR.equals("0")){ //执行插入操作
					insert = "insert into B_PO_BOXITEM(ID, AD_CLIENT_ID, AD_ORG_ID, B_PO_BOX_ID, B_SO_ID, M_PRODUCT_ID, M_PRODUCTALIAS_ID, M_ATTRIBUTESETINSTANCE_ID, QTY, OWNERID, MODIFIERID, CREATIONDATE, MODIFIEDDATE, ISACTIVE, B_PO_BOXNO_ID) " +
							 "select GET_SEQUENCES('B_PO_BOXITEM') ID,AD_CLIENT_ID, AD_ORG_ID, B_PO_BOX_ID, B_SO_ID, B.M_PRODUCT_ID, B.ID M_PRODUCTALIAS_ID, B.M_ATTRIBUTESETINSTANCE_ID, "+QTY_QR+" QTY, OWNERID, MODIFIERID, CREATIONDATE, MODIFIEDDATE, ISACTIVE, B_PO_BOXNO_ID "+
							 "from B_PO_BOXITEM A  "+
							 "LEFT JOIN (SELECT ID,M_PRODUCT_ID,M_ATTRIBUTESETINSTANCE_ID FROM M_PRODUCT_ALIAS WHERE NO='"+M_PRODUCT_ALIAS_NO+"' ) B ON 1=1 "+
							 "where exists(select 'x' from  B_PO_BOXNO B WHERE A.B_PO_BOXNO_ID = B.ID AND B.BOXNO='"+BOXNO+"' ) AND ROWNUM=1 ";
					jdbcTemplate.update(insert);
				}else if (QTY_QR.equals("0")){
					insert = "insert into B_PO_BOXITEM_BACK select * from B_PO_BOXITEM where B_PO_BOXNO_ID='"+B_PO_BOXNO_ID+"' and M_PRODUCTALIAS_ID='"+M_PRODUCTALIAS_ID+"'";
					delete = "delete from B_PO_BOXITEM where B_PO_BOXNO_ID='"+B_PO_BOXNO_ID+"' and M_PRODUCTALIAS_ID='"+M_PRODUCTALIAS_ID+"'";
					jdbcTemplate.update(insert);
					jdbcTemplate.update(delete);
				}else {					
					update = "update B_PO_BOXITEM a set a.QTY="+QTY_QR+" where a.B_PO_BOXNO_ID='"+B_PO_BOXNO_ID+"' and a.M_PRODUCTALIAS_ID='"+M_PRODUCTALIAS_ID+"'";
					jdbcTemplate.update(update);
				}
			}
			//更新B_PO_BOXNO 
			update = "update B_PO_BOXNO set TEST_STATUS=2,TOT_QTY="+TOT_QTY+",TOT_LINES="+TOT_LINES+" where ID="+B_PO_BOXNO_ID_;
			result = jdbcTemplate.update(update);
			
			//更新M_ISSUE_BOX
			if(IS_CHANG.equals("Y")){
				update = "update M_ISSUE_BOX set IS_CHANG='Y' where B_PO_BOXNO_ID="+B_PO_BOXNO_ID_;
				jdbcTemplate.update(update);				
			}
				
			
			query = "select max(M_ISSUE_TASK_ID) from M_ISSUE_BOX where B_PO_BOXNO_ID="+B_PO_BOXNO_ID_;
			
			final String M_ISSUE_TASK_ID = jdbcTemplate.queryForObject(query, String.class);
			
			//执行存储过程 m_sale_submit 
			procedure = "{call m_issue_fa_submit(?,?,?)}";  
			
			@SuppressWarnings("unchecked")
			Map<String,Object> map_m_issue_fa_submit = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
	            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
	                cs.setInt(1, Integer.parseInt(M_ISSUE_TASK_ID));
	                cs.registerOutParameter(2,Types.NUMERIC);//输出参数  
	                cs.registerOutParameter(3,Types.VARCHAR);//输出参数  
	                cs.execute();
	                Map<String,Object> map = new HashMap<String, Object>();  
	                map.put("r_code", cs.getInt(2));
	                map.put("r_message", cs.getString(3));
	                return map;
	            }
	        }); 
			
			
			// ============更新 配货发货箱 =========END
			
			/*// ============检测 一个配货发货单下的 所有箱号 是否 IS_CHANG 都已经更新为 Y
			query = "select count(*) from M_ISSUE_BOX a where exists( "+
					"select 'x' from M_ISSUE_BOX b where A.M_ISSUE_TASK_ID = B.M_ISSUE_TASK_ID and B.B_PO_BOXNO_ID='"+B_PO_BOXNO_ID_+"' "+
					") and A.IS_CHANG='N'" ;
			
			int IS_CHANG_N = jdbcTemplate.queryForInt(query);
			
			if(IS_CHANG_N==0){ // 对应的箱号都已经被更新为 Y
				//读取所有的 M_SALE_ID 
				query = "select M_SALE_ID from M_ISSUE_BOX a where exists( "+
						"select 'x' from M_ISSUE_BOX b where A.M_ISSUE_TASK_ID = B.M_ISSUE_TASK_ID and B.B_PO_BOXNO_ID='"+B_PO_BOXNO_ID_+"' "+
						") group by M_SALE_ID " ;
				
				List<Map<String,Object>> list_m_sale_id = jdbcTemplate.queryForList(query);
				
				//更新 M_SALE 表中 ISACTIVE ='Y' 
				for (Map<String, Object> map_m_sale_id : list_m_sale_id) {
					final String str_m_sale_id = map_m_sale_id.get("M_SALE_ID").toString();
					update = "update m_sale set ISACTIVE ='Y' where id= '"+str_m_sale_id+"'";
					jdbcTemplate.update(update);
					//执行存储过程 m_sale_submit 
					procedure = "{call m_sale_submit(?,?,?)}"; 
					
					@SuppressWarnings("unchecked")
					Map<String,Object> map_m_sale_submit = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			                cs.setInt(1, Integer.parseInt(str_m_sale_id));
			                cs.registerOutParameter(2,Types.NUMERIC);//输出参数  
			                cs.registerOutParameter(3,Types.VARCHAR);//输出参数  
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", cs.getInt(2));
			                map.put("r_message", cs.getString(3));
			                return map;
			            }
			        }); 
					
					//执行存储过程 m_out_submit
					procedure = "{call m_out_submit(?,?,?)}"; 
					
					@SuppressWarnings("unchecked")
					Map<String,Object> map_m_out_submit = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			                cs.setInt(1, Integer.parseInt(str_m_sale_id+"65"));
			                cs.registerOutParameter(2,Types.NUMERIC);//输出参数  
			                cs.registerOutParameter(3,Types.VARCHAR);//输出参数  
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", cs.getInt(2));
			                map.put("r_message", cs.getString(3));
			                return map;
			            }
			        });
					
				}
				
			}*/
			result = 1;
			
		} catch(Exception e){
			System.out.println(e);
			String str = "可能在调用存储过程时出错,请联系管理员";
			msg = e.toString();
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "01" : msg;
	}

}
