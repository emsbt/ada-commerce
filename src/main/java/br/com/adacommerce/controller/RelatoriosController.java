package br.com.adacommerce.controller;

import br.com.adacommerce.report.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
        if (t == null) txtDescricao.setText("");
        else txtDescricao.setText(t.getDescricao()); // supondo getDescricao() senão use switch
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
    public void onExportar() {
        // (implemente se quiser — já mandamos antes)
        lblStatus.setText("Exportação não implementada neste snippet.");
    }

    private Double parseDouble(String s){ if(s==null||s.isBlank()) return null; try{return Double.valueOf(s.replace(",","."));}catch(Exception e){return null;}}
    private Integer parseInt(String s){ if(s==null||s.isBlank()) return null; try{return Integer.valueOf(s);}catch(Exception e){return null;}}

    public static class ReportRowWrapper {
        private final ReportRow row;
        public ReportRowWrapper(ReportRow r){ this.row = r;}
        public ObjectProperty<Object> prop(String c){ return new SimpleObjectProperty<>(row.get(c));}
    }
}