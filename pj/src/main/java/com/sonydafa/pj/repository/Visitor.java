package com.sonydafa.pj.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class Visitor {
    @Autowired
    @Qualifier("jdbcTemplatePJ")
    private JdbcTemplate jdbcTemplate;
    public String store(Object[]args){
        String sql="insert into visitor(s_id,visit_date,visit_time,ip) values "+
                " (?,?,?,?)";
        jdbcTemplate.update(sql,args);
        return "ok";
    }
}
