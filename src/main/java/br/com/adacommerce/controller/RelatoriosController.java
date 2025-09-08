package br.com.adacommerce.controller;

import br.com.adacommerce.service.RelatorioService;
import br.com.adacommerce.util.CsvExporter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RelatoriosController {

    @FXML private ComboBox<RelatorioOption> comboTipo;
    @FXML private DatePicker dtInicio;
    @FXML private DatePicker dtFim;
    @FXML private TextField txtParametro;
    @FXML private TableView<Map<String,Object>> tableResultados;
    @FXML private Label lblResumo;
    @FXML private TextArea txtDescricao;
    @FXML private ProgressIndicator progress;
    @FXML private Button btnExportar;

    private final RelatorioService service = new RelatorioService();
    private List<Map<String,Object>> ultimoResultado = Collections.emptyList();

    public static class RelatorioOption {
        private final RelatorioService.TipoRelatorio tipo;
        public RelatorioOption(RelatorioService.TipoRelatorio tipo) { this.tipo = tipo; }
        public RelatorioService.TipoRelatorio getTipo() { return tipo; }
        @Override public String toString() { return tipo.getId() + " - " + tipo.getTitulo(); }
    }

    @FXML
    public void initialize() {
        comboTipo.setItems(FXCollections.observableArrayList(
                Arrays.stream(RelatorioService.TipoRelatorio.values())
                        .map(RelatorioOption::new)
                        .toList()
        ));
        comboTipo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> {
            if (b != null) {
                txtDescricao.setText(b.getTipo().getDescricao());
                ajustarCamposObrigatorios(b.getTipo());
            }
        });
        // Sugestão: período padrão = mês corrente
        LocalDate hoje = LocalDate.now();
        dtInicio.setValue(hoje.withDayOfMonth(1));
        dtFim.setValue(hoje);

        tableResultados.setPlaceholder(new Label("Nenhum dado. Selecione um tipo e clique em Gerar."));
    }

    private void ajustarCamposObrigatorios(RelatorioService.TipoRelatorio tipo) {
        // Apenas ESTOQUE_BAIXO ignora período (parametro = limite)
        boolean periodo = tipo != RelatorioService.TipoRelatorio.ESTOQUE_BAIXO;
        dtInicio.setDisable(!periodo);
        dtFim.setDisable(!periodo);
        txtParametro.setPromptText(
                tipo == RelatorioService.TipoRelatorio.ESTOQUE_BAIXO ? "Limite (ex: 5)" : "Valor opcional"
        );
    }

    @FXML
    private void onGerar() {
        RelatorioOption opt = comboTipo.getValue();
        if (opt == null) {
            alerta("Validação", "Selecione um tipo de relatório.");
            return;
        }
        var tipo = opt.getTipo();
        LocalDate ini = dtInicio.getValue();
        LocalDate fim = dtFim.getValue();
        if (tipo != RelatorioService.TipoRelatorio.ESTOQUE_BAIXO) {
            if (ini == null || fim == null) {
                alerta("Validação","Informe início e fim.");
                return;
            }
            if (fim.isBefore(ini)) {
                alerta("Validação","Fim não pode ser antes do início.");
                return;
            }
        }
        String parametro = txtParametro.getText().trim();
        progress.setVisible(true);
        btnExportar.setDisable(true);
        lblResumo.setText("Gerando...");
        tableResultados.getColumns().clear();
        tableResultados.getItems().clear();

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return service.gerar(tipo, ini, fim, parametro.isBlank()? null : parametro);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                })
                .whenComplete((resultado, ex) -> Platform.runLater(() -> {
                    progress.setVisible(false);
                    if (ex != null) {
                        alerta("Erro", ex.getMessage());
                        lblResumo.setText("Erro: " + ex.getMessage());
                        return;
                    }
                    ultimoResultado = resultado;
                    montarColunas(resultado);
                    tableResultados.getItems().setAll(resultado);
                    atualizarResumo(tipo, resultado);
                    btnExportar.setDisable(resultado.isEmpty());
                }));
    }

    private void montarColunas(List<Map<String,Object>> dados) {
        tableResultados.getColumns().clear();
        if (dados == null || dados.isEmpty()) return;
        Map<String,Object> primeira = dados.get(0);
        for (String colName : primeira.keySet()) {
            TableColumn<Map<String,Object>, Object> col = new TableColumn<>(colName);
            col.setCellValueFactory(cd -> {
                Object value = cd.getValue().get(colName);
                return new javafx.beans.property.ReadOnlyObjectWrapper<>(value);
            });
            col.setPrefWidth(150);
            tableResultados.getColumns().add(col);
        }
    }

    private void atualizarResumo(RelatorioService.TipoRelatorio tipo, List<Map<String,Object>> dados) {
        if (dados.isEmpty()) {
            lblResumo.setText("Sem resultados.");
            return;
        }
        switch (tipo) {
            case VENDAS_POR_DIA, PRODUTOS_MAIS_VENDIDOS, CLIENTES_TOP, PEDIDOS_POR_STATUS, TICKET_MEDIO_DIA -> {
                double total = somaColuna(dados, "total");
                lblResumo.setText("Linhas: " + dados.size() + " | Total: " + format(total));
            }
            case ESTOQUE_BAIXO -> {
                double somaEstoque = somaColuna(dados, "estoque");
                lblResumo.setText("Produtos: " + dados.size() + " | Soma Estoque: " + format(somaEstoque));
            }
        }
    }

    private double somaColuna(List<Map<String,Object>> dados, String coluna) {
        double acc = 0;
        for (Map<String,Object> row : dados) {
            Object v = row.get(coluna);
            if (v instanceof Number n) acc += n.doubleValue();
        }
        return acc;
    }

    private String format(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    @FXML
    private void onExportar() {
        if (ultimoResultado == null || ultimoResultado.isEmpty()) {
            alerta("Info","Nada para exportar.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV","*.csv"));
        fc.setInitialFileName("relatorio.csv");
        File file = fc.showSaveDialog(tableResultados.getScene().getWindow());
        if (file == null) return;
        try {
            CsvExporter.exportar(ultimoResultado, file);
            alerta("Sucesso","Arquivo salvo: " + file.getAbsolutePath());
        } catch (Exception e) {
            alerta("Erro","Falha ao exportar: " + e.getMessage());
        }
    }

    private void alerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(titulo);
        a.showAndWait();
    }
}