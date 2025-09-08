package br.com.adacommerce.report;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportRow {
    private final Map<String,Object> data = new LinkedHashMap<>();

    public void put(String key, Object value) { data.put(key, value); }
    public Object get(String key) { return data.get(key); }
    public Map<String,Object> asMap() { return data; }

    @Override
    public String toString() { return data.toString(); }
}