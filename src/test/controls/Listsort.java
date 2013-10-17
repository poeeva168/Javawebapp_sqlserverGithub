package test.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Listsort {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//模拟List列表数据，现在在用的获取数据库内容，一般使用spring jdbcTemplate 的queryforlist ，返回里面带Map类型的
		//普通数据内容，有时是不能讲结果映射到某个具体的对象，所以采用该种通用的方法		
		
		// TODO Auto-generated method stub
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for(int i=0;i<5;i++){
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("field1", i);
			map.put("field2", 5-i);
			list.add(map);
		}
		//排序前打印
		for (Map<String, Object> map : list) {
			String field1 = map.get("field1").toString();
			String field2 = map.get("field2").toString();
			int result = field1.compareTo(field2);
			System.out.println("field1:"+field1+ " field2:"+field2 + " result:"+result);
		}
		
		SortAction sort = new SortAction();
		sort.Sort(list, "field1", "desc");  //排序的列表、排序的字段、排序设定
				
		//排序后打印
		for (Map<String, Object> map : list) {
			String field1 = map.get("field1").toString();
			String field2 = map.get("field2").toString();
			int result = field1.compareTo(field2);
			System.out.println("field1:"+field1+ " field2:"+field2 + " result:"+result);
		}		
	}
}
