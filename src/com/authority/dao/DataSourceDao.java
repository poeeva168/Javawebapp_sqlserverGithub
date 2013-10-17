package com.authority.dao;

import java.util.HashMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;


public class DataSourceDao {
	
	@Autowired
	protected  JdbcTemplate jdbcTemplate;
	
	@Autowired
	protected  NamedParameterJdbcTemplate  njdbcTemplate;
	
	@Resource(name="jt_access_henlo")
	private JdbcTemplate jt_access_henlo;

	@Resource(name="jt_oracle_henlo")
	private JdbcTemplate jt_oracle_henlo;
	
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public NamedParameterJdbcTemplate getNjdbcTemplate() {
		return njdbcTemplate;
	}

	public JdbcTemplate getJt_access_henlo() {
		return jt_access_henlo;
	}

	public void setJt_access_henlo(JdbcTemplate jt_access_henlo) {
		this.jt_access_henlo = jt_access_henlo;
	}

	public JdbcTemplate getJt_oracle_henlo() {
		return jt_oracle_henlo;
	}

	public void setJt_oracle_henlo(JdbcTemplate jt_oracle_henlo) {
		this.jt_oracle_henlo = jt_oracle_henlo;
	}

	
	
}
