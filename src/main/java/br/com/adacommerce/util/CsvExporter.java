package br.com.adacommerce.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exporta lista de Map<String,Object> para CSV simples.
 */
public class CsvExporter {

    public static void exportar(List<Map<String,Object>> dados, File destino) throws IOException {
        if (dados == null || dados.isEmpty()) {
            try (Writer w = new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8)) {
                w.write("SEM DADOS\n");
            }
            return;
        }
        var headers = dados.get(0).keySet().stream().toList();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8)) {
            // cabeçalho
            w.write(headers.stream().map(CsvExporter::esc).collect(Collectors.joining(";")) + "\n");
            // linhas
            for (Map<String,Object> row : dados) {
                String linha = headers.stream()
                        .map(h -> esc(String.valueOf(row.get(h) == null ? "" : row.get(h))))
                        .collect(Collectors.joining(";"));
                w.write(linha + "\n");
            }
        }
    }

    private static String esc(String v) {
        if (v == null) return "";
        String s = v.replace("\"","\"\"");
        if (s.contains(";") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}