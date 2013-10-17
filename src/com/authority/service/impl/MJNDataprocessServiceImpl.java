package com.authority.service.impl;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.authority.common.utils.PoiHelper;
import com.authority.service.MJNDataprocessService;

@Service
public class MJNDataprocessServiceImpl implements MJNDataprocessService {

	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusSale(List<String[]> DataArray,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			//Tbussale 抬头明细表
			for (String[] strings : DataArray) {
				insert ="insert into TBusSale_TMP(MasterId, BillDate, SaleType, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate, :SaleType, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '0', '', :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusSale_TMP where ( (MasterId = :MasterId and Sku = :Sku and status !='2') or (MasterId = :MasterId and status ='1' ) )  )";
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[0]);
				paramMap.put("BillDate", strings[1]);
				paramMap.put("SaleType", strings[2]);
				paramMap.put("Orig", strings[3]);
				paramMap.put("Dest", strings[4]);
				paramMap.put("DateOut", strings[5]);
				paramMap.put("DateIn", strings[6]);
				paramMap.put("Opr", strings[7]);
				paramMap.put("OpDate", strings[8]);
				paramMap.put("Sku", strings[9]);
				paramMap.put("PreQty", strings[10]);
				paramMap.put("QtyOut", strings[11]);
				paramMap.put("QtyIn", strings[12]);
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusSale_TMP(MasterId, BillDate, SaleType, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :SaleType, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '2', '销售明细重复插入', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
					
			//更新不能插入的明细记录状态为 2 
			/*update ="update TBusSale_tmp set status = 2,note='1.明细重复' " +
					"where exists(select 'x' from TBusSaleDt where TBusSale_tmp.MasterId=TBusSaleDt.MasterId and TBusSale_tmp.Sku=TBusSaleDt.Sku )";
			jdbcTemplate.update(update);*/
			
			//读取临时表 插入到正式表 ， 同步
			//获取masterid 
			query = "select distinct MasterId,BillDate,SaleType,Orig,Dest,Opr,OpDate,DateOut,DateIn " +
					"from TBusSale_TMP where status = 0 ";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
				String SaleType = map_data.get("SaleType").toString();
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
					insert = "insert into TBusSale(MasterId,BillDate,SaleType,Orig,Dest,Opr,OpDate,DateOut,DateIn) " +
							 "select :MasterId_sys,:BillDate,:SaleType,:Orig,:Dest,:Opr,:OpDate,:DateOut,:DateIn " +
							 "where not exists( select 'x' from TBusSale where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("SaleType", SaleType);
					paramMap.put("Orig", Orig);
					paramMap.put("Dest", Dest);
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					paramMap.put("DateOut", DateOut);
					paramMap.put("DateIn", DateIn);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusSale_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 ";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusSale_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusSale失败' " +
								 "where MasterId = :MasterId and status =0 ";
					}
					njdbcTemplate.update(update, paramMap);
					
					//判断单据出库日期、入库日期与盘点日期，决定执行存储过程 or 手工更新表
					query = "select count(*) from TDefStore " +
							"where (store = :Orig and isnull(panddate,'1990-01-01')<=:DateOut) or " +
							"(store = :Dest and isnull(panddate,'1990-01-01')<=:DateIn) ";
					if(njdbcTemplate.queryForInt(query, paramMap)<2)
						procedure_exec = false;
					
				}
				
				//插入数据到明细表
				insert = "insert into TBusSaleDt(MasterId,Sku,PreQty,QtyOut,QtyIn,DPrice) " +
						 "select a.masterid_sys,a.Sku,a.PreQty,a.QtyOut,a.QtyIn,c.Price " +
						 "from TBusSale_tmp a,TDefSku b,TDefStyle c "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.MasterId = :MasterId and not exists(" +
						 "select 'x' from TBusSaleDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				njdbcTemplate.update(insert, paramMap);
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				//没有数据插入，跳过本次存储过程执行
				if(!procedure_exec){
					//手工更新 TBusSale 表 中 审核、验收、记账
					update = "update TBusSale set " +
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
				}
					 
				
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
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员";
			msg = str ;
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusRetail(List<String[]> DataArray,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			//TBusRetail 抬头明细表
			for (String[] strings : DataArray) {
				insert ="insert into TBusRetail_TMP(MasterId, BillDate, Store, TPayAmount, Opr, OpDate, Sku, Qty, Famount, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate, :Store, :TPayAmount, :Opr, :OpDate, :Sku, :Qty, :Famount, :status, :note, :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusRetail_TMP where ( (MasterId = :MasterId and Sku = :Sku and status !='2') or (MasterId = :MasterId and status ='1' ) ) )";
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[0]);
				paramMap.put("BillDate", strings[1]);
				paramMap.put("Store", strings[2]);
				paramMap.put("TPayAmount", strings[3]);
				paramMap.put("Opr", strings[4]);
				paramMap.put("OpDate", strings[5]);
				paramMap.put("Sku", strings[6]); 
				paramMap.put("Qty", strings[7]);
				paramMap.put("Famount", strings[8]);
				paramMap.put("status", "0");
				paramMap.put("note", "");
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusRetail_TMP(MasterId, BillDate, Store, TPayAmount, Opr, OpDate, Sku, Qty, Famount, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Store, :TPayAmount, :Opr, :OpDate, :Sku, :Qty, :Famount, :status, :note, :addwho, :addtime ";
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
			query = "select distinct MasterId, BillDate, Store, TPayAmount, Opr, OpDate " +
					"from TBusRetail_TMP where status = 0 ";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
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
					insert = "insert into TBusRetail(MasterId, BillDate, Store, PayWay, TPayAmount,RetailType, Opr, OpDate) " +
							 "select :MasterId_sys,:BillDate,:Store,:PayWay,:TPayAmount,:RetailType,:Opr,:OpDate " +
							 "where not exists( select 'x' from TBusRetail where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("Store", Store);
					paramMap.put("PayWay", "1");
					paramMap.put("TPayAmount", TPayAmount);
					paramMap.put("RetailType", "0");
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusRetail_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 ";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusRetail_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusRetail失败' " +
								 "where MasterId = :MasterId and status =0 ";
					}
					njdbcTemplate.update(update, paramMap);
					
					//判断单据日期与盘点日期，决定执行存储过程 or 手工更新表
					query = "select count(*) from TDefStore " +
							"where (store = :Store and isnull(panddate,'1990-01-01')<=:BillDate)";
					if(njdbcTemplate.queryForInt(query, paramMap)<1)
						procedure_exec = false;
					
				}
				
				//插入数据到明细表
				insert = "insert into TBusRetailDt(MasterId,Sku,ModDis,Qty,Price,Famount,RetailType) " +
						 "select a.masterid_sys,a.Sku,:ModDis,a.Qty,c.DPrice,a.Famount,:RetailType " +
						 "from TBusRetail_tmp a,TDefSku b,TDefStyle c "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.MasterId = :MasterId and not exists( " +
						 "select 'x' from TBusRetailDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				paramMap.put("ModDis", "0");
				paramMap.put("RetailType", "1");
				njdbcTemplate.update(insert, paramMap);
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				//没有数据插入，跳过本次存储过程执行
				if(!procedure_exec){
					//手工更新 TBusRetail 表 中 审核、验收、记账
					update = "update TBusRetail set " +
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
				}
					 
							
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
			
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员";
			msg = str ;
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusSalRet(List<String[]> DataArray,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			//TBusSalRet 抬头明细表
			for (String[] strings : DataArray) {
				insert ="insert into TBusSalRet_TMP(MasterId, BillDate, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '0', '', :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusSalRet_TMP where ( (MasterId = :MasterId and Sku = :Sku and status !='2') or (MasterId = :MasterId and status ='1' ) )  )";
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[0]);
				paramMap.put("BillDate", strings[1]);
				paramMap.put("Orig", strings[2]);
				paramMap.put("Dest", strings[3]);
				paramMap.put("DateOut", strings[4]);
				paramMap.put("DateIn", strings[5]);
				paramMap.put("Opr", strings[6]);
				paramMap.put("OpDate", strings[7]);
				paramMap.put("Sku", strings[8]);
				paramMap.put("PreQty", strings[9]);
				paramMap.put("QtyOut", strings[10]);
				paramMap.put("QtyIn", strings[11]);
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusSalRet_TMP(MasterId, BillDate, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, PreQty, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :PreQty, :QtyOut, :QtyIn, '2', '销售退货明细重复插入', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
					
			//更新不能插入的明细记录状态为 2 
			/*update ="update TBusSalRet_tmp set status = 2,note='1.明细重复' " +
					"where exists(select 'x' from TBusSalRetDt where TBusSalRet_tmp.MasterId=TBusSalRetDt.MasterId and TBusSalRet_tmp.Sku=TBusSalRetDt.Sku )";
			jdbcTemplate.update(update);*/
			
			//读取临时表 插入到正式表 ， 同步
			//获取masterid 
			query = "select distinct MasterId,BillDate,Orig,Dest,Opr,OpDate,DateOut,DateIn " +
					"from TBusSalRet_TMP where status = 0 ";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
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
					insert = "insert into TBusSalRet(MasterId,BillDate,ReturnType,Orig,Dest,Opr,OpDate,DateOut,DateIn) " +
							 "select :MasterId_sys,:BillDate,:ReturnType,:Orig,:Dest,:Opr,:OpDate,:DateOut,:DateIn " +
							 "where not exists( select 'x' from TBusSalRet where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("ReturnType", "1");
					paramMap.put("Orig", Orig);
					paramMap.put("Dest", Dest);
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					paramMap.put("DateOut", DateOut);
					paramMap.put("DateIn", DateIn);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusSalRet_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 ";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusSalRet_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusSalRet失败' " +
								 "where MasterId = :MasterId and status =0 ";
					}
					njdbcTemplate.update(update, paramMap);
					
					//判断单据出库日期、入库日期与盘点日期，决定执行存储过程 or 手工更新表
					query = "select count(*) from TDefStore " +
							"where (store = :Orig and isnull(panddate,'1990-01-01')<=:DateOut) or " +
							"(store = :Dest and isnull(panddate,'1990-01-01')<=:DateIn) ";
					if(njdbcTemplate.queryForInt(query, paramMap)<2)
						procedure_exec = false;
					
				}
				
				//插入数据到明细表
				insert = "insert into TBusSalRetDt(MasterId,Sku,PreQty,QtyOut,QtyIn,DPrice) " +
						 "select a.masterid_sys,a.Sku,a.PreQty,a.QtyOut,a.QtyIn,c.Price " +
						 "from TBusSalRet_tmp a,TDefSku b,TDefStyle c "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.MasterId = :MasterId and not exists(" +
						 "select 'x' from TBusSalRetDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				njdbcTemplate.update(insert, paramMap);
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				//没有数据插入，跳过本次存储过程执行
				if(!procedure_exec){
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
				}
					 
				
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
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员";
			msg = str ;
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusTran(List<String[]> DataArray,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			//TBusTran 抬头明细表
			for (String[] strings : DataArray) {
				insert ="insert into TBusTran_TMP(MasterId, BillDate, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, QtyTran, QtyOut, QtyIn, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :QtyTran, :QtyOut, :QtyIn, '0', '', :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusTran_TMP where ( (MasterId = :MasterId and Sku = :Sku and status !='2') or (MasterId = :MasterId and status ='1' ) )  )";
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[0]);
				paramMap.put("BillDate", strings[1]);
				paramMap.put("Orig", strings[2]);
				paramMap.put("Dest", strings[3]);
				paramMap.put("DateOut", strings[4]);
				paramMap.put("DateIn", strings[5]);
				paramMap.put("Opr", strings[6]);
				paramMap.put("OpDate", strings[7]);
				paramMap.put("Sku", strings[8]);
				paramMap.put("QtyTran", strings[9]);
				paramMap.put("QtyOut", strings[10]);
				paramMap.put("QtyIn", strings[11]);
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusTran_TMP(MasterId, BillDate, Orig, Dest, Opr, OpDate, DateOut, DateIn, Sku, QtyTran, QtyOut, QtyIn, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Orig, :Dest, :Opr, :OpDate, :DateOut, :DateIn, :Sku, :QtyTran, :QtyOut, :QtyIn, '2', '调拨明细重复插入', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
					
			//更新不能插入的明细记录状态为 2 
			/*update ="update TBusTran_tmp set status = 2,note='1.明细重复' " +
					"where exists(select 'x' from TBusTranDt where TBusTran_tmp.MasterId=TBusTranDt.MasterId and TBusTran_tmp.Sku=TBusTranDt.Sku )";
			jdbcTemplate.update(update);*/
			
			//读取临时表 插入到正式表 ， 同步
			//获取masterid 
			query = "select distinct MasterId,BillDate,Orig,Dest,Opr,OpDate,DateOut,DateIn " +
					"from TBusTran_TMP where status = 0 ";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
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
					insert = "insert into TBusTran(MasterId,BillDate,TranType,TranFlag,OutType,Orig,Dest,Opr,OpDate,DateOut,DateIn) " +
							 "select :MasterId_sys,:BillDate,:TranType,:TranFlag,:OutType,:Orig,:Dest,:Opr,:OpDate,:DateOut,:DateIn " +
							 "where not exists( select 'x' from TBusTran where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("TranType", "1");
					paramMap.put("TranFlag", "0");
					paramMap.put("OutType", "3");
					paramMap.put("Orig", Orig);
					paramMap.put("Dest", Dest);
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					paramMap.put("DateOut", DateOut);
					paramMap.put("DateIn", DateIn);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusTran_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 ";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusTran_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusTran失败' " +
								 "where MasterId = :MasterId and status =0 ";
					}
					njdbcTemplate.update(update, paramMap);
					
					//判断单据出库日期、入库日期与盘点日期，决定执行存储过程 or 手工更新表
					query = "select count(*) from TDefStore " +
							"where (store = :Orig and isnull(panddate,'1990-01-01')<=:DateOut) or " +
							"(store = :Dest and isnull(panddate,'1990-01-01')<=:DateIn) ";
					if(njdbcTemplate.queryForInt(query, paramMap)<2)
						procedure_exec = false;
					
				}
				
				//插入数据到明细表
				insert = "insert into TBusTranDt(MasterId,Sku,QtyTran,QtyOut,QtyIn,DPrice) " +
						 "select a.masterid_sys,a.Sku,a.QtyTran,a.QtyOut,a.QtyIn,c.Price " +
						 "from TBusTran_tmp a,TDefSku b,TDefStyle c "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.MasterId = :MasterId and not exists(" +
						 "select 'x' from TBusTranDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				njdbcTemplate.update(insert, paramMap);
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				//没有数据插入，跳过本次存储过程执行
				if(!procedure_exec){
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
				}
					 
				
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
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员";
			msg = str ;
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusAdj(List<String[]> DataArray,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			//TBusAdj 抬头明细表
			for (String[] strings : DataArray) {
				insert ="insert into TBusAdj_TMP(MasterId, BillDate, Store, Remark, Opr, OpDate, Sku, Qty, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate, :Store, :Remark, :Opr, :OpDate, :Sku, :Qty, '0', '', :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusAdj_TMP where ( (MasterId = :MasterId and Sku = :Sku and status !='2') or (MasterId = :MasterId and status ='1' ) )  )";
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[0]);
				paramMap.put("BillDate", strings[1]);
				paramMap.put("Store", strings[2]);
				paramMap.put("Remark", strings[3]);
				paramMap.put("Opr", strings[4]);
				paramMap.put("OpDate", strings[5]);
				paramMap.put("Sku", strings[6]);
				paramMap.put("Qty", strings[7]);
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusAdj_TMP(MasterId, BillDate, Store, Remark, Opr, OpDate, Sku, Qty, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Store, :Remark, :Opr, :OpDate, :Sku, :Qty, '0', ''明细插入重复, :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
					
			//更新不能插入的明细记录状态为 2 
			/*update ="update TBusAdj_tmp set status = 2,note='1.明细重复' " +
					"where exists(select 'x' from TBusAdjDt where TBusAdj_tmp.MasterId=TBusAdjDt.MasterId and TBusAdj_tmp.Sku=TBusAdjDt.Sku )";
			jdbcTemplate.update(update);*/
			
			//读取临时表 插入到正式表 ， 同步
			//获取masterid 
			query = "select distinct MasterId,BillDate,Store,Remark,Opr,OpDate " +
					"from TBusAdj_TMP where status = 0 ";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
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
					insert = "insert into TBusAdj(MasterId,BillDate,Store,Remark,Opr,OpDate,AdjType) " +
							 "select :MasterId_sys,:BillDate,:Store,:Remark,:Opr,:OpDate,'1' " +
							 "where not exists( select 'x' from TBusAdj where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("Store", Store);
					paramMap.put("Remark", Remark);
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusAdj_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 ";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusAdj_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusAdj失败' " +
								 "where MasterId = :MasterId and status =0 ";
					}
					njdbcTemplate.update(update, paramMap);
					
					//判断单据日期与盘点日期，决定执行存储过程 or 手工更新表
					query = "select count(*) from TDefStore " +
							"where (Store = :Store and isnull(panddate,'1990-01-01')<=:BillDate) ";
					if(njdbcTemplate.queryForInt(query, paramMap)<1)
						procedure_exec = false;
					
				}
				
				//插入数据到明细表
				insert = "insert into TBusAdjDt(MasterId,Sku,Qty,DPrice) " +
						 "select a.masterid_sys,a.Sku,a.Qty,c.Price " +
						 "from TBusAdj_tmp a,TDefSku b,TDefStyle c "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.MasterId = :MasterId and not exists(" +
						 "select 'x' from TBusAdjDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				njdbcTemplate.update(insert, paramMap);
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				//没有数据插入，跳过本次存储过程执行
				if(!procedure_exec){
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
				}
				
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
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员";
			msg = str ;
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String TBusPand(List<String[]> DataArray,String Account) {
		String msg = "";
		int result = 0;
		try{
			String insert ="",query="",update="",delete="";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String addtime =  sdf.format(new Date());
			//TBusPand 抬头明细表
			for (String[] strings : DataArray) {
				insert ="insert into TBusPand_TMP(MasterId, BillDate, Store, Opr, OpDate, Sku, PreQty, status, note, addwho, addtime)  " +
						"select :MasterId, :BillDate, :Store, :Opr, :OpDate, :Sku, :PreQty, '0', '', :addwho, :addtime " +
						"where not exists(" +
						"select 'x' from TBusPand_TMP where ( (MasterId = :MasterId and Sku = :Sku and status !='2') or (MasterId = :MasterId and status ='1' ) )  )";
				
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", strings[0]);
				paramMap.put("BillDate", strings[1]);
				paramMap.put("Store", strings[2]);
				paramMap.put("Opr", strings[3]);
				paramMap.put("OpDate", strings[4]);
				paramMap.put("Sku", strings[5]);
				paramMap.put("PreQty", strings[6]);
				paramMap.put("addwho", Account);
				paramMap.put("addtime", addtime);
				
				if(njdbcTemplate.update(insert, paramMap)==0){
					insert ="insert into TBusPand_TMP(MasterId, BillDate, Store, Opr, OpDate, Sku, PreQty, status, note, addwho, addtime)  " +
							"select :MasterId, :BillDate, :Store, :Opr, :OpDate, :Sku, :PreQty, '0', '明细重复插入', :addwho, :addtime " ;
					njdbcTemplate.update(insert, paramMap);
				}
				
			}
					
			//更新不能插入的明细记录状态为 2 
			/*update ="update TBusPand_tmp set status = 2,note='1.明细重复' " +
					"where exists(select 'x' from TBusPandDt where TBusPand_tmp.MasterId=TBusPandDt.MasterId and TBusPand_tmp.Sku=TBusPandDt.Sku )";
			jdbcTemplate.update(update);*/
			
			//读取临时表 插入到正式表 ， 同步
			//获取masterid 
			query = "select distinct MasterId,BillDate,Store,Opr,OpDate " +
					"from TBusPand_TMP where status = 0 ";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MASTERID").toString();
				String BillDate = map_data.get("BillDate").toString();
				String Store = map_data.get("Store").toString();
				String Opr = map_data.get("Opr").toString();
				String OpDate = map_data.get("OpDate").toString();
								
				String MasterId_sys = "";
				Boolean procedure_exec = true ;
				
				//对比当前盘点日期，决定是否更新库存
				query = "select count(*) from TDefStore " +
						"where (Store = :Store and isnull(panddate,'1990-01-01')<=:BillDate) ";
				Map<String,Object> paramPand = new HashMap<String, Object>();
				paramPand.put("Store", Store);
				paramPand.put("BillDate", BillDate);
				
				if(njdbcTemplate.queryForInt(query, paramPand)<1){ //盘点日期有误,不能插入记录
					update = "update  TBusPand_TMP set status = 2,note = '盘点日期不符合' " +
							 "where MasterId = :MasterId and status =0 ";
					paramPand.put("MasterId", MasterId);
					njdbcTemplate.update(update, paramPand);
					continue;
				}
				
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
					paramMap.put("BillDate", BillDate);
					paramMap.put("Store", Store);
					paramMap.put("Remark", "盘点库存");
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusPand_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 ";
					}else{
						procedure_exec = false; //不执行存储过程
						update = "update  TBusPand_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusPand失败' " +
								 "where MasterId = :MasterId and status =0 ";
					}
					njdbcTemplate.update(update, paramMap);
					
				}
				
				//插入数据到明细表
				insert = "insert into TBusPandDt(MasterId,Sku,PreQty,DocQty,DPrice) " +
						 "select a.masterid_sys,a.Sku,a.PreQty,d.Qty DocQty,c.Price " +
						 "from TBusPand_tmp a,TDefSku b,TDefStyle c,TAccStock d "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.Store = d.Store and a.Sku = d.Sku and " +
						 "a.MasterId = :MasterId and not exists(" +
						 "select 'x' from TBusPandDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				njdbcTemplate.update(insert, paramMap);
				
				//执行相应的存储过程 
				String procedure = "";
				final String MasterId_sys_p = MasterId_sys;
				
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
				
				//更新店仓的最后盘点日期为单据日期
				update = "update TDefStore set PandDate = :BillDate where Store = :Store ";
				Map<String,Object> paramPandDateMap = new HashMap<String, Object>();
				paramPandDateMap.put("BillDate", BillDate);
				paramPandDateMap.put("Store", Store);
				njdbcTemplate.update(update, paramPandDateMap);
				
				
			}
			result = 1 ;
		}catch(Exception e){
			String str = "处理执行过程中出错`请检查数据是否有误或联系管理员";
			msg = str ;
			throw new RuntimeException(e);
		}
		// TODO Auto-generated method stub
		return result > 0 ? "1" : msg;
	}

}
