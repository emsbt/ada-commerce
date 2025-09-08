package br.com.adacommerce.controller;

import br.com.adacommerce.report.CsvExporter;
import br.com.adacommerce.report.ReportRow;
import br.com.adacommerce.report.ReportService;
import br.com.adacommerce.report.ReportType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelatoriosController {

    @FXML private ComboBox<ReportType> cbTipo;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFim;
    @FXML private TextField tfValorOpcional;
    @FXML private TextField tfLimite;
    @FXML private TableView<ReportRowWrapper> table;
    @FXML private Label lblStatus;
    @FXML private TextArea txtDescricao;
    @FXML private Button btnExportar;

    private final ReportService service = new ReportService();
    private List<ReportRow> lastRows = new ArrayList<>();

    @FXML
    public void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList(ReportType.values()));
        cbTipo.getSelectionModel().selectFirst();
        dpInicio.setValue(LocalDate.now().minusDays(7));
        dpFim.setValue(LocalDate.now());
        atualizarDescricao();
        cbTipo.valueProperty().addListener((o,a,b)-> atualizarDescricao());
    }

    private void atualizarDescricao() {
        var t = cbTipo.getValue();
        txtDescricao.setText(t == null ? "" : t.getDescricao());
    }

    @FXML
    public void onGerar() {
        lblStatus.setText("Gerando...");
        table.getItems().clear();
        table.getColumns().clear();
        lastRows.clear();
        btnExportar.setDisable(true);
        try {
            List<ReportRow> rows = service.generate(
                    cbTipo.getValue(),
                    dpInicio.getValue(),
                    dpFim.getValue(),
                    parseDouble(tfValorOpcional.getText()),
                    parseInt(tfLimite.getText())
            );
            lastRows = rows;
            if (rows.isEmpty()) {
                lblStatus.setText("Sem dados.");
                return;
            }
            Map<String,Object> first = rows.get(0).asMap();
            for (String col : first.keySet()) {
                TableColumn<ReportRowWrapper,Object> tc = new TableColumn<>(col);
                tc.setCellValueFactory(d -> d.getValue().prop(col));
                tc.setPrefWidth(130);
                table.getColumns().add(tc);
            }
            table.setItems(FXCollections.observableArrayList(
                    rows.stream().map(ReportRowWrapper::new).toList()
            ));
            lblStatus.setText("OK ("+rows.size()+" linhas)");
            btnExportar.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    private void onExportar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar relatório");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(table.getScene().getWindow());
        if (file == null) return;

        try {
            CsvExporter.export(lastRows, file);
            Alert ok = new Alert(Alert.AlertType.INFORMATION, "Relatório exportado com sucesso!", ButtonType.OK);
            ok.setHeaderText("Exportação");
            ok.showAndWait();
        } catch (Exception ex) {
            Alert error = new Alert(Alert.AlertType.ERROR, "Erro ao exportar: " + ex.getMessage(), ButtonType.OK);
            error.setHeaderText("Exportação");
            error.showAndWait();
        }
    }


    private Double parseDouble(String s){ if(s==null||s.isBlank()) return null; try{return Double.valueOf(s.replace(",","."));}catch(Exception e){return null;} }
    private Integer parseInt(String s){ if(s==null||s.isBlank()) return null; try{return Integer.valueOf(s);}catch(Exception e){return null;} }

    public static class ReportRowWrapper {
        private final ReportRow row;
        public ReportRowWrapper(ReportRow r){ this.row = r;}
        public ObjectProperty<Object> prop(String c){ return new SimpleObjectProperty<>(row.get(c));}
    }
}