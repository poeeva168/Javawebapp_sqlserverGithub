package com.authority.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.authority.common.springmvc.DateConvertEditor;
import com.authority.common.utils.FileOperateUtil;
import com.authority.pojo.ExceptionReturn;
import com.authority.pojo.PdaReturn;
import com.authority.web.interseptor.WebConstants;

@Controller
@RequestMapping("/uniform/pdaaction")
public class UniformPdaActionController {
	private static final Logger logger = LoggerFactory.getLogger(UniformPdaActionController.class);
	
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;
	
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new DateConvertEditor());
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	
	@RequestMapping(value="/login")
	@ResponseBody	
	public Object login(@RequestParam String account, @RequestParam String password, HttpSession session, HttpServletRequest request) {
		try {
			//0010001 123456
			String result ="00",store=";";
			
			if (StringUtils.isBlank(account)) {
				return new PdaReturn("N", "帐号不能为空！");
			}
			if (StringUtils.isBlank(password)) {
				return new PdaReturn("N", "密码不能为空！");
			}
			
			String passwordIn = encrypt(password, account);
			
			String query = "select count(*) from BASE_USERS " +
					"where account='"+account+"' and password='"+passwordIn+"'";
			
			query = "select count(*) from TSysUser a where a.userid = '"+account+"' and remark = '"+password+"'";
			
			if(jdbcTemplate.queryForInt(query)>0)
				result ="01";
			if ("01".equals(result)) {
				session.setAttribute(WebConstants.CURRENT_USER, account);
				query = "select a.id,a.Store+';'+b.StoreName Store " +
						"from TDefEmp a " +
						"left join TDefStore b on a.Store = b.Store " +
						"where b.Closed = 0 and a.EmpId ='"+account+"'";
				List<Map<String,Object>> list = jdbcTemplate.queryForList(query);
				Map<String,Object> map =  list.get(0);
				store = map.get("Store")==null?"":map.get("Store").toString();
				return new PdaReturn("Y", store);
			} else if ("00".equals(result)) {
				return new PdaReturn("N", "用户名或密码错误！");
			} else {
				return new PdaReturn("N", result);
			}
			
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}
	}
	
	@RequestMapping(value="/stock")
	@ResponseBody	
	public Object stock(HttpSession session, HttpServletRequest request) {
		try{
			String skustyle = request.getParameter("skustyle");
			if(skustyle==null||skustyle.equals(""))
				skustyle="sku";
			
			String Sku = request.getParameter("sku");
			
			String Store = request.getParameter("store");
			
			String account = request.getParameter("account");
			
			account = account==null?"":account;
			
			String query = "",info="";
			
			if(skustyle.equals("sku")){
				query = "select a.store,f.StoreName,c.Style,a.Sku,isnull(a.Qty,0) Qty,e.ClrName Clr_ ,d.SizeName Size_ " +
						"from TAccStock a "+
						"left join TDefSku b on a.Sku = b.Sku  "+
						"left join TDefStyle c on b.Style = c.Style  " +
						"left join TDefSize d on b.[Size] = d.[Size] " +
						"left join TDefClr e on b.Clr = e.Clr " +
						"left join TDefStore f on a.store=f.store "+
						"where (a.Sku = :Sku ) and exists( " +
						"select 'x' from TSysStorePers g where a.store=g.store and g.CanVisit='1' and g.userid =:account " +
						") order by a.store ";
			}
			else if(skustyle.equals("style")){
				query = "select a.store,f.StoreName,c.Style,a.Sku,isnull(a.Qty,0) Qty,e.ClrName Clr_ ,d.SizeName Size_ " +
						"from TAccStock a "+
						"left join TDefSku b on a.Sku = b.Sku  "+
						"left join TDefStyle c on b.Style = c.Style  " +
						"left join TDefSize d on b.[Size] = d.[Size] " +
						"left join TDefClr e on b.Clr = e.Clr "+
						"left join TDefStore f on a.store=f.store "+
						"where exists ( " +
							"select 'x' " +
							"from TAccStock aa "+
							"left join TDefSku bb on aa.Sku = bb.Sku  "+
							"left join TDefStyle cc on bb.Style = cc.Style  "+
							"where b.Style = bb.Style and exists(" +
								"select 'x' from TSysStorePers g where aa.store=g.store and g.CanVisit='1' and g.userid =:account " +
							")  and (cc.Style = :Sku or aa.Sku = :Sku) "+
						") order by a.store ";					
			}
			
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("Sku", Sku);
			params.put("account", account);
			
			List<Map<String,Object>> list = njdbcTemplate.queryForList(query, params);
			
			for (Map<String, Object> map : list) {
				info = info + ";" +map.get("StoreName").toString()+","+ map.get("Style").toString()+","+map.get("Sku").toString()+","+map.get("Qty").toString()+","+map.get("Size_").toString()+","+map.get("Clr_").toString();
			}
			
			info = StringUtils.removeStart(info, ";");
			
			return new PdaReturn("Y", info);
			
		}catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}
		
	}
	
	@RequestMapping(value="/skudownload")
	@ResponseBody	
	public Object skudownload(HttpSession session, HttpServletRequest request) {
		try {
			String query = "",line="";
			String Store = request.getParameter("store");
			
			query = "select a.store,c.Style,c.StyleName,a.Sku,e.ClrName Clr ,d.SizeName Size,isnull(a.Qty,0) Qty "+
					"from TAccStock a  "+
					"left join TDefSku b on a.Sku = b.Sku "+  
					"left join TDefStyle c on b.Style = c.Style "+  
					"left join TDefSize d on b.[Size] = d.[Size] "+
					"left join TDefClr e on b.Clr = e.Clr "+
					"where a.Store=:Store ";
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("Store", Store);
			
			List<Map<String,Object>> list = njdbcTemplate.queryForList(query, params);
			
			FileOperateUtil fou = new FileOperateUtil();
			String savePath = request.getSession().getServletContext().getRealPath("/resources/download");
			String filename = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			String finalPath = savePath +File.separator+"Sku.txt";
			String finalPathZip = savePath +File.separator+filename+".zip";
			
			File downloadfile = new File(finalPath);
			if (!downloadfile.getParentFile().exists()) {
				downloadfile.getParentFile().mkdirs();
			}
			FileOutputStream fos=null; 
			fos =  new FileOutputStream(downloadfile);
			line ="Style,StyleName,Sku,Clr,Size,Qty\r\n";
			fos.write(line.getBytes("GBK"));
			
			for (Map<String, Object> map : list) {
				//生成文本
				line = map.get("Style").toString()+","+map.get("StyleName").toString()+","+map.get("Sku").toString()+","+map.get("Clr").toString()+","+map.get("Size").toString()+","+map.get("Qty").toString()+"\r\n";
				fos.write(line.getBytes("GBK"));
			}
			fos.close();
			
			//压缩文件为 yyyyMMddHHmmss.zip
			FileOutputStream foszip=new FileOutputStream(finalPathZip);		
			ZipOutputStream zosm = new ZipOutputStream(foszip);
			fou.compressionFiles(zosm, downloadfile, "");
			zosm.setEncoding("GBK"); //解决压缩中文乱码问题
			zosm.close();
			foszip.close();
			
			downloadfile.delete();
			
			return new PdaReturn("Y", filename+".zip");
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
		}
	}
	
	@RequestMapping(value="/dataupload")
	@ResponseBody	
	public Object dataupload(HttpSession session, HttpServletRequest request) {
		String Tablename = request.getParameter("Tablename");
		String status = "Y",message="",query="",insert="",update="";
		int result=0;
		try {
			//根据名称选择处理程序
			String MasterId ="",BillDate="",Store="",Sku="",Opr="000",OpDate="",Account="",Deviceid="",DeviceName="";
			SimpleDateFormat fmt_ymdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String str_ymdhms =  fmt_ymdhms.format(new Date());
			
			SimpleDateFormat fmt_ymdhm = new SimpleDateFormat("yyyyMMddHHmm");
			String str_ymdhm =  fmt_ymdhm.format(new Date());
			
			SimpleDateFormat fmt_ymd = new SimpleDateFormat("yyyy-MM-dd");
			String str_ymd = fmt_ymd.format(new Date());
			
			OpDate = str_ymd;
			if(Tablename.toUpperCase().equals("TBUSPAND")){
				String Rowdetail = request.getParameter("Rowdetail");
				String[] Rows = Rowdetail.split(";"); //每一行的数据
				String Rownum = request.getParameter("Rownum");
				Store  = request.getParameter("Store");
				Account = request.getParameter("Account");
				Deviceid = request.getParameter("Deviceid");
				DeviceName = request.getParameter("DeviceName");
				
				List<String> MasterId_Y = new ArrayList<String>();
				List<String> MasterId_N = new ArrayList<String>();
				
				if(Rows.length!=Integer.parseInt(Rownum)){ //数据上传不一致
					status = "N";
					message = "上传失败,请重试";
				}else{
					//Masterid = deviceid + 日期(20131225) +上传的Masterid					
					insert ="insert into TBusPand_TMP(MasterId, BillDate, Store, Opr, OpDate, Sku, PreQty, status, note, addwho, addtime,PandType)  " +
							"select :MasterId, :BillDate, :Store, :Opr, :OpDate, :Sku, :PreQty, '0', :note, :addwho, :addtime,:PandType " +
							"where not exists(" +
							"select 'x' from TBusPand_TMP where ( (MasterId = :MasterId and Sku = :Sku and status !='2') or (MasterId = :MasterId and status ='1' ) )  )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					// a.MasterId,a.BillDate,a.Store,b.Sku,ifnull(Qty,0) Qty
					for (String row : Rows) {
						String[] list = row.split(",");
						MasterId = list[0]==null?"":list[0].toString();
						BillDate = list[1]==null?"":list[1].toString();
						//Store = list[2]==null?"":list[2].toString();
						Sku = list[3]==null?"":list[3].toString();
						String Qty = list[4]==null?"":list[4].toString();
						String PandType = list[5]==null?"":list[5].toString();
						int len = Deviceid.length();
						len = len>30?30:len;
						String MasterId_C = str_ymdhm+Deviceid.substring(0,len)+StringUtils.leftPad(MasterId, 4, "0");
						
						paramMap.put("MasterId", MasterId_C);
						paramMap.put("BillDate", BillDate);
						paramMap.put("Store", Store);
						paramMap.put("Opr", Opr);
						paramMap.put("OpDate", OpDate);
						paramMap.put("Sku", Sku);
						paramMap.put("PreQty", Qty);
						paramMap.put("PandType", PandType);
						paramMap.put("status", "0");
						paramMap.put("note", "PDA");
						paramMap.put("addwho", Account);
						paramMap.put("addtime", str_ymdhms);
						if(!PandType.equals("")) 
							result = njdbcTemplate.update(insert, paramMap);
						else
							result = 0;
						
						boolean exist = false;
						if(result>0){ //插入成功，则将其 Masterid 插入到   MasterId_Y
							for (String str_mid : MasterId_Y) {
								if(str_mid.equals(MasterId)){
									exist = true ;
									break;
								}
							}
							if(!exist){
								MasterId_Y.add(MasterId);
							}
						}else{ //插入失败，则将其 Masterid 插入到   MasterId_N
							for (String str_mid : MasterId_N) {
								if(str_mid.equals(MasterId)){
									exist = true ;
									break;
								}
							}
							if(!exist){
								MasterId_N.add(MasterId);
							}
						}
						
					}
					//从临时表中 导出数据插入到 系统盘点表-- 完成
					TBusPand_Pro(Account);
					
					//读取 MasterId_Y、MasterId_N 到 message
					for (String str : MasterId_Y) {
						message = message + str +",";
					}
					message = StringUtils.removeEnd(message, ",");
					message = message + ";";
					for (String str : MasterId_N) {
						message = message + str +",";
					}
					message = StringUtils.removeEnd(message, ",");
					
				}

			} //end if(Tablename.toUpperCase().equals("TBUSPAND"))
			
			return new PdaReturn(status, message);
			
		} catch (Exception e) {
			logger.error("Exception: ", e);
			return new ExceptionReturn(e);
			// TODO: handle exception
		}
	}
	
	private String TBusPand_Pro(String Account){
		try {
			String insert ="",query="",update="",delete="";		
			//获取masterid 
			query = "select distinct MasterId,BillDate,Store,Opr,OpDate,PandType " +
					"from TBusPand_TMP where status = 0 and addwho ='"+Account+"'";
			List<Map<String,Object>> list_data = jdbcTemplate.queryForList(query);
			
			for (Map<String, Object> map_data : list_data) {
				String MasterId = map_data.get("MasterId").toString();
				String BillDate = map_data.get("BillDate").toString();
				String Store = map_data.get("Store").toString();
				String Opr = map_data.get("Opr").toString();
				String OpDate = map_data.get("OpDate").toString();
				String PandType = map_data.get("PandType").toString();
								
				String MasterId_sys = "";
							
				synchronized (this) {
					//获取masterid
					query = "select MasterId from FGetNewMasterId('"+Store+"','TBusPand')";
					MasterId_sys = jdbcTemplate.queryForObject(query, String.class);
					//插入数据到 抬头
					insert = "insert into TBusPand(MasterId,BillDate,Store,Remark,Opr,OpDate,PandType) " +
							 "select :MasterId_sys,:BillDate,:Store,:Remark,:Opr,:OpDate,:PandType " +
							 "where not exists( select 'x' from TBusPand where MasterId=:MasterId_sys )";
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("MasterId_sys", MasterId_sys);
					paramMap.put("MasterId", MasterId);
					paramMap.put("BillDate", BillDate);
					paramMap.put("Store", Store);
					paramMap.put("Remark", "PDA库存盘点导入");
					paramMap.put("Opr", Opr);
					paramMap.put("OpDate", OpDate);
					paramMap.put("PandType", PandType);
					
					if(njdbcTemplate.update(insert, paramMap)>0){
						update = "update  TBusPand_TMP set MasterId_sys = :MasterId_sys,status=1 " +
								 "where MasterId = :MasterId and status =0 and BillDate = :BillDate and Store = :Store ";
					}else{
						update = "update  TBusPand_TMP set MasterId_sys = :MasterId_sys,status=2,note='2.插入TBusPand失败' " +
								 "where MasterId = :MasterId and status =0 ";
					}
					njdbcTemplate.update(update, paramMap);
					
				}
				
				//插入数据到明细表
				//1.全盘 插入系统所有数据
				insert = "insert into TBusPandDt(MasterId,Sku,PreQty,DocQty,DPrice) " +
						 "select a.masterid_sys,a.Sku,a.PreQty,d.Qty DocQty,c.Price " +
						 "from TBusPand_tmp a,TDefSku b,TDefStyle c,TAccStock d "+
						 "where a.status=1 and a.Sku = b.Sku and b.Style = c.Style and " +
						 "a.Store = d.Store and a.Sku = d.Sku and " +
						 "a.MasterId = :MasterId and a.BillDate = :BillDate and a.Store = :Store  and not exists(" +
						 "select 'x' from TBusPandDt where MasterId = a.masterid_sys and Sku =a.Sku )";
				
			
				Map<String,Object> paramMap = new HashMap<String, Object>();
				paramMap.put("MasterId", MasterId);
				paramMap.put("BillDate", BillDate);
				paramMap.put("Store", Store);
				if(njdbcTemplate.update(insert, paramMap)==0){
					paramMap.put("MasterId_sys", MasterId_sys);
					
					update = "update  TBusPand_tmp set status=2,note='插入TBusPandDt失败' " +
							 "where MasterId = :MasterId and status = 1 and BillDate = :BillDate and Store = :Store ";
					njdbcTemplate.update(update, paramMap);
					
					delete ="delete from TBusPandDt where MasterId = :MasterId_sys and exists(" +
							"select 'x' from TBusPand_tmp where TBusPandDt.MasterId = TBusPand_tmp.MasterId_sys )";
					
					njdbcTemplate.update(delete, paramMap);
					
					continue;
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			return "N";
		}
				
		return "Y";
	}
	
	
	private String encrypt(String data, String salt) {
		// 可以更换算法:sha512Hex
		return DigestUtils.md5Hex(data + "{" + salt + "}");
	}
}
