package test.controls;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.authority.common.utils.PoiHelper;
 
public class ExcelOperate {
 
    public static void main(String[] args) throws Exception {
       String[] col_id = {"MASTERID","BILLDATE"};
       String[] col_name = {"单据编号","单据日期"};
       String filePath = "D:\\data\\ExcelDemo.xls";
       
       List<Map<String,Object>> list = new ArrayList();
       for(int i=0;i<5000;i++){
    	   Map<String,Object> map = new HashMap<String, Object>();
           map.put("masterid", "00"+i);
           map.put("billdate", "2014-3-18");
           list.add(map);
       }
       
       PoiHelper.Excel_Generate(list, col_id, col_name, filePath , false);
       
       //读取Excel
       File file = new File(filePath);
       List<String[]> DataArray = PoiHelper.getData(file, 0, 0);
       for (String[] strings : DataArray) {
    	   System.out.println(strings[0]+","+strings[1]);
       }
       
      
    }
   
}
