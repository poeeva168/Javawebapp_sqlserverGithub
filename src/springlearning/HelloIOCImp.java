package springlearning;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class HelloIOCImp implements HelloIOC {

	private String message;
	
	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void hello() {
		// TODO Auto-generated method stub
		System.out.println("HelloIOCImp");
		System.out.println("message:"+message);
		String sql="select getdate() as SYSDATE";
		Map<String,Object> map = jdbcTemplate.queryForMap(sql);
		System.out.println(map.get("SYSDATE"));
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
