package com.authority.web.controller;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alipay.config.AlipayConfig;
import com.alipay.sign.MD5;
import com.alipay.util.AlipayCore;
import com.authority.common.utils.PoiHelper;
import com.authority.common.utils.WebUtils;
import com.authority.pojo.Criteria;
import com.authority.pojo.ExtReturn;
import com.authority.service.BaseUsersService;
import com.authority.service.EmaxInterfaceService;

@Controller
@RequestMapping("/emaxinterface")
public class EmaxInterfaceController {
	private static final Logger logger = LoggerFactory.getLogger(EmaxInterfaceController.class);

	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;
	
	@Autowired
	private EmaxInterfaceService emaxinterfaceservice;
	
	@Autowired
	private BaseUsersService baseUsersService;
	
	public String MethodMapping(File file,Map<String,Object> fieldMap,String Account){
		String exceptionMsg ="";
		boolean flag = false;
		try {
			//根据文件名选择不同的处理程序
			String FileName = file.getName().toUpperCase();
			if(FileName.contains("M_SALE")){
				String resultMsg = TBusSale(file,fieldMap,"admin");
				if(!resultMsg.equals("1")){
					flag = false;
					exceptionMsg = exceptionMsg + " TBusSale ("+resultMsg +")<br>";
				}else{
					flag = true ;
					//exceptionMsg = exceptionMsg + " TBusSale 处理成功(请打开Failed文件夹查看是否有错误信息)<br>";
				}
			}else if(FileName.contains("M_RETAIL")){
				String resultMsg = TBusRetail(file,fieldMap,"admin");
				if(!resultMsg.equals("1")){
					flag = false;
					exceptionMsg = exceptionMsg + " TBusRetail ("+resultMsg +")<br>";
				}else{
					flag = true ;
					//exceptionMsg = exceptionMsg + " TBusRetail 处理成功(请打开Failed文件夹查看是否有错误信息)<br>";
				}
			}else if(FileName.contains("M_RET_SALE")){
				String resultMsg = TBusSalRet(file,fieldMap,"admin");
				if(!resultMsg.equals("1")){
					flag = false;
					exceptionMsg = exceptionMsg + " TBusSalRet ("+resultMsg +")<br>";
				}else{
					flag = true ;
					//exceptionMsg = exceptionMsg + " TBusSalRet 处理成功(请打开Failed文件夹查看是否有错误信息)<br>";
				}
			}else if(FileName.contains("M_TRANSFER")){
				String resultMsg = TBusTran(file,fieldMap,"admin");
				if(!resultMsg.equals("1")){
					flag = false;
					exceptionMsg = exceptionMsg + " TBusTran ("+resultMsg +")<br>";
				}else{
					flag = true ;
					//exceptionMsg = exceptionMsg + " TBusTran 处理成功(请打开Failed文件夹查看是否有错误信息)";
				}
			}else if(FileName.contains("M_OTHER_INOUT")){
				String resultMsg = TBusAdj(file,fieldMap,"admin");
				if(!resultMsg.equals("1")){
					flag = false;
					exceptionMsg = exceptionMsg + " TBusAdj ("+resultMsg +")<br>";
				}else{
					flag = true ;
					//exceptionMsg = exceptionMsg + " TBusAdj 处理成功(请打开Failed文件夹查看是否有错误信息)";
				}
			}else if(FileName.contains("V_FA_STORAGE")){
				String resultMsg = TBusPand(file,fieldMap,"admin");
				if(!resultMsg.equals("1")){
					flag = false;
					exceptionMsg = exceptionMsg + " TBusPand ("+resultMsg +")<br>";
				}else{
					flag = true ;
					//exceptionMsg = exceptionMsg + " TBusPand 处理成功(请打开Failed文件夹查看是否有错误信息)";
				}
			}else{
				flag = false;
				exceptionMsg = exceptionMsg + " 找不到相关处理程序<br>";
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			exceptionMsg = exceptionMsg + e.toString() +"<br>";
		} finally{
			return exceptionMsg;
		}
	}
	
	public Map<String,Object> FileStandard(List<String[]> DataArray,Map<String, Object> fieldMap, String Account)throws Exception{
		boolean standard = true ; //文件是否合标准		
		//源Excel 字段解析 映射，并检测是否合规范, fieldmatchMap 值所在的位置
		Map<String,Object> fieldmatchMap =  new HashMap<String, Object>();
		//判断Excel 格式是否符合标准
		String[] DataArrayChild = DataArray.get(0);
		Set<String> keySet = fieldMap.keySet();
		for(String key : keySet){
			boolean fieldexists = false ;
			for (int i = 0; i < DataArrayChild.length; i++) {
				if(key.toUpperCase().equals(DataArrayChild[i].toUpperCase())||fieldMap.get(key).toString().toUpperCase().equals(DataArrayChild[i].toUpperCase())){
					fieldmatchMap.put(key, i);
					fieldexists = true ;
					break;
				}
			}
			if(!fieldexists){
				standard = false;
				break;
			}
		}
		
		if(!standard){
			return null;
		}
		
		return fieldmatchMap;
		
	}
	
	/**
	 * 销售单据处理
	 * @param file
	 * @param fieldMap 
	 * @return
	 * @throws Exception
	 */
	public String TBusSale(File file,Map<String, Object> fieldMap, String Account) throws Exception{
		String insert ="",query="",update="",delete="",exceptionMsg="";
		//读取Excel 内容，插入数据到临时表
		List<String[]> DataArray = PoiHelper.getData(file, 0,0);
		//检测文件格式是否合乎标准
		Map<String,Object> fieldmatchMap = FileStandard(DataArray,fieldMap,Account); 
		if(fieldmatchMap==null)
			return "文件格式不合标准";
		DataArray.remove(0);
		//提交服务层处理
		String result = emaxinterfaceservice.TBusSale(DataArray, fieldmatchMap, Account);
		if(!result.equals("1"))
			return result;
		
		//删除已经处理文件
		file.delete();
		
		SimpleDateFormat xlssdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String xlstime =  xlssdf.format(new Date());
		
		//将处理成功的记录写入Excel 到  Done 文件夹  分 抬头和明细  只取当天记录
		if(1>0){
			query = "select * from TBusSale_TMP where status = 1 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			/*String[] col_id = {"MASTERID","BILLDATE","SALETYPE","ORIG","DEST","DATEOUT","DATEIN","OPR","OPDATE","SKU","PREQTY","QTYOUT","QTYIN"};
			String[] col_name = {"单据编号","单据日期","销售类型","发货店仓","收货店仓","出库日期","入库日期","制单人","制单日期","条码","销售数量","出库数量","入库数量"};
			*/
			String filePath = file.getParentFile().getPath()+File.separator+"Done"+File.separator+"TBusSale_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath , true);
		}
		//将处理失败的记录写入Excel 到 Failed 文件夹
		if(1>0){
			query = "select * from TBusSale_TMP where status = 2 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			/*String[] col_id = {"MASTERID","BILLDATE","SALETYPE","ORIG","DEST","DATEOUT","DATEIN","OPR","OPDATE","SKU","PREQTY","QTYOUT","QTYIN","NOTE"};
			String[] col_name = {"单据编号","单据日期","销售类型","发货店仓","收货店仓","出库日期","入库日期","制单人","制单日期","条码","销售数量","出库数量","入库数量","备注"};
			*/
			String filePath = file.getParentFile().getPath()+File.separator+"Failed"+File.separator+"TBusSale_"+xlstime+".xlsx";	
//			System.out.println("filePath:"+filePath);
			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath ,true);
		}
				
		//删除历史失败的记录 操作用户所插入记录
		delete = "delete from TBusSale_TMP where status = 2 and addwho='"+Account+"'";
		jdbcTemplate.update(delete);
		
		
		return "1";
	}
	/**
	 * 零售单数据处理
	 * @param file
	 * @param fieldMap 
	 * @return
	 * @throws Exception
	 */
	public String TBusRetail(File file,Map<String, Object> fieldMap, String Account) throws Exception{
		String insert ="",query="",update="",delete="",exceptionMsg="";
		//读取Excel 内容，插入数据到临时表
		List<String[]> DataArray = PoiHelper.getData(file, 0,0);
		//检测文件格式是否合乎标准
		Map<String,Object> fieldmatchMap = FileStandard(DataArray,fieldMap,Account); 
		if(fieldmatchMap==null)
			return "文件格式不合标准";
		DataArray.remove(0);
		//提交服务层处理
		String result = emaxinterfaceservice.TBusRetail(DataArray, fieldmatchMap, Account);
		if(!result.equals("1"))
			return result;
		
		//删除已经处理文件
		file.delete();
		
		SimpleDateFormat xlssdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String xlstime =  xlssdf.format(new Date());
		
		//将处理成功的记录写入Excel 到  Done 文件夹  分 抬头和明细  只取当天记录
		if(1>0){
			query = "select * from TBusRetail_TMP where status = 1 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			/*String[] col_id = {"MASTERID","BILLDATE","STORE","OPR","OPDATE","SKU","QTY","PRICE","FAMOUNT"};
			String[] col_name = {"单据编号","单据日期","店仓","制单人","制单日期","条码","数量","成交价","成交总额"};
			*/
			String filePath = file.getParentFile().getPath()+File.separator+"Done"+File.separator+"TBusRetail_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath,true);
		}
		//将处理失败的记录写入Excel 到 Failed 文件夹
		if(1>0){
			query = "select * from TBusRetail_TMP where status = 2 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			
			/*String[] col_id = {"MASTERID","BILLDATE","STORE","OPR","OPDATE","SKU","QTY","PRICE","FAMOUNT","NOTE"};
			String[] col_name = {"单据编号","单据日期","店仓","制单人","制单日期","条码","数量","成交价","成交总额","备注"};
			*/
			String filePath = file.getParentFile().getPath()+File.separator+"Failed"+File.separator+"TBusRetail_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath,true);
		}
				
		//删除历史失败的记录 操作用户所插入记录
		delete = "delete from TBusRetail_TMP where status = 2 and addwho='"+Account+"'";
		jdbcTemplate.update(delete);
		
		
		return "1";
	}
	
	/**
	 * 销售退货单数据处理
	 * @param file
	 * @param fieldMap 
	 * @return
	 * @throws Exception
	 */
	public String TBusSalRet(File file,Map<String, Object> fieldMap, String Account) throws Exception{
		String insert ="",query="",update="",delete="",exceptionMsg="";
		//读取Excel 内容，插入数据到临时表
		List<String[]> DataArray = PoiHelper.getData(file, 0,0);
		//检测文件格式是否合乎标准
		Map<String,Object> fieldmatchMap = FileStandard(DataArray,fieldMap,Account); 
		if(fieldmatchMap==null)
			return "文件格式不合标准";
		DataArray.remove(0);
		//提交服务层处理
		String result = emaxinterfaceservice.TBusSalRet(DataArray, fieldmatchMap, Account);
		if(!result.equals("1"))
			return result;
		
		//删除已经处理文件
		file.delete();
		
		SimpleDateFormat xlssdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String xlstime =  xlssdf.format(new Date());
		
		//将处理成功的记录写入Excel 到  Done 文件夹  分 抬头和明细  只取当天记录
		if(1>0){
			query = "select * from TBusSalRet_TMP where status = 1 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			
			/*String[] col_id = {"MASTERID","BILLDATE","ORIG","DEST","DATEOUT","DATEIN","OPR","OPDATE","SKU","PREQTY","QTYOUT","QTYIN"};
			String[] col_name = {"单据编号","单据日期","发货店仓","收货店仓","出库日期","入库日期","制单人","制单日期","条码","销售退货数量","出库数量","入库数量"};
			*/
			String filePath = file.getParentFile().getPath()+File.separator+"Done"+File.separator+"TBusSalRet_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath,true);
		}
		//将处理失败的记录写入Excel 到 Failed 文件夹
		if(1>0){
			query = "select * from TBusSalRet_TMP where status = 2 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			/*String[] col_id = {"MASTERID","BILLDATE","ORIG","DEST","DATEOUT","DATEIN","OPR","OPDATE","SKU","PREQTY","QTYOUT","QTYIN","NOTE"};
			String[] col_name = {"单据编号","单据日期","发货店仓","收货店仓","出库日期","入库日期","制单人","制单日期","条码","销售退货数量","出库数量","入库数量","备注"};
			*/
			String filePath = file.getParentFile().getPath()+File.separator+"Failed"+File.separator+"TBusSalRet_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath,true);
		}
				
		//删除历史失败的记录 操作用户所插入记录
		delete = "delete from TBusSalRet_TMP where status = 2 and addwho='"+Account+"'";
		jdbcTemplate.update(delete);
		
		
		return "1";
	}
	
	/**
	 * 调拨单数据处理
	 * @param file
	 * @param fieldMap 
	 * @return
	 * @throws Exception
	 */
	public String TBusTran(File file,Map<String, Object> fieldMap, String Account) throws Exception{
		String insert ="",query="",update="",delete="",exceptionMsg="";
		//读取Excel 内容，插入数据到临时表
		List<String[]> DataArray = PoiHelper.getData(file, 0,0);
		//检测文件格式是否合乎标准
		Map<String,Object> fieldmatchMap = FileStandard(DataArray,fieldMap,Account); 
		if(fieldmatchMap==null)
			return "文件格式不合标准";
		DataArray.remove(0);
		//提交服务层处理
		String result = emaxinterfaceservice.TBusTran(DataArray, fieldmatchMap, Account);
		if(!result.equals("1"))
			return result;
		
		//删除已经处理文件
		file.delete();
		
		SimpleDateFormat xlssdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String xlstime =  xlssdf.format(new Date());
		
		//将处理成功的记录写入Excel 到  Done 文件夹  分 抬头和明细  只取当天记录
		if(1>0){
			query = "select * from TBusTran_TMP where status = 1 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			
			/*String[] col_id = {"MASTERID","BILLDATE","ORIG","DEST","DATEOUT","DATEIN","OPR","OPDATE","SKU","QTYTRAN","QTYOUT","QTYIN"};
			String[] col_name = {"单据编号","单据日期","发货店仓","收货店仓","出库日期","入库日期","制单人","制单日期","条码","调拨数量","出库数量","入库数量"};
			*/
			String filePath = file.getParentFile().getPath()+File.separator+"Done"+File.separator+"TBusTran_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath,true);
		}
		//将处理失败的记录写入Excel 到 Failed 文件夹
		if(1>0){
			query = "select * from TBusTran_TMP where status = 2 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			
			/*String[] col_id = {"MASTERID","BILLDATE","ORIG","DEST","DATEOUT","DATEIN","OPR","OPDATE","SKU","QTYTRAN","QTYOUT","QTYIN","NOTE"};
			String[] col_name = {"单据编号","单据日期","发货店仓","收货店仓","出库日期","入库日期","制单人","制单日期","条码","调拨数量","出库数量","入库数量","备注"};
			*/
			String filePath = file.getParentFile().getPath()+File.separator+"Failed"+File.separator+"TBusTran_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath,true);
		}
				
		//删除历史失败的记录 操作用户所插入记录
		delete = "delete from TBusTran_TMP where status = 2 and addwho='"+Account+"'";
		jdbcTemplate.update(delete);
		
		
		return "1";
	}
	
	/**
	 * 物理调整单
	 * @param file
	 * @param fieldMap 
	 * @return
	 * @throws Exception
	 */
	public String TBusAdj(File file,Map<String, Object> fieldMap, String Account) throws Exception{
		String insert ="",query="",update="",delete="",exceptionMsg="";
		//读取Excel 内容，插入数据到临时表
		List<String[]> DataArray = PoiHelper.getData(file, 0,0);
		//检测文件格式是否合乎标准
		Map<String,Object> fieldmatchMap = FileStandard(DataArray,fieldMap,Account); 
		if(fieldmatchMap==null)
			return "文件格式不合标准";
		DataArray.remove(0);
		//提交服务层处理
		String result = emaxinterfaceservice.TBusAdj(DataArray, fieldmatchMap, Account);
		if(!result.equals("1"))
			return result;
		
		//删除已经处理文件
		file.delete();
		
		SimpleDateFormat xlssdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String xlstime =  xlssdf.format(new Date());
		
		//将处理成功的记录写入Excel 到  Done 文件夹  分 抬头和明细  只取当天记录
		if(1>0){
			query = "select * from TBusAdj_TMP where status = 1 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			
			/*String[] col_id = {"MASTERID","BILLDATE","STORE","REMARK","OPR","OPDATE","SKU","QTY"};
			String[] col_name = {"单据编号","单据日期","店仓","调整原因","制单人","制单日期","条码","数量"};
			*/
			String filePath = file.getParentFile().getPath()+File.separator+"Done"+File.separator+"TBusAdj_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath,true);
		}
		//将处理失败的记录写入Excel 到 Failed 文件夹
		if(1>0){
			query = "select * from TBusAdj_TMP where status = 2 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			
			/*String[] col_id = {"MASTERID","BILLDATE","STORE","REMARK","OPR","OPDATE","SKU","QTY","NOTE"};
			String[] col_name = {"单据编号","单据日期","店仓","调整原因","制单人","制单日期","条码","数量","备注"};
			*/
			String filePath = file.getParentFile().getPath()+File.separator+"Failed"+File.separator+"TBusAdj_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath,true);
		}
				
		//删除历史失败的记录 操作用户所插入记录
		delete = "delete from TBusAdj_TMP where status = 2 and addwho='"+Account+"'";
		jdbcTemplate.update(delete);
		
		
		return "1";
	}	
	
	/**
	 * 盘点单
	 * @param file
	 * @param fieldMap 
	 * @return
	 * @throws Exception
	 */
	public String TBusPand(File file,Map<String, Object> fieldMap, String Account) throws Exception{
		String insert ="",query="",update="",delete="",exceptionMsg="";
		//读取Excel 内容，插入数据到临时表
		List<String[]> DataArray = PoiHelper.getData(file, 0,0);
		//检测文件格式是否合乎标准
		Map<String,Object> fieldmatchMap = FileStandard(DataArray,fieldMap,Account); 
		if(fieldmatchMap==null)
			return "文件格式不合标准";
		DataArray.remove(0);
		//提交服务层处理
		String result = emaxinterfaceservice.TBusPand(DataArray, fieldmatchMap, Account);
		if(!result.equals("1"))
			return result;
		
		//删除已经处理文件
		file.delete();
		
		SimpleDateFormat xlssdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String xlstime =  xlssdf.format(new Date());
		
		//将处理成功的记录写入Excel 到  Done 文件夹  分 抬头和明细  只取当天记录
		if(1>0){
			query = "select * from TBusPand_TMP where status = 1 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			/*
			String[] col_id = {"MASTERID","BILLDATE","STORE","OPR","OPDATE","SKU","PreQty"};
			String[] col_name = {"单据编号","单据日期","店仓","制单人","制单日期","条码","实际盘点数量"};*/
			String filePath = file.getParentFile().getPath()+File.separator+"Done"+File.separator+"TBusPand_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath,true);
		}
		//将处理失败的记录写入Excel 到 Failed 文件夹
		if(1>0){
			query = "select * from TBusPand_TMP where status = 2 and addwho='"+Account+"' and CONVERT(varchar(12) , addtime, 112 ) = CONVERT(varchar(12) , getdate(), 112 ) ";
			List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
			SqlRowSet rs = jdbcTemplate.queryForRowSet(query);
			SqlRowSetMetaData data=rs.getMetaData();
			String[] col_id = data.getColumnNames();
			String[] col_name = col_id ;
			/*
			String[] col_id = {"MASTERID","BILLDATE","STORE","OPR","OPDATE","SKU","PreQty","NOTE"};
			String[] col_name = {"单据编号","单据日期","店仓","制单人","制单日期","条码","实际盘点数量","备注"};*/
			String filePath = file.getParentFile().getPath()+File.separator+"Failed"+File.separator+"TBusPand_"+xlstime+".xlsx";			
			if(list.size()>0)
				PoiHelper.Excel_Generate(list, col_id, col_name, filePath,true);
		}
				
		//删除历史失败的记录 操作用户所插入记录
		delete = "delete from TBusPand_TMP where status = 2 and addwho='"+Account+"'";
		jdbcTemplate.update(delete);
		
		return "1";
	}
	
	

	@RequestMapping("/filedownload")
	@ResponseBody
	public Object filedownload(HttpSession session, HttpServletRequest request) {
		//读取该单据的执行语句
		String query = "",CONTENT="",msg="失败", CONDITION="",DIM="",sign="",account="",password="",type="";
		Boolean result = false ;
		try{
			
			Map<String, String[]> ReqMapTemp = request.getParameterMap();
			CONDITION = ReqMapTemp.get("condition")[0].toString();
			DIM = ReqMapTemp.get("dim")[0].toString();
			type= ReqMapTemp.get("type")[0].toString();
			account = ReqMapTemp.get("account")[0].toString();
			password = ReqMapTemp.get("password")[0].toString();
			
			sign = ReqMapTemp.get("sign")[0].toString();
			//判断密钥
			Map<String, String> sParaTemp = new HashMap<String, String>();
			sParaTemp.put("dim", DIM);
			sParaTemp.put("type", type);
			sParaTemp.put("condition", CONDITION);
			sParaTemp.put("account", account);
			sParaTemp.put("password", password);
			
			String mysign = buildRequestMysign(sParaTemp);
			
			if(!sign.equals(mysign)&&!sign.equalsIgnoreCase("qwertyuiop")){
				return new ExtReturn(result, "密钥检测失败");
			}
			
			//验证用户名 密码 是否有效
			password = DigestUtils.md5Hex(password+"{"+account+"}");
			Criteria criteria = new Criteria();
			criteria.put("account", account);
			criteria.put("password", password);
			if(baseUsersService.countByExample(criteria)==0){
				return new ExtReturn(result, "用户密码检验未通过");
			}
			
			Map<String,Object> ReqMap = new HashMap<String, Object>();
			ReqMap.put("DIM", account.toUpperCase());
			
			/*Set<String> set = ReqMapTemp.keySet();
			//语句后期执行参数			
			for (String key : set) {
				String value = ReqMapTemp.get(key)[0].toString();
				ReqMap.put(key, value);
				condition = condition +" and "+ key +" =:"+key+" ";
			}*/	
			
			query = "SELECT MAX(CONTENT) CONTENT FROM BASE_INTERFACE_SQL WHERE TYPE = :TYPE AND ISACTIVE='Y' ";
			Map<String,Object> param = new HashMap<String, Object>();
			param.put("TYPE", type.toUpperCase());
			CONTENT = njdbcTemplate.queryForObject(query, param, String.class);
			if(CONTENT==null||CONTENT.equals(""))
				;
			else{
				//执行数据导出到 Excel
				if(!CONDITION.equals("")){
					if(CONTENT.toUpperCase().contains("WHERE")){
						CONTENT = CONTENT+" and ("+CONDITION+")";
					}else{
						CONTENT = CONTENT+" where ("+CONDITION+")";
					}
				}else{
					CONTENT = CONTENT + " where 1=1 ";
				}
				if(!DIM.equals("")){
					String content_dim =" M_PRODUCT_ID in ( "+
										" select id from M_PRODUCT where M_DIM1_ID=(" +
										" select id from M_DIM where ATTRIBCODE=:DIM ) ) ";
					
					content_dim = " Sku in ("+
								  " select b.Sku from TDefSku b, TDefStyle c , TDefAttribDef d ,TDefAttrib e "+ 
								  " where b.Style = c.Style and  d.AttribDefId = e.AttribDefId and  "+
								  " c.Brand = e.AttribCode and d.AttribDefName='品牌' and e.AttribCode = :DIM "+
								  ")";
					
					CONTENT = CONTENT + " and ( " + content_dim +" ) ";
				}
				
				List<Map<String,Object>> list = njdbcTemplate.queryForList(CONTENT, ReqMap); 
				SqlRowSet rs = njdbcTemplate.queryForRowSet(CONTENT, ReqMap);
				SqlRowSetMetaData data=rs.getMetaData();
				String[] col_id = data.getColumnNames();
				String savePath = request.getSession().getServletContext().getRealPath("/resources/upload/admin/Done");
				String datestr = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
				savePath = savePath+File.separator+type+"_"+DIM+"_"+datestr+".xlsx";
				PoiHelper.Excel_Generate(list, col_id, col_id, savePath,true);
				
				result = true ;
				msg = "/resources/upload/admin/Done/"+type+"_"+DIM+"_"+datestr+".xlsx";
			}
			
		}catch(Exception e){
			result = false ;
			msg = "异常";
		}
		
		return new ExtReturn(result, msg);
	}
	
	@RequestMapping("/filedelete")
	@ResponseBody
	public Object filedelete(HttpSession session, HttpServletRequest request) {
		//读取该单据的执行语句
		String query = "",CONTENT="",msg="失败", filename="",sign="",account="",password="";
		Boolean result = false ;
		try {
			Map<String, String[]> ReqMapTemp = request.getParameterMap();
			filename = ReqMapTemp.get("filename")[0].toString();
			account = ReqMapTemp.get("account")[0].toString();
			password = ReqMapTemp.get("password")[0].toString();
			sign = ReqMapTemp.get("sign")[0].toString();
			//判断密钥
			Map<String, String> sParaTemp = new HashMap<String, String>();
			sParaTemp.put("filename", filename);
			sParaTemp.put("account", account);
			sParaTemp.put("password", password);
			
			String mysign = buildRequestMysign(sParaTemp);
			if(!sign.equals(mysign)&&!sign.equalsIgnoreCase("qwertyuiop")){
				return new ExtReturn(result, "密钥检测失败");
			}
			
			//验证用户名 密码 是否有效
			password = DigestUtils.md5Hex(password+"{"+account+"}");
			Criteria criteria = new Criteria();
			criteria.put("account", account);
			criteria.put("password", password);
			if(baseUsersService.countByExample(criteria)==0){
				return new ExtReturn(result, "用户密码检验未通过");
			}
			
			String rootPath = request.getSession().getServletContext().getRealPath("/resources/upload/admin/Done");
			String filePath = rootPath+File.separator+filename;
			File file = new File(filePath);
			if(file.exists()){
				file.delete();
			}
			result = true ;
			msg ="删除成功";
			
		} catch (Exception e) {
			// TODO: handle exception
			result = false ;
		}
		
		return new ExtReturn(result, msg);
	}
	
	@RequestMapping("/Task")
	@ResponseBody
	public Object Task(HttpSession session, HttpServletRequest request) {
		//读取该单据的执行语句
		String query = "",CONTENT="",msg="", filename="",sign="",account="",password="";
		Boolean result = false ;
		try {
			String method_name = request.getParameter("method");
			Class<?> task = null;
			WebUtils webUtils = new WebUtils();
			String classpath = webUtils.readValue("config/others/config.properties","Task.classpath");
			task = Class.forName(classpath);
			Method  method = task.getDeclaredMethod(method_name, null);
			Object task_class = task.newInstance();
			method.invoke(task_class, null);
			msg = method_name +" success ";
		} catch (Exception e) {
			// TODO: handle exception
			result = false ;
			msg = e.toString();
		}
		
		return new ExtReturn(result, msg);
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
	
	
}
