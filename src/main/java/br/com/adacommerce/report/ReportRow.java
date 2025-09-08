package br.com.adacommerce.report;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportRow {
    private final LinkedHashMap<String,Object> cols = new LinkedHashMap<>();

    public void put(String col, Object value) { cols.put(col, value); }
    public Object get(String col) { return cols.get(col); }
    public Map<String,Object> asMap() { return cols; }
}