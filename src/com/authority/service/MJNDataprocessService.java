package com.authority.service;

import java.util.List;

public interface MJNDataprocessService {
	
	String TBusSale(List<String[]> DataArray,String Account);
	
	String TBusRetail(List<String[]> DataArray,String Account);
	
	String TBusSalRet(List<String[]> DataArray,String Account);
	
	String TBusTran(List<String[]> DataArray,String Account);
	
	String TBusAdj(List<String[]> DataArray,String Account);
	
	String TBusPand(List<String[]> DataArray,String Account);

}
