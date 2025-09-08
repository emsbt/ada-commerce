package br.com.adacommerce.report;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvExporter {

    public static void export(List<ReportRow> rows, File file) throws IOException {
        if (rows == null || rows.isEmpty()) {
            try (Writer w = writer(file)) {
                w.write("Sem dados\n");
            }
            return;
        }
        Map<String,Object> first = rows.get(0).asMap();
        String header = first.keySet().stream().collect(Collectors.joining(";"));
        try (Writer w = writer(file)) {
            w.write(header);
            w.write("\n");
            for (ReportRow r : rows) {
                String line = r.asMap().values().stream()
                        .map(v -> v == null ? "" : v.toString().replace(";", ","))
                        .collect(Collectors.joining(";"));
                w.write(line);
                w.write("\n");
            }
        }
    }

    private static Writer writer(File f) throws IOException {
        return new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
    }
}