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

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PedidosController {

    @FXML private TableView<Pedido> tablePedidos;
    @FXML private TableColumn<Pedido,String> colNumero;
    @FXML private TableColumn<Pedido,String> colCliente;
    @FXML private TableColumn<Pedido,String> colStatus;
    @FXML private TableColumn<Pedido,Number> colTotal;

    @FXML private TextField txtNumero;
    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private TextField txtDesconto;
    @FXML private TableView<PedidoItem> tableItens;
    @FXML private TableColumn<PedidoItem,String> colItemProduto;
    @FXML private TableColumn<PedidoItem,Number> colItemQuantidade;
    @FXML private TableColumn<PedidoItem,Number> colItemPreco;
    @FXML private TableColumn<PedidoItem,Number> colItemSubtotal;
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
    private final ObservableList<PedidoItem> itens = FXCollections.observableArrayList();
    private Pedido emEdicao;
    private enum Modo { VISUAL, NOVO, EDICAO }
    private Modo modo = Modo.VISUAL;

    @FXML
    public void initialize() {
        configurarColunas();
        tablePedidos.setItems(pedidos);
        tableItens.setItems(itens);
        carregarClientes();
        // (Carregar pedidos mais tarde se implementar listagem)
        aplicarModo(Modo.VISUAL);

        tablePedidos.getSelectionModel().selectedItemProperty().addListener((o,a,sel) -> {
            if (sel != null && modo == Modo.VISUAL) {
                emEdicao = sel;
                preencher(sel);
            }
        });
    }

    private void configurarColunas() {
        colNumero.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumero()));
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

    private void preencher(Pedido p) {
        txtNumero.setText(p.getNumero());
        comboCliente.getSelectionModel().select(p.getCliente());
        txtDesconto.setText(String.valueOf(p.getDesconto()));
        itens.setAll(p.getItens());
        atualizarTotais(p);
    }

    private void limpar() {
        txtNumero.clear();
        comboCliente.getSelectionModel().clearSelection();
        txtDesconto.clear();
        itens.clear();
        lblTotais.setText("");
    }

    private void atualizarTotais(Pedido p) {
        p.recalcularTotais();
        lblTotais.setText("Bruto: " + String.format("%.2f", p.getTotalBruto()) +
                " | Desconto: " + String.format("%.2f", p.getDesconto()) +
                " | Líquido: " + String.format("%.2f", p.getTotalLiquido()));
    }

    private void aplicarModo(Modo m) {
        modo = m;
        boolean ed = (m != Modo.VISUAL);
        txtNumero.setDisable(!ed);
        comboCliente.setDisable(!ed);
        txtDesconto.setDisable(!ed);
        btnSalvar.setDisable(!ed);
        btnCancelar.setDisable(!ed);
        btnConfirmar.setDisable(m == Modo.VISUAL ? false : true); // só quando seleciona um pedido salvo?
        btnAddItem.setDisable(!ed);
        btnRemItem.setDisable(!ed);
        lblModo.setText("Modo: " + (m == Modo.VISUAL ? "visualização" : (m==Modo.NOVO?"novo":"edição")));
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
        if (comboCliente.getValue() == null) {
            erro("Validação","Selecione um cliente");
            return;
        }
        try {
            emEdicao.setCliente(comboCliente.getValue());
            double desconto = 0.0;
            if (!txtDesconto.getText().isBlank()) {
                try { desconto = Double.parseDouble(txtDesconto.getText().trim()); } catch (NumberFormatException ignore){}
            }
            emEdicao.setDesconto(desconto);
            emEdicao.getItens().clear();
            emEdicao.getItens().addAll(itens);
            emEdicao.recalcularTotais();
            pedidoService.salvarRascunho(emEdicao);
            if (!pedidos.contains(emEdicao)) pedidos.add(emEdicao);
            aplicarModo(Modo.VISUAL);
        } catch (SQLException e) {
            erro("Erro","Falha ao salvar: " + e.getMessage());
        }
    }

    @FXML private void onConfirmar() {
        Pedido sel = tablePedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { erro("Info","Selecione um pedido"); return; }
        try {
            pedidoService.confirmarPedido(sel);
            atualizarTotais(sel);
            tablePedidos.refresh();
        } catch (SQLException e) {
            erro("Erro","Falha ao confirmar: " + e.getMessage());
        }
    }

    @FXML private void onCancelar() {
        Pedido sel = tablePedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { aplicarModo(Modo.VISUAL); return; }
        try {
            pedidoService.cancelarPedido(sel);
            tablePedidos.refresh();
            atualizarTotais(sel);
        } catch (SQLException e) {
            erro("Erro","Falha ao cancelar: " + e.getMessage());
        }
    }

    @FXML private void onAddItem() {
        // Placeholder: em produção abrir diálogo para escolher produto e quantidade
        List<Produto> prods = produtoService.listarTodos();
        if (prods.isEmpty()) { erro("Info","Cadastre produtos primeiro"); return; }
        Produto prod = prods.get(0);
        PedidoItem it = new PedidoItem();
        it.setProduto(prod);
        it.setQuantidade(1);
        it.setPrecoUnitario(prod.getPreco());
        itens.add(it);
        if (emEdicao != null) { emEdicao.getItens().clear(); emEdicao.getItens().addAll(itens); emEdicao.recalcularTotais(); atualizarTotais(emEdicao); }
    }

    @FXML private void onRemItem() {
        PedidoItem sel = tableItens.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        itens.remove(sel);
        if (emEdicao != null) { emEdicao.getItens().clear(); emEdicao.getItens().addAll(itens); emEdicao.recalcularTotais(); atualizarTotais(emEdicao); }
    }

    private void erro(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(titulo);
        a.showAndWait();
    }
}