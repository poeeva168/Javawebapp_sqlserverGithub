package com.authority.service;

import java.util.List;
import java.util.Map;

public interface EmaxInterfaceService {
	
	String TBusSale(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account);
	
	String TBusRetail(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account);
	
	String TBusSalRet(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account);
	
	String TBusTran(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account);
	
	String TBusAdj(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account);
	
	String TBusPand(List<String[]> DataArray,Map<String,Object> fieldmatchMap,String Account);
}
