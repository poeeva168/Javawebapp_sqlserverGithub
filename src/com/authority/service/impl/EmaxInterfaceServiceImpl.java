package com.authority.service.impl;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.authority.common.utils.WebUtils;
import com.authority.service.EmaxInterfaceService;
import com.sun.faces.renderkit.html_basic.HtmlBasicRenderer.Param;

@Service
public class EmaxInterfaceServiceImpl implements EmaxInterfaceService {

	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;
	
	private static final Logger logger = LoggerFactory.getLogger(EmaxInterfaceServiceImpl.class);
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusSale(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
			//Tbussale 抬头明细表
			for (String[] strings : DataArray) {
				String SaleType =InterfaceValue("M_SALE","SaleType",strings[Integer.parseInt(fieldmatchMap.get("SALETYPE").toString())],false);
				String OutType =InterfaceValue("M_SALE","OutType",strings[Integer.parseInt(fieldmatchMap.get("OUTTYPE").toString())],false);
				String SaleFlag =InterfaceValue("M_SALE","SaleFlag",strings[Integer.parseInt(fieldmatchMap.get("SALEFLAG").toString())],false);
				String Orig = InterfaceValue("C_STORE","Store",strings[Integer.parseInt(fieldmatchMap.get("ORIG").toString())],true);
				String Dest = InterfaceValue("C_STORE","Store",strings[Integer.parseInt(fieldmatchMap.get("DEST").toString())],true);
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[Integer.parseInt(fieldmatchMap.get("MASTERID").toString())]);
				paramMap.put("BillDate", format_date.format(format_date.parse(strings[Integer.parseInt(fieldmatchMap.get("BILLDATE").toString())])));
				paramMap.put("SaleType", SaleType);
				paramMap.put("OutType",  OutType);
				paramMap.put("SaleFlag", SaleFlag);
				paramMap.put("Orig", Orig);
				paramMap.put("Dest", Dest);
				paramMap.put("DateOut", strings[Integer.parseInt(fieldmatchMap.get("DATEOUT").toString())]);
				paramMap.put("DateIn", strings[Integer.parseInt(fieldmatchMap.get("DATEIN").toString())]);
				paramMap.put("Opr", strings[Integer.parseInt(fieldmatchMap.get("OPR").toString())]);
				paramMap.put("OpDate", strings[Integer.parseInt(fieldmatchMap.get("OPDATE").toString())]);
				paramMap.put("Sku", strings[Integer.parseInt(fieldmatchMap.get("SKU").toString())]);
				paramMap.put("PreQty", strings[Integer.parseInt(fieldmatchMap.get("PREQTY").toString())]);
				paramMap.put("QtyOut", strings[Integer.parseInt(fieldmatchMap.get("QTYOUT").toString())]);
				paramMap.put("QtyIn", strings[Integer.parseInt(fieldmatchMap.get("QTYIN").toString())]);
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				//上下级经销商产生销售单
				query = "select count(*) from TDefStore a where Store = :Orig and  "+
						"BuyerId in ( "+
						"  select BuyerUp from TDefBuyer b where b.BuyerId in ( "+
						"    select c.BuyerId from TDefStore c where c.Store= :Dest and Closed=0 "+
						"  ) "+
						")  and Closed=0 ";
				
				if(njdbcTemplate.queryForInt(query, paramMap)==0){
					insert ="insert into TBusSale_TMP(MasterId, BillDate, SaleType, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :SaleType, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '2', '经销商关系不符', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
					continue;
				}
				
				//盘点日期判断
				query = "select count(*) from TDefStore " +
						"where (store = :Orig and isnull(panddate,'1990-01-01')<:DateOut) or " +
						"(store = :Dest and isnull(panddate,'1990-01-01')<:DateIn) ";
				if(njdbcTemplate.queryForInt(query, paramMap)<2){
					insert ="insert into TBusSale_TMP(MasterId, BillDate, SaleType, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :SaleType, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '2', '与盘点日期不符', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
					continue;
				}
				
				//明细插入重复判断
				insert ="insert into TBusSale_TMP(MasterId, BillDate, SaleType, OutType, SaleFlag, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate, :SaleType, :OutType, :SaleFlag, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '0', '', :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusSale_TMP where (  (MasterId = :MasterId and status ='1' ) )  )";
				// (MasterId = :MasterId and Sku = :Sku and status !='2') or
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusSale_TMP(MasterId, BillDate, SaleType, OutType, SaleFlag, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :SaleType, :OutType, :SaleFlag, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '2', '销售明细重复插入', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
					
			//读取临时表 插入到正式表 ， 同步
			//获取masterid  		     
			query = "select distinct MasterId,BillDate,SaleType,OutType,SaleFlag,Orig,Dest,Opr,OpDate,DateOut,DateIn " +
					"from TBusSale_TMP where status = 0 and addwho ='"+Account+"'";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			
			//读取是否执行相应存储过程
			boolean PChkSale = true ,PChgSale= true ,PAcpSale= true;
			query = "select * from BASE_Interface_Procedure where TableName='TBusSale' and Run='N' ";
			List<Map<String,Object>> ProcedureMap = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map : ProcedureMap) {
				String ProName = map.get("PRONAME")==null?"":map.get("PRONAME").toString();
				if(ProName.toUpperCase().equals("PCHKSALE"))
					PChkSale = false ;
				if(ProName.toUpperCase().equals("PCHGSALE"))
					PChgSale = false ;
				if(ProName.toUpperCase().equals("PACPSALE"))
					PAcpSale = false ;
			}
			
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
				String SaleType = map_data.get("SaleType").toString();
				String OutType = map_data.get("OutType").toString();
				String Opr = map_data.get("Opr").toString();
				String OpDate = map_data.get("OpDate").toString();
				String Orig = map_data.get("ORIG").toString();
				String Dest = map_data.get("DEST").toString();
				String DateOut = map_data.get("DATEOUT").toString();
				String DateIn = map_data.get("DATEIN").toString();
				
				String MasterId_sys = "";
				Boolean procedure_exec = true ;
				synchronized (this) {
					//获取masterid
					query = "select MasterId from FGetNewMasterId('"+Orig+"','TBusSale')";
					MasterId_sys = jdbcTemplate.queryForObject(query, String.class);
					//插入数据到 抬头
					insert = "insert into TBusSale(MasterId,BillDate,SaleType,OutType,Orig,Dest,Opr,OpDate,DateOut,DateIn,Remark) " +
							 "select :MasterId_sys,:BillDate,:SaleType,:OutType,:Orig,:Dest,:Opr,:OpDate,:DateOut,:DateIn,:Remark " +
							 "where not exists( select 'x' from TBusSale where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("SaleType", SaleType);
					paramMap.put("OutType", OutType);
					paramMap.put("Orig", Orig);
					paramMap.put("Dest", Dest);
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					paramMap.put("DateOut", DateOut);
					paramMap.put("DateIn", DateIn);
					paramMap.put("Remark", "数据接口导入"+MasterId);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusSale_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0  and BillDate = :BillDate and Orig = :Orig and Dest = :Dest";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusSale_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusSale失败' " +
								 "where MasterId = :MasterId and status =0  and BillDate = :BillDate and Orig = :Orig and Dest = :Dest";
					}
					njdbcTemplate.update(update, paramMap);
					
				}
				
				if(!procedure_exec)
					continue;
				
				//插入数据到明细表
				insert = "insert into TBusSaleDt(MasterId,Sku,PreQty,QtyOut,QtyIn,DPrice,SaleFlag) " +
						 "select a.masterid_sys,a.Sku,a.PreQty,a.QtyOut,a.QtyIn,c.Price,a.SaleFlag " +
						 "from TBusSale_tmp a,TDefSku b,TDefStyle c "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.MasterId = :MasterId and a.BillDate = :BillDate and a.SaleType =:SaleType and a.Orig = :Orig and a.Dest = :Dest " +
						 "and not exists( " +
						 "select 'x' from TBusSaleDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				paramMap.put("BillDate", BillDate);
				paramMap.put("SaleType", SaleType);
				paramMap.put("Orig", Orig);
				paramMap.put("Dest", Dest);	
				if(njdbcTemplate.update(insert, paramMap)==0){
					//如果没有明细插入，则不执行后续程序
					paramMap.put("MasterId_sys", MasterId_sys);
					
					update = "update  TBusSale_TMP set status=2,note='插入TBusSaleDt失败' " +
							 "where MasterId = :MasterId and BillDate = :BillDate and SaleType =:SaleType and Orig = :Orig and Dest = :Dest and status = 1 ";
					njdbcTemplate.update(update, paramMap);
					
					delete ="delete from TBusSale where MasterId = :MasterId_sys and exists(" +
							"select 'x' from TBusSale_tmp where TBusSale.MasterId = TBusSale_tmp.MasterId_sys )";
					
					njdbcTemplate.update(delete, paramMap);
					
					continue;
				}
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				
				if(PChkSale){
					// 销售单审核存储过程	
					procedure = "{call PChkSale(?,?)}";  		
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PChkSale = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			                cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        }); 
				}
				
				if(PChgSale){
					// 销售单记账存储过程	
					procedure = "{call PChgSale(?,?)}";
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PChgSale = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			            	cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        });
				}
				
				if(PAcpSale){
					// 销售单验收存储过程	
					procedure = "{call PAcpSale(?,?)}";  		
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PAcpSale = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			            	cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        });
				}
			}
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员(TBusSale)";
			msg = str ;
			logger.error("TBusSale:"+e.toString());
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusRetail(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
			
			//TBusRetail 抬头明细表
			for (String[] strings : DataArray) {
				String RetailType =InterfaceValue("M_RETAIL","RetailType",strings[Integer.parseInt(fieldmatchMap.get("RETAILTYPE").toString())],false);
				String Store = InterfaceValue("C_STORE","Store",strings[Integer.parseInt(fieldmatchMap.get("STORE").toString())],true);
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[Integer.parseInt(fieldmatchMap.get("MASTERID").toString())]);
				paramMap.put("BillDate", format_date.format(format_date.parse(strings[Integer.parseInt(fieldmatchMap.get("BILLDATE").toString())])));
				paramMap.put("RetailType", RetailType);
				paramMap.put("Store", Store);
				paramMap.put("Opr", strings[Integer.parseInt(fieldmatchMap.get("OPR").toString())]);
				paramMap.put("OpDate", strings[Integer.parseInt(fieldmatchMap.get("OPDATE").toString())]);
				paramMap.put("Sku", strings[Integer.parseInt(fieldmatchMap.get("SKU").toString())]); 
				paramMap.put("Qty", strings[Integer.parseInt(fieldmatchMap.get("QTY").toString())]);
				paramMap.put("Price", strings[Integer.parseInt(fieldmatchMap.get("PRICE").toString())]);
				paramMap.put("Famount", strings[Integer.parseInt(fieldmatchMap.get("FAMOUNT").toString())]);
				paramMap.put("status", "0");
				paramMap.put("note", "");
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				//判断单据日期与盘点日期，决定执行存储过程 or 手工更新表
				query = "select count(*) from TDefStore " +
						"where (store = :Store and isnull(panddate,'1990-01-01')<:BillDate)";
				if(njdbcTemplate.queryForInt(query, paramMap)<1){
					insert ="insert into TBusRetail_TMP(MasterId, BillDate, RetailType, Store, Opr, OpDate, Sku, Qty, Price, Famount, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :RetailType, :Store, :Opr, :OpDate, :Sku, :Qty, :Price, :Famount, :status, :note, :addwho, :addtime ";
					paramMap.put("note", "与盘点日期不符");		
					paramMap.put("status", "2");
					njdbcTemplate.update(insert, paramMap);
					continue;
				}
				
				//判断明细插入是否重复
				insert ="insert into TBusRetail_TMP(MasterId, BillDate, RetailType ,Store, Opr, OpDate, Sku, Qty, Price, Famount, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate,:RetailType , :Store, :Opr, :OpDate, :Sku, :Qty, :Price, :Famount, :status, :note, :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusRetail_TMP where (  (MasterId = :MasterId and status ='1' ) ) )";
				// (MasterId = :MasterId and Sku = :Sku and status !='2') or
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusRetail_TMP(MasterId, BillDate, RetailType, Store, Opr, OpDate, Sku, Qty, Price, Famount, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate,:RetailType, :Store, :Opr, :OpDate, :Sku, :Qty, :Price, :Famount, :status, :note, :addwho, :addtime ";
					paramMap.put("note", "零售单插入重复");		
					paramMap.put("status", "2");
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
					
			//更新不能插入的明细记录状态为 2 
			/*update ="update TBusRetail_TMP set status = 2,note='1.明细重复' " +
					"where exists(select 'x' from TBusRetailDt where TBusRetail_TMP.MasterId=TBusRetailDt.MasterId and TBusRetail_TMP.Sku=TBusRetailDt.Sku )";
			jdbcTemplate.update(update);*/
			
			//读取临时表 插入到正式表 ， 同步
			//获取masterid 
			query = "select MasterId, BillDate,RetailType, Store, Opr, OpDate ,sum(Famount) TPayAmount " +
					"from TBusRetail_TMP where status = 0 and addwho ='"+Account+"' " +
					"group by MasterId, BillDate,RetailType ,Store, Opr, OpDate ";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			
			//读取是否执行相应存储过程
			boolean PChgRetail = true ;
			query = "select * from BASE_Interface_Procedure where TableName='TBusRetail' and Run='N' ";
			List<Map<String,Object>> ProcedureMap = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map : ProcedureMap) {
				String ProName = map.get("PRONAME")==null?"":map.get("PRONAME").toString();
				if(ProName.toUpperCase().equals("PCHGRETAIL"))
					PChgRetail = false ;
			}
			
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
				String RetailType = map_data.get("RETAILTYPE").toString();
				String Store = map_data.get("STORE").toString();
				String TPayAmount = map_data.get("TPAYAMOUNT").toString();
				String Opr = map_data.get("OPR").toString();
				String OpDate = map_data.get("OPDATE").toString();
				
				String MasterId_sys = "";
				Boolean procedure_exec = true ;
				synchronized (this) {
					//获取masterid
					query = "select MasterId from FGetNewMasterId('"+Store+"','TBusRetail')";
					MasterId_sys = jdbcTemplate.queryForObject(query, String.class);
					//插入数据到 抬头
					insert = "insert into TBusRetail(MasterId, BillDate,RetailType, Store, PayWay,PayAmount, TPayAmount, Opr, OpDate,Remark) " +
							 "select :MasterId_sys,:BillDate,:RetailType ,:Store,:PayWay,:PayAmount,:TPayAmount,:Opr,:OpDate,:Remark " +
							 "where not exists( select 'x' from TBusRetail where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("RetailType", RetailType);
					paramMap.put("Store", Store);
					paramMap.put("PayWay", "1");
					paramMap.put("PayAmount", TPayAmount);
					paramMap.put("TPayAmount", TPayAmount);
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					paramMap.put("Remark", "数据接口导入"+MasterId);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusRetail_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 and Store =:Store and BillDate =:BillDate  ";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusRetail_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusRetail失败' " +
								 "where MasterId = :MasterId and status =0 and Store =:Store and BillDate =:BillDate  ";
					}
					njdbcTemplate.update(update, paramMap);
				}
				
				if(!procedure_exec)
					continue;
				
				//插入数据到明细表
				insert = "insert into TBusRetailDt(MasterId,Sku,ModDis,Qty,Price,Famount,RetailType) " +
						 "select a.masterid_sys,a.Sku,:ModDis,a.Qty,c.DPrice,a.Famount,:RetailType " +
						 "from TBusRetail_tmp a,TDefSku b,TDefStyle c "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.MasterId = :MasterId and a.Store =:Store and a.BillDate =:BillDate and not exists( " +
						 "select 'x' from TBusRetailDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				paramMap.put("ModDis", "0");
				paramMap.put("RetailType", RetailType);
				paramMap.put("Store", Store);
				paramMap.put("BillDate", BillDate);
				
				logger.info("insert:{}, MasterId: {}",new Object[]{insert,MasterId});
				
				if(njdbcTemplate.update(insert, paramMap)==0){
					paramMap.put("MasterId_sys", MasterId_sys);
					update = "update  TBusRetail_tmp set status=2,note='插入TBusRetailDt失败' " +
							 "where MasterId = :MasterId and status = 1 and Store =:Store and BillDate =:BillDate ";
					njdbcTemplate.update(update, paramMap);
					
					delete ="delete from TBusRetail where MasterId = :MasterId_sys and exists(" +
							"select 'x' from TBusRetail_tmp where TBusRetail.MasterId = TBusRetail_tmp.MasterId_sys )";
					njdbcTemplate.update(delete, paramMap);
					
					continue;
				}
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				//没有数据插入，跳过本次存储过程执行
				/*if(!procedure_exec){
					//手工更新 TBusRetail 表 中 审核、验收、记账
					update = "update TBusRetail set " +
							"Charger='000',ChargeDate=:ChargeDate,Charged='1'," +
							"Remark='盘点日期不对,手工更新' " +
							"where MasterId = :MasterId_sys ";
					paramMap.clear();
					paramMap.put("ChargeDate", addtime);
					paramMap.put("MasterId_sys", MasterId_sys);
					njdbcTemplate.update(update, paramMap);
					
					continue; 
				}*/
					 
							
				// 零售单记账存储过程	
				if(PChgRetail){
					// 零售单记账存储过程	
					procedure = "{call PChgRetail(?,?)}";
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PChgRetail = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			            	cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        });
				}
				
			}
			
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员";
			msg = str ;
			logger.error("TBusRetail:"+e.toString());
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusSalRet(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
			//TBusSalRet 抬头明细表
			for (String[] strings : DataArray) {
				String ReturnType =InterfaceValue("M_RET_SALE","ReturnType",strings[Integer.parseInt(fieldmatchMap.get("RETURNTYPE").toString())],false);
				String Orig = InterfaceValue("C_STORE","Store",strings[Integer.parseInt(fieldmatchMap.get("ORIG").toString())],true);
				String Dest = InterfaceValue("C_STORE","Store",strings[Integer.parseInt(fieldmatchMap.get("DEST").toString())],true);
				
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[Integer.parseInt(fieldmatchMap.get("MASTERID").toString())]);
				paramMap.put("BillDate", format_date.format(format_date.parse(strings[Integer.parseInt(fieldmatchMap.get("BILLDATE").toString())])));
				paramMap.put("ReturnType", ReturnType);
				paramMap.put("Orig", Orig);
				paramMap.put("Dest", Dest);
				paramMap.put("DateOut", strings[Integer.parseInt(fieldmatchMap.get("DATEOUT").toString())]);
				paramMap.put("DateIn", strings[Integer.parseInt(fieldmatchMap.get("DATEIN").toString())]);
				paramMap.put("Opr", strings[Integer.parseInt(fieldmatchMap.get("OPR").toString())]);
				paramMap.put("OpDate", strings[Integer.parseInt(fieldmatchMap.get("OPDATE").toString())]);
				paramMap.put("Sku", strings[Integer.parseInt(fieldmatchMap.get("SKU").toString())]);
				paramMap.put("PreQty", strings[Integer.parseInt(fieldmatchMap.get("PREQTY").toString())]);
				paramMap.put("QtyOut", strings[Integer.parseInt(fieldmatchMap.get("QTYOUT").toString())]);
				paramMap.put("QtyIn", strings[Integer.parseInt(fieldmatchMap.get("QTYIN").toString())]);
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				
				//上下级经销商产生销售退货单
				query = "select count(*) from TDefStore a where Store = :Dest and  "+
						"BuyerId in ( "+
						"  select BuyerUp from TDefBuyer b where b.BuyerId in ( "+
						"    select c.BuyerId from TDefStore c where c.Store= :Orig and Closed=0 "+
						"  ) "+
						") and Closed=0 ";
				if(njdbcTemplate.queryForInt(query, paramMap)==0){
					insert ="insert into TBusSalRet_TMP(MasterId, BillDate, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '2', '经销商关系不符', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
					continue;
				}
				
				//盘点日期判断
				query = "select count(*) from TDefStore " +
						"where (store = :Orig and isnull(panddate,'1990-01-01')<:DateOut) or " +
						"(store = :Dest and isnull(panddate,'1990-01-01')<:DateIn) ";
				if(njdbcTemplate.queryForInt(query, paramMap)<2){
					insert ="insert into TBusSalRet_TMP(MasterId, BillDate, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '2', '与盘点日期不符', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
					continue;
				}
				
				//判断明细是否重复
				insert ="insert into TBusSalRet_TMP(MasterId, BillDate,ReturnType, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate,:ReturnType, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '0', '', :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusSalRet_TMP where ( (MasterId = :MasterId and status ='1' ) )  )";
				// (MasterId = :MasterId and Sku = :Sku and status !='2') or
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusSalRet_TMP(MasterId, BillDate,ReturnType, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate,:ReturnType, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '2', '销售退货明细重复插入', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
					
			//更新不能插入的明细记录状态为 2 
			/*update ="update TBusSalRet_tmp set status = 2,note='1.明细重复' " +
					"where exists(select 'x' from TBusSalRetDt where TBusSalRet_tmp.MasterId=TBusSalRetDt.MasterId and TBusSalRet_tmp.Sku=TBusSalRetDt.Sku )";
			jdbcTemplate.update(update);*/
			
			//读取临时表 插入到正式表 ， 同步
			//获取masterid 
			query = "select distinct MasterId,BillDate,ReturnType,Orig,Dest,Opr,OpDate,DateOut,DateIn " +
					"from TBusSalRet_TMP where status = 0 and addwho ='"+Account+"'";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			
			//读取是否执行相应存储过程
			boolean PChkSalRet = true ,PChgSalRet =true ,PAcpSalRet =true ;
			query = "select * from BASE_Interface_Procedure where TableName='TBusSalRet' and Run='N' ";
			List<Map<String,Object>> ProcedureMap = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map : ProcedureMap) {
				String ProName = map.get("PRONAME")==null?"":map.get("PRONAME").toString();
				if(ProName.toUpperCase().equals("PCHKSALRET"))
					PChkSalRet = false ;
				if(ProName.toUpperCase().equals("PCHGSALRET"))
					PChgSalRet = false ;
				if(ProName.toUpperCase().equals("PACPSALRET"))
					PAcpSalRet = false ;
			}
			
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
				String ReturnType = map_data.get("ReturnType").toString();
				String Opr = map_data.get("Opr").toString();
				String OpDate = map_data.get("OpDate").toString();
				String Orig = map_data.get("ORIG").toString();
				String Dest = map_data.get("DEST").toString();
				String DateOut = map_data.get("DATEOUT").toString();
				String DateIn = map_data.get("DATEIN").toString();
				
				String MasterId_sys = "";
				Boolean procedure_exec = true ;
				synchronized (this) {
					//获取masterid
					query = "select MasterId from FGetNewMasterId('"+Orig+"','TBusSalRet')";
					MasterId_sys = jdbcTemplate.queryForObject(query, String.class);
					//插入数据到 抬头
					insert = "insert into TBusSalRet(MasterId,BillDate,ReturnType,Orig,Dest,Opr,OpDate,DateOut,DateIn,Remark) " +
							 "select :MasterId_sys,:BillDate,:ReturnType,:Orig,:Dest,:Opr,:OpDate,:DateOut,:DateIn,:Remark " +
							 "where not exists( select 'x' from TBusSalRet where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("ReturnType", ReturnType);
					paramMap.put("Orig", Orig);
					paramMap.put("Dest", Dest);
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					paramMap.put("DateOut", DateOut);
					paramMap.put("DateIn", DateIn);
					paramMap.put("Remark", "数据接口导入"+MasterId);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusSalRet_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 and BillDate = :BillDate and Orig =:Orig and Dest =:Dest ";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusSalRet_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusSalRet失败' " +
								 "where MasterId = :MasterId and status =0 and BillDate = :BillDate and Orig =:Orig and Dest =:Dest";
					}
					njdbcTemplate.update(update, paramMap);
					
				}
				
				if(!procedure_exec)
					continue;
				
				//插入数据到明细表
				insert = "insert into TBusSalRetDt(MasterId,Sku,PreQty,QtyOut,QtyIn,DPrice) " +
						 "select a.masterid_sys,a.Sku,a.PreQty,a.QtyOut,a.QtyIn,c.Price " +
						 "from TBusSalRet_tmp a,TDefSku b,TDefStyle c "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.MasterId = :MasterId and BillDate = :BillDate and Orig =:Orig and Dest =:Dest and not exists(" +
						 "select 'x' from TBusSalRetDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				paramMap.put("BillDate", BillDate);
				paramMap.put("Orig", Orig);
				paramMap.put("Dest", Dest);
				
				if(njdbcTemplate.update(insert, paramMap)==0){
					paramMap.put("MasterId_sys", MasterId_sys);
					update = "update  TBusSalRet_tmp set status=2,note='插入TBusSalRet失败' " +
							 "where MasterId = :MasterId and status = 1 and BillDate = :BillDate and Orig =:Orig and Dest =:Dest";
					njdbcTemplate.update(update, paramMap);
					
					delete ="delete from TBusSalRet where MasterId = :MasterId_sys and exists(" +
							"select 'x' from TBusSalRet_tmp where TBusSalRet.MasterId = TBusSalRet_tmp.MasterId_sys )";
					
					njdbcTemplate.update(delete, paramMap);
					
					continue;
				}
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				//没有数据插入，跳过本次存储过程执行
				/*if(!procedure_exec){
					//手工更新 TBusSalRet 表 中 审核、验收、记账
					update = "update TBusSalRet set " +
							"Checker='000',CheckDate=:CheckDate,Checked=1," +
							"Charger='000',ChargeDate=:ChargeDate,Charged='1'," +
							"Accepter='000',AcceptDate=:AcceptDate,Accepted=1,Remark='盘点日期不对,手工更新' " +
							"where MasterId = :MasterId_sys ";
					paramMap.clear();
					paramMap.put("CheckDate", addtime);
					paramMap.put("ChargeDate", addtime);
					paramMap.put("AcceptDate", addtime);
					paramMap.put("MasterId_sys", MasterId_sys);
					njdbcTemplate.update(update, paramMap);
					
					continue; 
				}*/
					 
				
				if(PChkSalRet){
					// 销售退货单审核存储过程	
					procedure = "{call PChkSalRet(?,?)}";  		
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PChkSalRet = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			                cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        }); 
				}
				
				if(PChgSalRet){
					// 销售退货单记账存储过程	
					procedure = "{call PChgSalRet(?,?)}";
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PChgSalRet = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			            	cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        });
				}
				
				if(PAcpSalRet){
					// 销售退货单验收存储过程	
					procedure = "{call PAcpSalRet(?,?)}";  		
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PAcpSalRet = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			            	cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        });
				}
				
			}
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员(TBusSalRet)";
			msg = str ;
			logger.error("TBusSalRet:"+e.toString());
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusTran(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
			//TBusTran 抬头明细表
			for (String[] strings : DataArray) {
				String TranType =InterfaceValue("M_TRANSFER","TranType",strings[Integer.parseInt(fieldmatchMap.get("TRANTYPE").toString())],false);
				String Orig = InterfaceValue("C_STORE","Store",strings[Integer.parseInt(fieldmatchMap.get("ORIG").toString())],true);
				String Dest = InterfaceValue("C_STORE","Store",strings[Integer.parseInt(fieldmatchMap.get("DEST").toString())],true);
				
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[Integer.parseInt(fieldmatchMap.get("MASTERID").toString())]);
				paramMap.put("BillDate", format_date.format(format_date.parse(strings[Integer.parseInt(fieldmatchMap.get("BILLDATE").toString())])));
				paramMap.put("TranType", TranType);
				paramMap.put("Orig", Orig);
				paramMap.put("Dest", Dest);
				paramMap.put("DateOut", strings[Integer.parseInt(fieldmatchMap.get("DATEOUT").toString())]);
				paramMap.put("DateIn", strings[Integer.parseInt(fieldmatchMap.get("DATEIN").toString())]);
				paramMap.put("Opr", strings[Integer.parseInt(fieldmatchMap.get("OPR").toString())]);
				paramMap.put("OpDate", strings[Integer.parseInt(fieldmatchMap.get("OPDATE").toString())]);
				paramMap.put("Sku", strings[Integer.parseInt(fieldmatchMap.get("SKU").toString())]);
				paramMap.put("QtyTran", strings[Integer.parseInt(fieldmatchMap.get("QTYTRAN").toString())]);
				paramMap.put("QtyOut", strings[Integer.parseInt(fieldmatchMap.get("QTYOUT").toString())]);
				paramMap.put("QtyIn", strings[Integer.parseInt(fieldmatchMap.get("QTYIN").toString())]);
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				
				//同一经销商产生调拨单
				query = "select count(*) from TDefStore a where Store = :Orig and  "+
						"BuyerId in ( "+
						"  select c.BuyerId from TDefStore c where c.Store= :Dest and Closed=0 "+
						")  and Closed=0 ";
				if(njdbcTemplate.queryForInt(query, paramMap)==0){
					insert ="insert into TBusTran_TMP(MasterId, BillDate, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, QtyTran, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :QtyTran, :QtyOut, :QtyIn, '2', '经销商关系不符', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
					continue;
				}
				
				//判断盘点日期
				query = "select count(*) from TDefStore " +
						"where (store = :Orig and isnull(panddate,'1990-01-01')<:DateOut) or " +
						"(store = :Dest and isnull(panddate,'1990-01-01')<:DateIn) ";
				if(njdbcTemplate.queryForInt(query, paramMap)<2){
					insert ="insert into TBusTran_TMP(MasterId, BillDate, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, QtyTran, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :QtyTran, :QtyOut, :QtyIn, '2', '与盘点日期不符', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
					continue;
				}
				
				//判断明细是否重复
				insert ="insert into TBusTran_TMP(MasterId, BillDate,TranType, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, QtyTran, QtyOut, QtyIn, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate,:TranType, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :QtyTran, :QtyOut, :QtyIn, '0', '', :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusTran_TMP where ( (MasterId = :MasterId and status ='1' ) )  )";
				// (MasterId = :MasterId and Sku = :Sku and status !='2') or 
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusTran_TMP(MasterId, BillDate,TranType, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, QtyTran, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate,:TranType, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :QtyTran, :QtyOut, :QtyIn, '2', '调拨明细重复插入', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
					
			//更新不能插入的明细记录状态为 2 
			/*update ="update TBusTran_tmp set status = 2,note='1.明细重复' " +
					"where exists(select 'x' from TBusTranDt where TBusTran_tmp.MasterId=TBusTranDt.MasterId and TBusTran_tmp.Sku=TBusTranDt.Sku )";
			jdbcTemplate.update(update);*/
			
			//读取临时表 插入到正式表 ， 同步
			//获取masterid 
			query = "select distinct MasterId,BillDate,TranType,Orig,Dest,Opr,OpDate,DateOut,DateIn " +
					"from TBusTran_TMP where status = 0 and addwho ='"+Account+"'";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			
			//读取是否执行相应存储过程
			boolean PChkTran = true,PChgTran=true,PAcpTran=true ;
			query = "select * from BASE_Interface_Procedure where TableName='TBusTran' and Run='N' ";
			List<Map<String,Object>> ProcedureMap = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map : ProcedureMap) {
				String ProName = map.get("PRONAME")==null?"":map.get("PRONAME").toString();
				if(ProName.toUpperCase().equals("PCHKTRAN"))
					PChkTran = false ;
				if(ProName.toUpperCase().equals("PCHGTRAN"))
					PChgTran = false ;
				if(ProName.toUpperCase().equals("PACPTRAN"))
					PAcpTran = false ;
			}
			
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
				String TranType = map_data.get("TranType").toString();
				String Opr = map_data.get("Opr").toString();
				String OpDate = map_data.get("OpDate").toString();
				String Orig = map_data.get("ORIG").toString();
				String Dest = map_data.get("DEST").toString();
				String DateOut = map_data.get("DATEOUT").toString();
				String DateIn = map_data.get("DATEIN").toString();
				
				String MasterId_sys = "";
				Boolean procedure_exec = true ;
				synchronized (this) {
					//获取masterid
					query = "select MasterId from FGetNewMasterId('"+Orig+"','TBusTran')";
					MasterId_sys = jdbcTemplate.queryForObject(query, String.class);
					//插入数据到 抬头
					insert = "insert into TBusTran(MasterId,BillDate,TranType,TranFlag,OutType,Orig,Dest,Opr,OpDate,DateOut,DateIn,Remark) " +
							 "select :MasterId_sys,:BillDate,:TranType,:TranFlag,:OutType,:Orig,:Dest,:Opr,:OpDate,:DateOut,:DateIn,:Remark " +
							 "where not exists( select 'x' from TBusTran where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("TranType", TranType);
					paramMap.put("TranFlag", "0");
					paramMap.put("OutType", "3");
					paramMap.put("Orig", Orig);
					paramMap.put("Dest", Dest);
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					paramMap.put("DateOut", DateOut);
					paramMap.put("DateIn", DateIn);
					paramMap.put("Remark", "数据接口导入"+MasterId);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusTran_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 and BillDate = :BillDate and Orig = :Orig and Dest = :Dest";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusTran_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusTran失败' " +
								 "where MasterId = :MasterId and status =0 and BillDate = :BillDate and Orig = :Orig and Dest = :Dest";
					}
					njdbcTemplate.update(update, paramMap);
				}
				
				if(!procedure_exec)
					continue;
				//插入数据到明细表
				insert = "insert into TBusTranDt(MasterId,Sku,QtyTran,QtyOut,QtyIn,DPrice) " +
						 "select a.masterid_sys,a.Sku,a.QtyTran,a.QtyOut,a.QtyIn,c.Price " +
						 "from TBusTran_tmp a,TDefSku b,TDefStyle c "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.MasterId = :MasterId and a.BillDate = :BillDate and a.Orig = :Orig and a.Dest = :Dest and " +
						 "not exists(" +
						 "select 'x' from TBusTranDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				paramMap.put("BillDate", BillDate);
				paramMap.put("Orig", Orig);
				paramMap.put("Dest", Dest);
				
				if(njdbcTemplate.update(insert, paramMap)==0){
					paramMap.put("MasterId_sys", MasterId_sys);
					
					update = "update  TBusTran_tmp set status=2,note='插入TBusTranDt失败' " +
							 "where MasterId = :MasterId and status = 1 and BillDate = :BillDate and Orig = :Orig and Dest = :Dest";
					njdbcTemplate.update(update, paramMap);
					
					delete ="delete from TBusTran where MasterId = :MasterId_sys and exists(" +
							"select 'x' from TBusTran_tmp where TBusTran.MasterId = TBusTran_tmp.MasterId_sys )";
					
					njdbcTemplate.update(delete, paramMap);
					
					continue;
				}
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				//没有数据插入，跳过本次存储过程执行
				/*if(!procedure_exec){
					//手工更新 TBusTran 表 中 审核、验收、记账
					update = "update TBusTran set " +
							"Checker='000',CheckDate=:CheckDate,Checked=1," +
							"Charger='000',ChargeDate=:ChargeDate,Charged='1'," +
							"Accepter='000',AcceptDate=:AcceptDate,Accepted=1,Remark='盘点日期不对,手工更新' " +
							"where MasterId = :MasterId_sys ";
					paramMap.clear();
					paramMap.put("CheckDate", addtime);
					paramMap.put("ChargeDate", addtime);
					paramMap.put("AcceptDate", addtime);
					paramMap.put("MasterId_sys", MasterId_sys);
					njdbcTemplate.update(update, paramMap);
					
					continue; 
				}*/
				
				if(PChkTran){
					// 调拨单审核存储过程	
					procedure = "{call PChkTran(?,?)}";  		
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PChkTran = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			                cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        }); 
				}
				
				if(PChgTran){
					// 调拨单记账存储过程	
					procedure = "{call PChgTran(?,?)}";
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PChgTran = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			            	cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        });
				}
				
				if(PAcpTran){
					// 调拨单验收存储过程	
					procedure = "{call PAcpTran(?,?)}";  		
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PAcpTran = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			            	cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        });
				}
				
			}
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员";
			msg = str ;
			logger.error("TBusTran:"+e.toString());
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusAdj(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
			//TBusAdj 抬头明细表
			for (String[] strings : DataArray) {
				String AdjType =InterfaceValue("M_OTHER_INOUT","AdjType",strings[Integer.parseInt(fieldmatchMap.get("ADJTYPE").toString())],false);
				String Store = InterfaceValue("C_STORE","Store",strings[Integer.parseInt(fieldmatchMap.get("STORE").toString())],true);
				String Remark = strings[Integer.parseInt(fieldmatchMap.get("REMARK").toString())];
				int Remark_len = Remark.length()>9?9:Remark.length();
				Remark = Remark.substring(0,Remark_len);
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[Integer.parseInt(fieldmatchMap.get("MASTERID").toString())]);
				paramMap.put("BillDate", format_date.format(format_date.parse(strings[Integer.parseInt(fieldmatchMap.get("BILLDATE").toString())])));
				paramMap.put("AdjType", AdjType);
				paramMap.put("Store", Store);
				paramMap.put("Remark", Remark);
				paramMap.put("Opr", strings[Integer.parseInt(fieldmatchMap.get("OPR").toString())]);
				paramMap.put("OpDate", strings[Integer.parseInt(fieldmatchMap.get("OPDATE").toString())]);
				paramMap.put("Sku", strings[Integer.parseInt(fieldmatchMap.get("SKU").toString())]);
				paramMap.put("Qty", strings[Integer.parseInt(fieldmatchMap.get("QTY").toString())]);
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				
				//盘点日期判断
				query = "select count(*) from TDefStore " +
						"where (Store = :Store and isnull(panddate,'1990-01-01')<:BillDate) ";
				if(njdbcTemplate.queryForInt(query, paramMap)<1){
					insert ="insert into TBusAdj_TMP(MasterId, BillDate, Store, Remark, Opr, OpDate, Sku, Qty, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Store, :Remark, :Opr, :OpDate, :Sku, :Qty, '2', '与盘点日期不符', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
					continue;
				}
				
				//明细是否重复
				insert ="insert into TBusAdj_TMP(MasterId, BillDate,AdjType, Store, Remark, Opr, OpDate, Sku, Qty, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate, :AdjType, :Store, :Remark, :Opr, :OpDate, :Sku, :Qty, '0', '', :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusAdj_TMP where (  (MasterId = :MasterId and status ='1' ) )  )";
				//(MasterId = :MasterId and Sku = :Sku and status !='2') or
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusAdj_TMP(MasterId, BillDate, AdjType, Store, Remark, Opr, OpDate, Sku, Qty, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :AdjType, :Store, :Remark, :Opr, :OpDate, :Sku, :Qty, '2', '明细插入重复', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
					
			//更新不能插入的明细记录状态为 2 
			/*update ="update TBusAdj_tmp set status = 2,note='1.明细重复' " +
					"where exists(select 'x' from TBusAdjDt where TBusAdj_tmp.MasterId=TBusAdjDt.MasterId and TBusAdj_tmp.Sku=TBusAdjDt.Sku )";
			jdbcTemplate.update(update);*/
			
			//读取临时表 插入到正式表 ， 同步
			//获取masterid 
			query = "select distinct MasterId,BillDate,AdjType,Store,Remark,Opr,OpDate " +
					"from TBusAdj_TMP where status = 0 and addwho ='"+Account+"'";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			
			//读取是否执行相应存储过程
			boolean PChkAdj = true ,PChgAdj=true;
			query = "select * from BASE_Interface_Procedure where TableName='TBusAdj' and Run='N' ";
			List<Map<String,Object>> ProcedureMap = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map : ProcedureMap) {
				String ProName = map.get("PRONAME")==null?"":map.get("PRONAME").toString();
				if(ProName.toUpperCase().equals("PCHKADJ"))
					PChkAdj = false ;
				if(ProName.toUpperCase().equals("PCHGADJ"))
					PChgAdj = false ;
			}
			
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
				String AdjType = map_data.get("AdjType").toString();
				String Store = map_data.get("Store").toString();
				String Remark = map_data.get("Remark").toString();
				String Opr = map_data.get("Opr").toString();
				String OpDate = map_data.get("OpDate").toString();
				if(Remark.equals(""))
					Remark = "无调整原因";
				
				String MasterId_sys = "";
				Boolean procedure_exec = true ;
				synchronized (this) {
					//获取masterid
					query = "select MasterId from FGetNewMasterId('"+Store+"','TBusAdj')";
					MasterId_sys = jdbcTemplate.queryForObject(query, String.class);
					//插入数据到 抬头
					insert = "insert into TBusAdj(MasterId,BillDate,AdjType,Store,Remark,Opr,OpDate) " +
							 "select :MasterId_sys,:BillDate,:AdjType,:Store,:Remark,:Opr,:OpDate " +
							 "where not exists( select 'x' from TBusAdj where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("AdjType", AdjType);
					paramMap.put("Store", Store);
					paramMap.put("Remark", Remark);
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusAdj_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 and BillDate = :BillDate and Store = :Store ";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusAdj_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusAdj失败' " +
								 "where MasterId = :MasterId and status =0 and BillDate = :BillDate and Store = :Store ";
					}
					njdbcTemplate.update(update, paramMap);
				}
				
				if(!procedure_exec)
					continue;
				
				//插入数据到明细表
				insert = "insert into TBusAdjDt(MasterId,Sku,Qty,DPrice) " +
						 "select a.masterid_sys,a.Sku,a.Qty,c.Price " +
						 "from TBusAdj_tmp a,TDefSku b,TDefStyle c "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.MasterId = :MasterId and a.BillDate = :BillDate and a.Store = :Store and " +
						 "not exists(" +
						 "select 'x' from TBusAdjDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				paramMap.put("BillDate", BillDate);
				paramMap.put("Store", Store);
				if(njdbcTemplate.update(insert, paramMap)==0){
					paramMap.put("MasterId_sys", MasterId_sys);
					
					update = "update  TBusAdj_tmp set status=2,note='插入TBusAdjDt失败' " +
							 "where MasterId = :MasterId and status = 1 and BillDate = :BillDate and Store = :Store ";
					njdbcTemplate.update(update, paramMap);
					
					delete ="delete from TBusAdj where MasterId = :MasterId_sys and exists(" +
							"select 'x' from TBusAdj_tmp where TBusAdj.MasterId = TBusAdj_tmp.MasterId_sys )";
					njdbcTemplate.update(delete, paramMap);
					
					continue;
				}
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				//没有数据插入，跳过本次存储过程执行
				/*if(!procedure_exec){
					//手工更新 TBusAdj 表 中 审核、验收、记账
					update = "update TBusAdj set " +
							"Checker='000',CheckDate=:CheckDate,Checked=1," +
							"Charger='000',ChargeDate=:ChargeDate,Charged='1'," +
							"Remark='盘点日期不对,手工更新' " +
							"where MasterId = :MasterId_sys ";
					paramMap.clear();
					paramMap.put("CheckDate", addtime);
					paramMap.put("ChargeDate", addtime);
					paramMap.put("AcceptDate", addtime);
					paramMap.put("MasterId_sys", MasterId_sys);
					njdbcTemplate.update(update, paramMap);
					
					continue; 
				}*/
				
				if(PChkAdj){
					// 物理调整单审核存储过程	
					procedure = "{call PChkAdj(?,?)}";  		
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PChkAdj = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			                cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        }); 
				}
				
				if(PChgAdj){
					// 物理调整单记账存储过程	
					procedure = "{call PChgAdj(?,?)}";
					@SuppressWarnings("unchecked")
					Map<String,Object> map_PChgAdj = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
			            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
			            	cs.setString(1, MasterId_sys_p);
			                cs.setString(2, "000");	
			                cs.execute();
			                Map<String,Object> map = new HashMap<String, Object>();  
			                map.put("r_code", "0");
			                return map;
			            }
			        });
				}
								
			}
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员";
			msg = str ;
			logger.error("TBusAdj:"+e.toString());
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusPand(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			//每天或者某一天的凌晨 2点到4点之间，插入一张盘点单据，数据取有前一天有库存变化的商品或者全部商品的当前库存信息。
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			SimpleDateFormat format_date = new SimpleDateFormat("yyyy-MM-dd");
			
			//盘点日期为前一天
			Date now = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(now);
			calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
			Date PreDate = format_date.parse(format_date.format(calendar.getTime()));
			
			//TBusPand 抬头明细表
			for (String[] strings : DataArray) {
				Map<String,Object> paramMap = new HashMap<String, Object>();
				//String Store = InterfaceValue("C_STORE","Store",strings[Integer.parseInt(fieldmatchMap.get("STORE").toString())],true);
				String Store = strings[Integer.parseInt(fieldmatchMap.get("STORE").toString())];
				
				paramMap.put("MasterId", strings[Integer.parseInt(fieldmatchMap.get("MASTERID").toString())]);
				paramMap.put("BillDate", format_date.format(format_date.parse(strings[Integer.parseInt(fieldmatchMap.get("BILLDATE").toString())])));
				paramMap.put("Store", Store);
				paramMap.put("Opr", strings[Integer.parseInt(fieldmatchMap.get("OPR").toString())]);
				paramMap.put("OpDate", strings[Integer.parseInt(fieldmatchMap.get("OPDATE").toString())]);
				paramMap.put("Sku", strings[Integer.parseInt(fieldmatchMap.get("SKU").toString())]);
				paramMap.put("PreQty", strings[Integer.parseInt(fieldmatchMap.get("PREQTY").toString())]);
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				/*//判断盘点日期
				query = "select count(*) from TDefStore " +
						"where (Store = :Store and isnull(panddate,'1990-01-01')<:BillDate) ";
				if(njdbcTemplate.queryForInt(query, paramMap)<1){ //盘点日期有误,不能插入记录
					insert ="insert into TBusPand_TMP(MasterId, BillDate, Store, Opr, OpDate, Sku, PreQty, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Store, :Opr, :OpDate, :Sku, :PreQty, '2', '与盘点日期不符', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
					continue;
				}*/
				
				//判断明细是否重复
				insert ="insert into TBusPand_TMP(MasterId, BillDate, Store, Opr, OpDate, Sku, PreQty, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate, :Store, :Opr, :OpDate, :Sku, :PreQty, '0', '', :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusPand_TMP where ( (MasterId = :MasterId and Sku = :Sku and BillDate = :BillDate and Store = :Store ) )  )";
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusPand_TMP(MasterId, BillDate, Store, Opr, OpDate, Sku, PreQty, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Store, :Opr, :OpDate, :Sku, :PreQty, '2', '库存无变化', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
			//根据映射表更新Store 的值
			update ="update TBusPand_TMP  set Store = ( "+
					"select LocalKey from BASE_Interface_Value b where Store=b.TargetKey and b.TableName='C_STORE' "+ 
					"and isactive='Y') where status='0' and exists(" +
					"select 'x' from BASE_Interface_Value b where Store=b.TargetKey and b.TableName='C_STORE' "+ 
					"and isactive='Y')";
			
			jdbcTemplate.update(update);
			
			delete ="delete from TBusPand_TMP where not exists(select 'x' from TDefStore b where Store = b.Store ) " +
					"and status='0' ";
			jdbcTemplate.update(delete);
			
			
			//将不存在的库存信息插入到相应的店仓,数量先默认为 0
			insert ="insert into TAccStock(Store,Sku) " +
					"select Store,Sku from TBusPand_TMP a where not exists(" +
					"	select 'x' from TAccStock b where a.Store = b.Store and a.Sku=b.Sku " +
					") and status = 0 and addwho ='"+Account+"'";
			
			jdbcTemplate.update(insert);
			
			
			
			//读取库存更新方式
			WebUtils web = new WebUtils();
			String Style = web.readValue("config/others/config.properties","Emax.Stock_Update_Style");
			if(Style==null||Style.equals("")){
				Style = "0";
			}
			
			if(Style.equals("0")){
				
				query = "select distinct Masterid , Store " +
						"from TBusPand_TMP where status = 0 and addwho ='"+Account+"'";
				List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
				for (Map<String, Object> map : list_data) {
					//直接更新库存
					update = "update TAccStock set Adj = Adj-" +
							"(select TAccStock.Qty-PreQty from TBusPand_TMP where TAccStock.Store = TBusPand_TMP.Store and TBusPand_TMP.status = 0 and TAccStock.Sku = TBusPand_TMP.Sku) " +
							"where exists(select 'x' from TBusPand_TMP where TAccStock.Store = TBusPand_TMP.Store and TBusPand_TMP.status = 0 and TAccStock.Sku = TBusPand_TMP.Sku) and " +
							"Store = :Store ";
					if(njdbcTemplate.update(update, map)>0){
						update = "update TBusPand_TMP set status =1 where Store = :Store and Masterid = :Masterid and status=0";
					}else{
						update = "update TBusPand_TMP set status =2,note='直接更新库存失败' where Store = :Store and Masterid = :Masterid and status=0";
					}
					njdbcTemplate.update(update, map);
				}
				
			}else{ // 生成盘点单据
				
				//更新不能插入的明细记录状态为 2 
				/*update ="update TBusPand_tmp set status = 2,note='1.明细重复' " +
						"where exists(select 'x' from TBusPandDt where TBusPand_tmp.MasterId=TBusPandDt.MasterId and TBusPand_tmp.Sku=TBusPandDt.Sku )";
				jdbcTemplate.update(update);*/
				
				//读取临时表 插入到正式表 ， 同步
				//获取masterid 
				query = "select distinct MasterId,Store " +
						"from TBusPand_TMP where status = 0 and addwho ='"+Account+"'";
				List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
				
				//读取是否执行相应存储过程
				boolean PChkPand = true,PChgPand=true ;
				query = "select * from BASE_Interface_Procedure where TableName='TBusPand' and Run='N' ";
				List<Map<String,Object>> ProcedureMap = jdbcTemplate.queryForList(query);
				for (Map<String, Object> map : ProcedureMap) {
					String ProName = map.get("PRONAME")==null?"":map.get("PRONAME").toString();
					if(ProName.toUpperCase().equals("PCHKPAND"))
						PChkPand = false ;
					if(ProName.toUpperCase().equals("PCHGPAND"))
						PChgPand = false ;
				}
				
				for (Map<String, Object> map_data : list_data) {
					String MasterId = map_data.get("MasterId").toString();
					String Store = map_data.get("Store").toString();
									
					String MasterId_sys = "";
					Boolean procedure_exec = true ;
					
					synchronized (this) {
						//获取masterid
						query = "select MasterId from FGetNewMasterId('"+Store+"','TBusPand')";
						MasterId_sys = jdbcTemplate.queryForObject(query, String.class);
						//插入数据到 抬头
						insert = "insert into TBusPand(MasterId,BillDate,Store,Remark,Opr,OpDate,PandType) " +
								 "select :MasterId_sys,:BillDate,:Store,:Remark,:Opr,:OpDate,'2' " +
								 "where not exists( select 'x' from TBusPand where MasterId=:MasterId_sys )";
						Map<String,Object> paramMap = new HashMap<String, Object>();
						paramMap.put("MasterId_sys", MasterId_sys);
						paramMap.put("MasterId", MasterId);
						paramMap.put("BillDate", format_date.format(PreDate));
						paramMap.put("Store", Store);
						paramMap.put("Remark", "盘点库存");
						paramMap.put("Opr", "");
						paramMap.put("OpDate", addtime);
						
						if(njdbcTemplate.update(insert, paramMap)>0){
							update = "update  TBusPand_TMP set MasterId_sys = :MasterId_sys,status=1 " +
									 "where MasterId = :MasterId and status =0  and Store = :Store";
						}else{
							procedure_exec = false; //不执行存储过程
							update = "update  TBusPand_TMP set MasterId_sys = :MasterId_sys,status=2,note='插入TBusPand失败' " +
									 "where MasterId = :MasterId and status =0 and Store = :Store";
						}
						njdbcTemplate.update(update, paramMap);
						
					}
					
					if(!procedure_exec)
						continue;
					
					
					//插入数据到明细表
					insert = "insert into TBusPandDt(MasterId,Sku,PreQty,DocQty,DPrice) " +
							 "select a.masterid_sys,a.Sku,a.PreQty,d.Qty DocQty,c.Price " +
							 "from TBusPand_tmp a,TDefSku b,TDefStyle c,TAccStock d "+
							 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
							 "a.Store = d.Store and a.Sku = d.Sku and " +
							 "a.MasterId = :MasterId and a.Store = :Store and " +
							 "not exists(" +
							 "select 'x' from TBusPandDt where MasterId = a.masterid_sys and Sku =a.Sku )"; 
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId", MasterId);
					paramMap.put("Store", Store);
					if(njdbcTemplate.update(insert, paramMap)==0){
						paramMap.put("MasterId_sys", MasterId_sys);
						
						update = "update  TBusPand_tmp set status=2,note='插入TBusPandDt失败' " +
								 "where MasterId = :MasterId and status = 1 and Store = :Store";
						njdbcTemplate.update(update, paramMap);
						
						delete ="delete from TBusPandDt where MasterId = :MasterId_sys and exists(" +
								"select 'x' from TBusPand_tmp where TBusPandDt.MasterId = TBusPand_tmp.MasterId_sys )";
						njdbcTemplate.update(delete, paramMap);
						
						continue;
					}
					
					//读取原始盘点日期
					query = "select max(isnull(PandDate,'2000-03-25')) PandDate from TdefStore where Store = :Store ";
					String PandDate = njdbcTemplate.queryForObject(query, paramMap, String.class);
					
					
					//执行相应的存储过程 
					String procedure = "";
					final String MasterId_sys_p = MasterId_sys;
					
					if(PChkPand){
						// 盘点单审核存储过程	
						procedure = "{call PChkPand(?,?)}";  		
						@SuppressWarnings("unchecked")
						Map<String,Object> map_PChkPand = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
				            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
				                cs.setString(1, MasterId_sys_p);
				                cs.setString(2, "000");	
				                cs.execute();
				                Map<String,Object> map = new HashMap<String, Object>();  
				                map.put("r_code", "0");
				                return map;
				            }
				        }); 
					}
					
					if(PChgPand){
						// 盘点单记账存储过程	
						procedure = "{call PChgPand(?,?)}";
						@SuppressWarnings("unchecked")
						Map<String,Object> map_PChgAdj = (HashMap<String, Object>) jdbcTemplate.execute(procedure,new CallableStatementCallback() {  
				            public Object doInCallableStatement(CallableStatement cs)throws SQLException,DataAccessException {  
				            	cs.setString(1, MasterId_sys_p);
				                cs.setString(2, "000");	
				                cs.execute();
				                Map<String,Object> map = new HashMap<String, Object>();  
				                map.put("r_code", "0");
				                return map;
				            }
				        });
					}
					
					//是否更新盘点日期   0 不更新   1 更新
					String PandDate_Update = web.readValue("config/others/config.properties","Emax.PandDate_Update");
					if(PandDate_Update==null||PandDate_Update.equals("")){
						PandDate_Update = "0";
					}
					if(PandDate_Update.equalsIgnoreCase("0")){
						//更新店仓的最后盘点日期为 前一天，这样不影响今天单据的录入，如果后续有补单的情况，就无法录入
						update = "update TDefStore set PandDate = :BillDate where Store = :Store ";
						Map<String,Object> paramPandDateMap = new HashMap<String, Object>();
						paramPandDateMap.put("BillDate", PandDate);
						paramPandDateMap.put("Store", Store);
						njdbcTemplate.update(update, paramPandDateMap);
					}
					
					
				}
				
			}
			
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员";
			msg = str ;
			logger.error("TBusPand:"+e.toString());
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}
	
	public String InterfaceValue(String TableName,String Type,String TargetKey,Boolean force ){
		String query=""; 
		query = "select max(LocalKey) from BASE_Interface_Value where tablename =:tablename and type =:type and targetkey =:targetkey and isactive='Y'";
		Map<String,Object> ParamMap = new HashMap<String, Object>();
		ParamMap.put("tablename", TableName);
		ParamMap.put("type", Type);
		ParamMap.put("targetkey", TargetKey);
		String LocalKey = njdbcTemplate.queryForObject(query, ParamMap, String.class);
		if(LocalKey==null||LocalKey.equals("")){
			if(force)
				query = "select max(DefaultValue) from BASE_Interface_Value where tablename =:tablename and type =:type and targetkey =:targetkey and isactive='Y' ";
			else
				query = "select max(DefaultValue) from BASE_Interface_Value where tablename =:tablename and type =:type and isactive='Y' ";
			LocalKey = njdbcTemplate.queryForObject(query, ParamMap, String.class);
		}
				
		return LocalKey==null||LocalKey.equals("")?TargetKey:LocalKey;
	}

	
}
