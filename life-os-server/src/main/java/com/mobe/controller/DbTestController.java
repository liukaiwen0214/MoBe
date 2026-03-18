package com.mobe.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DbTestController {

    private final JdbcTemplate jdbcTemplate;

    public DbTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/db/test")
    public Map<String, Object> testDb() {
        Map<String, Object> result = new HashMap<>();
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        result.put("success", true);
        result.put("message", "database connected");
        result.put("version", version);
        return result;
    }
}