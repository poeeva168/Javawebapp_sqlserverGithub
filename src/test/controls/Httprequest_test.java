package test.controls;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Httprequest_test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HttpRequester req = new HttpRequester();
		String url="http://eai.tmall.com/api";
		try {			
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("sign", "F4E1054967F086C42D51EE4ACA064EB2");
			map.put("app_key", "jip544720130515094913");
			map.put("format", "json");
			map.put("from_date", "2013-04-02 09:24:21");
			map.put("length", "0");
			map.put("method", "taobao.logistics.station.order.query");
			map.put("partner_id", "top-apitools-zhejiangshizu");
			map.put("service_provider_code", "xiaoyouju");
			map.put("session", "966d334ff66b64714dd168ed75b7b4a5aaf93dfc48a9e542f1102637c199df35");
			map.put("sign_method", "md5");
			map.put("start", "0");
			map.put("station_id", "7473");
			map.put("status", "2");
			map.put("timestamp", "13-7-2 9:23:55");
			map.put("to_date", "2013-07-02 09:24:21");
			map.put("user_id", "1704223042");
			map.put("v", "2.0");			
			HttpRespons rep =  req.sendPost(url, map);	
			
			System.out.println(rep.getContent());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
