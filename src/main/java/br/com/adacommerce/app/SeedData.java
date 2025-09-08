package br.com.adacommerce.app;

import br.com.adacommerce.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SeedData {
    public static void main(String[] args) throws Exception {
        Connection c = DatabaseConfig.getConnection();
        try (Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM itens_pedido");
            st.executeUpdate("DELETE FROM pedido");
            st.executeUpdate("DELETE FROM produto");
            st.executeUpdate("DELETE FROM cliente");
            st.executeUpdate("DELETE FROM categoria");
        }
        try (Statement st = c.createStatement()) {
            st.executeUpdate("INSERT INTO categoria(nome) VALUES ('Eletrônicos'),('Livros')");
            st.executeUpdate("INSERT INTO cliente(nome,email) VALUES ('Ana','ana@x.com'),('Bruno','bruno@x.com')");
            st.executeUpdate("INSERT INTO produto(nome,preco,estoque) VALUES ('Mouse',50,100),('Teclado',120,50),('Livro Java',90,20)");
        }

        // criar 5 dias de pedidos confirmados
        LocalDate hoje = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i=0;i<5;i++) {
            LocalDate dia = hoje.minusDays(i);
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO pedido(cliente_id,data_criacao,status_pedido,status_pagamento) VALUES (?,?, 'CONFIRMADO','PAGO')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, (i % 2) + 1);
                ps.setString(2, dia.toString());
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                rs.next();
                int pedidoId = rs.getInt(1);

                // itens
                try (PreparedStatement it = c.prepareStatement(
                        "INSERT INTO itens_pedido(pedido_id,produto_id,produto_nome_snapshot,quantidade,preco_unitario) VALUES (?,?,?,?,?)")) {
                    it.setInt(1, pedidoId);
                    it.setInt(2, 1);
                    it.setString(3, "Mouse");
                    it.setInt(4, 1 + (i % 3));
                    it.setDouble(5, 50);
                    it.addBatch();

                    it.setInt(1, pedidoId);
                    it.setInt(2, 3);
                    it.setString(3, "Livro Java");
                    it.setInt(4, 1);
                    it.setDouble(5, 90);
                    it.addBatch();

                    it.executeBatch();
                }
            }
        }
        System.out.println("Seed concluído.");
    }
}