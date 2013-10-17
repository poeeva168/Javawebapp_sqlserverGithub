package test.controls;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SortAction {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void Sort(List<Map<String,Object>> list, final String field, final String sort){  
        Collections.sort(list, new Comparator() {
            public int compare(Object a, Object b) {
            	Map<String,Object> map1 = (Map<String, Object>) a ;
            	Map<String,Object> map2 = (Map<String, Object>) b ;
            	int result = 0 ;
            	if(sort != null && "desc".equals(sort)){//倒序
            		result = map2.get(field).toString().compareTo(map1.get(field).toString());
            	}else { //正序
            		result = map1.get(field).toString().compareTo(map2.get(field).toString());
            	}            	                 
                return result;  
            }  
         });  
    }
}
