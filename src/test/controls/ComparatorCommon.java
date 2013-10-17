package test.controls;

import java.util.Comparator;
import java.util.Map;

public class ComparatorCommon implements Comparator{

	public int compare(Map<String,Object> map1, Map<String,Object> map2,String field) {
		int result = map1.get(field).toString().compareTo(map2.get(field).toString());		
		
		// TODO Auto-generated method stub
		return result;
	}

	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
