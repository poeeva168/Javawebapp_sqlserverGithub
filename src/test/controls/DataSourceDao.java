package test.controls;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DataSourceDao {
	
	protected  JdbcTemplate jdbcTemplate;
	
	protected  NamedParameterJdbcTemplate  njdbcTemplate;

	public NamedParameterJdbcTemplate getNjdbcTemplate() {
		return njdbcTemplate;
	}

	public void setNjdbcTemplate(NamedParameterJdbcTemplate njdbcTemplate) {
		this.njdbcTemplate = njdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		System.out.println("Spring MVC 3.x"+jdbcTemplate.queryForInt("select count(*) from drp_maic"));
	}
	
	
	
}
