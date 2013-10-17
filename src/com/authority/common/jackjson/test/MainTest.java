package com.authority.common.jackjson.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;

import com.authority.common.utils.ClassLoaderUtil;
import com.authority.common.utils.FileDigest;
import com.authority.common.utils.PropertiesHolder;

/**
 * 
 * 
 * @author chenxin
 * @date 2011-5-12 下午02:31:26
 */
public class MainTest {
	public static void main(String[] args) throws Exception {
		test1();
		//DigestUtils.md5Hex(DigestUtils.md5Hex(password){account});
		String str = "";
		System.out.println("123456 MD5:"+DigestUtils.md5Hex("123456"));
		String test = "b8a2ba63d7da46bb02321ab4f93043d056454app_keyjip544720130515094913formatjsonfrom_date2013-04-03 13:46:10length0methodtaobao.logistics.station.order.querypartner_idtop-apitools-zhejiangshizuservice_provider_codexiaoyoujusession966d334ff66b64714dd168ed75b7b4a5aaf93dfc48a9e542f1102637c199df35sign_methodmd5start0station_id7357status3timestamp2013-7-3 13:45:57to_date2013-07-03 13:46:09user_id1703074920v2.0b8a2ba63d7da46bb02321ab4f93043d056454";
		System.out.println("test _MD5:"+DigestUtils.md5Hex(test));
		
		String inputtime = "20130";
		System.out.println("inputtime:"+StringUtils.substring(inputtime, 0, 8));
		
		
		System.out.println(DigestUtils.shaHex(str));
		System.out.println(DigestUtils.sha256Hex(str));
		System.out.println(DigestUtils.sha384Hex(str));
		System.out.println(DigestUtils.sha512Hex(str));
		
		long start=System.currentTimeMillis();
		System.out.println(FileDigest.getFileMD5(new File("D:\\Personal\\Music\\崔子格 - 皇上吉祥.mp3")));
		System.out.println("耗时(毫秒)："+(System.currentTimeMillis()-start));
		Map<String, String> maps=FileDigest.getDirMD5(new File("E:\\BCKF\\apache-tomcat-6.0.29\\logs\\javawebapp"), true);
		for (Entry<String, String> entry : maps.entrySet()) {
			String key = entry.getKey();
			String md5 = entry.getValue();
			System.out.println(md5+" "+key);
		}
	}

	@SuppressWarnings({ "deprecation", "unused" })
	private static void test1() throws JsonGenerationException, JsonMappingException, IOException {
		User user = new User();
		user.setAge(23);
		user.setName("cx");
		user.setPassword("123456");
		ObjectMapper mapper = new ObjectMapper();
		FilterProvider filters = new SimpleFilterProvider().addFilter("myFilter", SimpleBeanPropertyFilter.serializeAllExcept("password"));
		// SimpleBeanPropertyFilter.filterOutAllExcept("password"));
		// and then serialize using that filter provider:
		String json = mapper.filteredWriter(filters).writeValueAsString(user);
		System.out.println(json);
		
		//反序列化
		User user_ = mapper.readValue(json, User.class);
		System.out.println(user_.getName());
		
		InputStreamReader reader = null;
		Properties properties = new Properties();
		InputStream is = ClassLoaderUtil.getResourceAsStream("config/others/config.properties", MainTest.class);
		if (null != is) {
			reader = new InputStreamReader(is, "UTF-8");
			properties.load(reader);
		}
		PropertiesHolder p = new PropertiesHolder();
		p.setProperties(properties);
		System.out.println(p.getProperty("system.url"));
	}
}
