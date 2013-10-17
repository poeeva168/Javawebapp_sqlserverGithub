package com.authority.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.authority.service.GMQPdaService;
@Service
public class GMQPdaServiceImpl implements GMQPdaService {
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Resource(name="njdbcTemplate")
	private NamedParameterJdbcTemplate njdbcTemplate;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public String xiangck_submit(List list,String userid) {
		// TODO Auto-generated method stub
		String query="",update ="",insert = "",delete="",BOXNO_R="";
		for (Object object : list) {
			Map<String,Object> list_child = JSON.parseObject(object.toString());
			String BOXNO = list_child.get("BOXNO")==null?"":list_child.get("BOXNO").toString();
			//1.该箱号是否已经被检验
			query = "select count(*) from b_po_boxno where TEST_STATUS=2 and boxno='"+BOXNO+"'";
			if(jdbcTemplate.queryForInt(query)==0){
				BOXNO_R=BOXNO_R+BOXNO+",";		
			}else {
				//2.该箱号是否已经被出库
				query = "select count(*) from M_ISSUE_BOX " +
						"where OUT_STATUS=2 and " +
						"b_po_boxno_id=(select id from b_po_boxno where TEST_STATUS=2 and boxno='"+BOXNO+"')";
				if(jdbcTemplate.queryForInt(query)>0)
					BOXNO_R=BOXNO_R+BOXNO+",";
				else{
					update = "update M_ISSUE_BOX set OUT_STATUS=2 , OUT_USERID='" +userid+"',OUTDATE=sysdate " +
							"where b_po_boxno_id=(select id from b_po_boxno where TEST_STATUS=2 and boxno='"+BOXNO+"')";
					
					if(jdbcTemplate.update(update)>0)
						;
					else
						BOXNO_R=BOXNO_R+BOXNO+",";
					
				}
			}
		}
		if(BOXNO_R.equals(""))
			return "01";
		else
			return StringUtils.removeEnd(BOXNO_R, ",");
	}
}
