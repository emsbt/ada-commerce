    public void excluir(int id) throws SQLException {
        // Verifica se há subcategorias dependentes
        try (PreparedStatement check = DatabaseConfig.getConnection()
                .prepareStatement("SELECT COUNT(*) FROM categoria WHERE categoria_pai_id = ?")) {
            check.setInt(1, id);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Existe(m) subcategoria(s). Remova-as antes.");
            }
        }
        try (PreparedStatement stmt = DatabaseConfig.getConnection()
                .prepareStatement("DELETE FROM categoria WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }