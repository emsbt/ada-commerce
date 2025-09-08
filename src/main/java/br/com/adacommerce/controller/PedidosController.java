package br.com.adacommerce.controller;

import br.com.adacommerce.model.*;
import br.com.adacommerce.service.ClienteService;
import br.com.adacommerce.service.PedidoService;
import br.com.adacommerce.service.ProdutoService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;

import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

public class PedidosController {

    @FXML private TableView<Pedido> tablePedidos;
    @FXML private TableColumn<Pedido,String> colNumero;
    @FXML private TableColumn<Pedido,String> colCliente;
    @FXML private TableColumn<Pedido,String> colStatus;
    @FXML private TableColumn<Pedido,Number> colTotal;

    @FXML private TextField txtNumero;
    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private TextField txtDesconto;
    @FXML private ComboBox<Produto> cbProduto;
    @FXML private Spinner<Integer> spQtd;

    @FXML private TableView<ItemPedido> tableItens;
    @FXML private TableColumn<ItemPedido,String> colItemProduto;
    @FXML private TableColumn<ItemPedido,Number> colItemQuantidade;
    @FXML private TableColumn<ItemPedido,Number> colItemPreco;
    @FXML private TableColumn<ItemPedido,Number> colItemSubtotal;

    @FXML private Button btnSalvar;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;
    @FXML private Button btnAddItem;
    @FXML private Button btnRemItem;
    @FXML private Label lblModo;
    @FXML private Label lblTotais;

    private final PedidoService pedidoService = new PedidoService();
    private final ClienteService clienteService = new ClienteService();
    private final ProdutoService produtoService = new ProdutoService();

    private final ObservableList<Pedido> pedidos = FXCollections.observableArrayList();
    private final ObservableList<ItemPedido> itens = FXCollections.observableArrayList();
    private Pedido emEdicao;

    private enum Modo { VISUAL, NOVO, EDICAO }
    private Modo modo = Modo.VISUAL;

    @FXML
    public void initialize() {
        configurarColunas();
        tablePedidos.setItems(pedidos);
        tableItens.setItems(itens);
        carregarClientes();
        carregarProdutos();
        carregarPedidos();
        aplicarModo(Modo.VISUAL);

        tablePedidos.getSelectionModel().selectedItemProperty()
                .addListener((o,a,sel)-> {
                    if (sel != null && modo == Modo.VISUAL) {
                        emEdicao = sel;
                        preencher(sel);
                        habilitarConfirmar(sel);
                    }
                });

        tableItens.setRowFactory(tv -> {
            TableRow<ItemPedido> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (!row.isEmpty() && evt.getButton()== MouseButton.PRIMARY && evt.getClickCount()==2 && modo != Modo.VISUAL) {
                    editarQuantidadeItem(row.getItem());
                }
            });
            return row;
        });

        cbProduto.setCellFactory(listView -> new ListCell<>() {
            @Override protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? null : item.getNome());
            }
        });
        cbProduto.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? null : item.getNome());
            }
        });
    }

    private void configurarColunas() {
        colNumero.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumero() != null ? c.getValue().getNumero() : ""));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCliente()!=null ? c.getValue().getCliente().getNome() : ""));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus()!=null ? c.getValue().getStatus().name() : ""));
        colTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotalLiquido()));

        colItemProduto.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getProduto()!=null ? c.getValue().getProduto().getNome() : ""));
        colItemQuantidade.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantidade()));
        colItemPreco.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrecoUnitario()));
        colItemSubtotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSubtotal()));
    }

    private void carregarClientes() {
        comboCliente.getItems().setAll(clienteService.listarTodos());
    }

    private void carregarProdutos() {
        cbProduto.getItems().setAll(produtoService.listarTodos());
    }

    private void carregarPedidos() {
        try {
            pedidos.setAll(pedidoService.listar());
        } catch (SQLException e) {
            erro("Aviso","Não foi possível listar pedidos: " + e.getMessage());
        }
    }

    private void preencher(Pedido p) {
        txtNumero.setText(p.getNumero());
        comboCliente.getSelectionModel().select(p.getCliente());
        txtDesconto.setText(String.format("%.2f", p.getDesconto()));
        itens.setAll(p.getItens());
        atualizarTotais(p);
    }

    private void limpar() {
        txtNumero.clear();
        comboCliente.getSelectionModel().clearSelection();
        txtDesconto.clear();
        cbProduto.getSelectionModel().clearSelection();
        if (spQtd != null && spQtd.getValueFactory()!=null) spQtd.getValueFactory().setValue(1);
        itens.clear();
        lblTotais.setText("Bruto: 0,00 | Desconto: 0,00 | Líquido: 0,00");
    }

    private void atualizarTotais(Pedido p) {
        p.recalcularTotais();
        lblTotais.setText("Bruto: "+fmt(p.getTotalBruto())
                +" | Desconto: "+fmt(p.getDesconto())
                +" | Líquido: "+fmt(p.getTotalLiquido()));
    }

    private String fmt(double v) { return String.format("%.2f", v); }

    private void aplicarModo(Modo m) {
        modo = m;
        boolean ed = (m != Modo.VISUAL);
        txtNumero.setDisable(!ed);
        comboCliente.setDisable(!ed);
        txtDesconto.setDisable(!ed);
        cbProduto.setDisable(!ed);
        spQtd.setDisable(!ed);
        btnSalvar.setDisable(!ed);
        btnCancelar.setDisable(!ed);
        btnAddItem.setDisable(!ed);
        btnRemItem.setDisable(!ed);
        btnConfirmar.setDisable(true);
        lblModo.setText("Modo: " + (m==Modo.VISUAL? "visualização": (m==Modo.NOVO? "novo":"edição")));
    }

    private void habilitarConfirmar(Pedido p) {
        btnConfirmar.setDisable(!(p != null && p.getStatus()!=null && p.getStatus().isRascunho()));
    }

    @FXML private void onNovo() {
        emEdicao = new Pedido();
        emEdicao.setNumero("P" + System.currentTimeMillis());
        emEdicao.setDataPedido(new Date());
        emEdicao.setStatus(PedidoStatus.RASCUNHO);
        limpar();
        txtNumero.setText(emEdicao.getNumero());
        aplicarModo(Modo.NOVO);
    }

    @FXML private void onSalvar() {
        if (emEdicao == null) return;
        if (comboCliente.getValue() == null) { erro("Validação","Selecione um cliente"); return; }
        try {
            emEdicao.setCliente(comboCliente.getValue());
            double desconto = parse(txtDesconto.getText());
            emEdicao.setDesconto(desconto);
            emEdicao.getItens().clear();
            emEdicao.getItens().addAll(itens);
            emEdicao.recalcularTotais();
            pedidoService.salvarRascunho(emEdicao);
            if (!pedidos.contains(emEdicao)) pedidos.add(0, emEdicao);
            aplicarModo(Modo.VISUAL);
            tablePedidos.refresh();
            habilitarConfirmar(emEdicao);
            atualizarTotais(emEdicao);
        } catch (SQLException e) {
            erro("Erro","Falha ao salvar: " + e.getMessage());
        }
    }

    @FXML private void onConfirmar() {
        Pedido sel = tablePedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { erro("Info","Selecione um pedido"); return; }
        try {
            pedidoService.confirmarPedido(sel);
            tablePedidos.refresh();
            atualizarTotais(sel);
            habilitarConfirmar(sel);
        } catch (SQLException e) {
            erro("Erro","Falha ao confirmar: " + e.getMessage());
        }
    }

    @FXML private void onCancelar() {
        if (modo != Modo.VISUAL) {
            aplicarModo(Modo.VISUAL);
            if (emEdicao != null && emEdicao.getId()==null) pedidos.remove(emEdicao);
            emEdicao = null;
            limpar();
            return;
        }
        Pedido sel = tablePedidos.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            pedidoService.cancelarPedido(sel);
            tablePedidos.refresh();
            atualizarTotais(sel);
            habilitarConfirmar(sel);
        } catch (SQLException e) {
            erro("Erro","Falha ao cancelar: " + e.getMessage());
        }
    }

    @FXML private void onAddItem() {
        Produto prod = cbProduto.getValue();
        if (prod == null) { erro("Validação","Selecione um produto"); return; }
        int qtd = spQtd.getValue() != null ? spQtd.getValue() : 1;
        ItemPedido existente = itens.stream()
                .filter(i -> i.getProduto()!=null && i.getProduto().getId().equals(prod.getId()))
                .findFirst().orElse(null);
        if (existente != null) {
            existente.setQuantidade(existente.getQuantidade()+qtd);
            tableItens.refresh();
        } else {
            ItemPedido it = new ItemPedido();
            it.setProduto(prod);
            it.setQuantidade(qtd);
            it.setPrecoUnitario(prod.getPreco());
            itens.add(it);
        }
        if (emEdicao != null) {
            emEdicao.getItens().clear();
            emEdicao.getItens().addAll(itens);
            atualizarTotais(emEdicao);
        }
        spQtd.getValueFactory().setValue(1);
    }

    @FXML private void onRemItem() {
        ItemPedido sel = tableItens.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        itens.remove(sel);
        if (emEdicao != null) {
            emEdicao.getItens().clear();
            emEdicao.getItens().addAll(itens);
            atualizarTotais(emEdicao);
        }
    }

    private void editarQuantidadeItem(ItemPedido item) {
        TextInputDialog dlg = new TextInputDialog(String.valueOf(item.getQuantidade()));
        dlg.setTitle("Editar quantidade");
        dlg.setHeaderText("Produto: " + (item.getProduto()!=null? item.getProduto().getNome():""));
        dlg.setContentText("Nova quantidade:");
        dlg.showAndWait().ifPresent(val -> {
            try {
                int q = Integer.parseInt(val.trim());
                if (q <= 0) throw new NumberFormatException();
                item.setQuantidade(q);
                tableItens.refresh();
                if (emEdicao != null) {
                    emEdicao.recalcularTotais();
                    atualizarTotais(emEdicao);
                }
            } catch (NumberFormatException ex) {
                erro("Valor inválido","Quantidade deve ser > 0");
            }
        });
    }

    private double parse(String s) {
        if (s == null || s.isBlank()) return 0.0;
        try { return Double.parseDouble(s.replace(",", ".").trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private void erro(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(titulo);
        a.showAndWait();
    }
}