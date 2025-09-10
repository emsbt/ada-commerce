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
    @FXML private Button btnAbrir;
    @FXML private Button btnAguardarPagamento;
    @FXML private Button btnPagar;
    @FXML private Button btnFinalizar;
    @FXML private Button btnEditar;

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
                    if (sel != null) {
                        emEdicao = sel;
                        preencher(sel);
                        // atualizar botões conforme status do pedido selecionado
                        atualizarAcoesPorStatus(sel);
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
        btnConfirmar.setDisable(!ed);
        btnAbrir.setDisable(!ed);
        btnAguardarPagamento.setDisable(!ed);
        btnPagar.setDisable(!ed);
        // finalização só é habilitada via atualizarAcoesPorStatus quando em modo VISUAL
        btnFinalizar.setDisable(true);
        lblModo.setText("Modo: " + (m==Modo.VISUAL? "visualização": (m==Modo.NOVO? "novo":"edição")));

        if (m == Modo.VISUAL) {
            // ajustar botões conforme o pedido selecionado
            atualizarAcoesPorStatus(emEdicao);
        }
    }

    private void habilitarConfirmar(Pedido p) {
        btnConfirmar.setDisable(!(p != null && p.getStatus()!=null && p.getStatus().isRascunho()));
    }

    /**
     * Atualiza habilitação dos botões de ação com base no status do pedido selecionado.
     * Regras aplicadas:
     *  - Cancelar permitido apenas quando ABERTO, CONFIRMADO ou AGUARDANDO_PAGAMENTO
     *  - Não permitir editar/abrir/cancelar se pedido estiver CANCELADO, PAGO ou FINALIZADO
     *  - Finalizar habilitado apenas quando PAGO
     *  - Pagar habilitado apenas quando AGUARDANDO_PAGAMENTO
     *  - Abrir habilitado apenas quando RASCUNHO ou CONFIRMADO
     *  - Aguardar pagamento habilitado quando ABERTO ou CONFIRMADO
     */
    private void atualizarAcoesPorStatus(Pedido p) {
        // default: desabilitar tudo (quando não há pedido selecionado)
        if (p == null || p.getStatus() == null) {
            btnConfirmar.setDisable(true);
            btnAbrir.setDisable(true);
            btnAguardarPagamento.setDisable(true);
            btnPagar.setDisable(true);
            btnFinalizar.setDisable(true);
            btnCancelar.setDisable(true);

            // permitir criar novo sempre
            return;
        }

        PedidoStatus s = p.getStatus();

        boolean isCancelado = s == PedidoStatus.CANCELADO;
        boolean isPago = s == PedidoStatus.PAGO;
        boolean isFinalizado = s == PedidoStatus.FINALIZADO;

        // Confirmar (só rascunho)
        btnConfirmar.setDisable(!(s.isRascunho()));

        // Abrir: só de RASCUNHO ou CONFIRMADO (e nunca se cancelado/pago/finalizado)
        btnAbrir.setDisable(!( (s.isRascunho() || s.isConfirmado()) && !isCancelado && !isPago && !isFinalizado ));

        // Aguardar pagamento: permitir quando ABERTO ou CONFIRMADO (e não cancelado/pago/finalizado)
        btnAguardarPagamento.setDisable(!( (s.isAberto() || s.isConfirmado()) && !isCancelado && !isPago && !isFinalizado ));

        // Pagar: apenas quando aguardando pagamento
        btnPagar.setDisable(!(s.isAguardandoPagamento()));

        // Finalizar: apenas quando pago
        btnFinalizar.setDisable(!(s.isPago()));
        btnEditar.setDisable(s.isPago()|| s.isCancelado()|| s.isFinalizado());

        // Cancelar: apenas quando ABERTO, CONFIRMADO ou AGUARDANDO_PAGAMENTO
        btnCancelar.setDisable(!(s.isAberto() || s.isConfirmado() || s.isAguardandoPagamento()));

        // Itens/edit: só permitidos se não estiver em CANCELADO / PAGO / FINALIZADO
        boolean podeEditar = !isCancelado && !isPago && !isFinalizado;
        btnAddItem.setDisable(!podeEditar);
        btnRemItem.setDisable(!podeEditar);
        btnSalvar.setDisable(!podeEditar);
    }

    @FXML private void onNovo() {
        emEdicao = new Pedido();
        emEdicao.setNumero("P" + System.currentTimeMillis());
        emEdicao.setDataPedido(new Date());
        // iniciar como ABERTO para permitir adicionar itens imediatamente
        emEdicao.setStatus(PedidoStatus.ABERTO);
        limpar();
        txtNumero.setText(emEdicao.getNumero());
        aplicarModo(Modo.NOVO);
    }

    @FXML private void onSalvar() {
        if (emEdicao == null) return;
        if (comboCliente.getValue() == null) { erro("Validação","Selecione um cliente"); return; }
        // bloquear salvar se pedido estiver em estados imutáveis
        if (emEdicao.getStatus() != null &&
                (emEdicao.getStatus().isCancelado() || emEdicao.getStatus().isPago() || emEdicao.getStatus().isFinalizado())) {
            erro("Ação não permitida", "Não é possível salvar/editar um pedido que está cancelado, pago ou finalizado.");
            return;
        }
        try {
            emEdicao.setCliente(comboCliente.getValue());
            double desconto = parse(txtDesconto.getText());
            emEdicao.setDesconto(desconto);

            // atualiza itens e totais antes de salvar (evita mostrar 0/0)
            emEdicao.getItens().clear();
            emEdicao.getItens().addAll(itens);
            emEdicao.recalcularTotais();

            pedidoService.salvarRascunho(emEdicao);

            if (!pedidos.contains(emEdicao)) pedidos.add(0, emEdicao);
            aplicarModo(Modo.VISUAL);
            tablePedidos.refresh();
            habilitarConfirmar(emEdicao);
            atualizarTotais(emEdicao);
            // atualizar botoes conforme novo status salvo
            atualizarAcoesPorStatus(emEdicao);
        } catch (SQLException e) {
            erro("Erro","Falha ao salvar: " + e.getMessage());
        }
    }

    @FXML private void onConfirmar() {
        Pedido sel = tablePedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { erro("Info","Selecione um pedido"); return; }
        // bloquear confirmar se cancelado/pago/finalizado
        if (sel.getStatus()!=null && (sel.getStatus().isCancelado() || sel.getStatus().isPago() || sel.getStatus().isFinalizado())) {
            erro("Ação não permitida", "Não é possível confirmar um pedido cancelado, pago ou finalizado.");
            return;
        }
        try {
            pedidoService.confirmarPedido(sel);
            tablePedidos.refresh();
            atualizarTotais(sel);
            habilitarConfirmar(sel);
            atualizarAcoesPorStatus(sel);
        } catch (SQLException e) {
            erro("Erro","Falha ao confirmar: " + e.getMessage());
        }
    }

    @FXML private void onAbrir() {
        Pedido sel = tablePedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { erro("Info","Selecione um pedido"); return; }
        // não permitir abrir se estiver cancelado, pago ou finalizado
        if (sel.getStatus()!=null && (sel.getStatus().isCancelado() || sel.getStatus().isPago() || sel.getStatus().isFinalizado())) {
            erro("Ação não permitida", "Não é possível abrir um pedido que está cancelado, pago ou finalizado.");
            return;
        }
        try {
            pedidoService.abrirPedido(sel);
            tablePedidos.refresh();
            atualizarTotais(sel);
            habilitarConfirmar(sel);
            atualizarAcoesPorStatus(sel);
        } catch (SQLException e) {
            erro("Erro","Falha ao abrir: " + e.getMessage());
        }
    }

    @FXML private void onAguardarPagar() {
        Pedido sel = tablePedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { erro("Info","Selecione um pedido"); return; }
        if (sel.getStatus()!=null && (sel.getStatus().isCancelado() || sel.getStatus().isPago() || sel.getStatus().isFinalizado())) {
            erro("Ação não permitida", "Não é possível marcar para aguardar pagamento um pedido cancelado, pago ou finalizado.");
            return;
        }
        try {
            pedidoService.aguardarPagamento(sel);
            tablePedidos.refresh();
            atualizarTotais(sel);
            habilitarConfirmar(sel);
            atualizarAcoesPorStatus(sel);
        } catch (SQLException e) {
            erro("Erro","Falha ao marcar aguardando pagamento: " + e.getMessage());
        }
    }

    @FXML private void onPagar() {
        Pedido sel = tablePedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { erro("Info","Selecione um pedido"); return; }
        if (sel.getStatus()!=null && sel.getStatus().isCancelado()) {
            erro("Ação não permitida", "Não é possível pagar um pedido cancelado.");
            return;
        }
        try {
            pedidoService.pagarPedido(sel);
            tablePedidos.refresh();
            atualizarTotais(sel);
            habilitarConfirmar(sel);
            atualizarAcoesPorStatus(sel);
        } catch (SQLException e) {
            erro("Erro","Falha ao processar pagamento: " + e.getMessage());
        }
    }
    @FXML private void onFinalizar() {
        Pedido sel = tablePedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { erro("Info","Selecione um pedido"); return; }
        // não permitir finalizar se cancelado
        if (sel.getStatus()!=null && sel.getStatus().isCancelado()) {
            erro("Ação não permitida", "Não é possível finalizar um pedido cancelado.");
            return;
        }
        try {
            pedidoService.finalizarPedido(sel);
            tablePedidos.refresh();
            atualizarTotais(sel);
            habilitarConfirmar(sel);
            atualizarAcoesPorStatus(sel);
        } catch (SQLException e) {
            erro("Erro","Falha ao finalizar: " + e.getMessage());
        }
    }

    @FXML private void onCancelar() {
        // se não há pedido em edição, só limpa a tela (descartar edição)
        if (emEdicao == null) {
            limpar();
            aplicarModo(Modo.VISUAL);
            return;
        }

        // Se pedido foi salvo (tem id) perguntar se quer cancelar no banco
        if (emEdicao.getId() != null) {
            // Permitir cancelamento só em ABERTO, CONFIRMADO ou AGUARDANDO_PAGAMENTO
            PedidoStatus s = emEdicao.getStatus();
            if (!(s != null && (s.isAberto() || s.isConfirmado() || s.isAguardandoPagamento()))) {
                erro("Ação não permitida", "Só é possível cancelar pedidos que estejam ABERTO, CONFIRMADO ou AGUARDANDO_PAGAMENTO.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Deseja cancelar (marcar como CANCELADO) este pedido? Esta ação é registrada.",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Cancelar pedido");
            Optional<ButtonType> resp = confirm.showAndWait();
            if (resp.isPresent() && resp.get() == ButtonType.YES) {
                try {
                    pedidoService.cancelarPedido(emEdicao);
                    // só limpar/atualizar UI depois que o cancelamento no DB ocorrer com sucesso
                    carregarPedidos();
                    limpar();
                    aplicarModo(Modo.VISUAL);
                    tablePedidos.refresh();
                    return;
                } catch (SQLException e) {
                    erro("Erro","Falha ao cancelar pedido: " + e.getMessage());
                    // não limpar a tela se falhou no cancelamento
                    return;
                }
            } else {
                // usuário escolheu não cancelar: volta ao modo visual/edição sem apagar dados
                aplicarModo(Modo.VISUAL);
                if (emEdicao != null) {
                    itens.setAll(emEdicao.getItens());
                    atualizarTotais(emEdicao);
                }
                return;
            }
        }

        // se pedido está só em edição (sem id), tratamos como descartar edição: limpa a UI
        limpar();
        aplicarModo(Modo.VISUAL);
    }

    @FXML private void onAddItem() {
        if (emEdicao == null) { erro("Validação","Nenhum pedido em edição"); return; }
        // bloquear se status CANCELADO/PAGO/FINALIZADO
        if (emEdicao.getStatus() != null &&
                (emEdicao.getStatus().isCancelado() || emEdicao.getStatus().isPago() || emEdicao.getStatus().isFinalizado())) {
            erro("Ação não permitida", "Não é possível editar um pedido que está cancelado, pago ou finalizado.");
            return;
        }
        boolean podeEditar = (emEdicao.getStatus() != null && emEdicao.getStatus().isAberto())
                || modo == Modo.NOVO
                || modo == Modo.EDICAO
                || (emEdicao.getStatus() != null && emEdicao.getStatus().isRascunho());
        if (!podeEditar) {
            erro("Ação não permitida", "Só é possível adicionar itens em pedidos com status ABERTO ou em modo de edição/rascunho");
            return;
        }

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
    }

    @FXML private void onRemItem() {
        if (emEdicao == null) { erro("Validação","Nenhum pedido em edição"); return; }
        // bloquear se status CANCELADO/PAGO/FINALIZADO
        if (emEdicao.getStatus() != null &&
                (emEdicao.getStatus().isCancelado() || emEdicao.getStatus().isPago() || emEdicao.getStatus().isFinalizado())) {
            erro("Ação não permitida", "Não é possível editar um pedido que está cancelado, pago ou finalizado.");
            return;
        }
        boolean podeRemover = (emEdicao.getStatus() != null && emEdicao.getStatus().isAberto())
                || modo == Modo.NOVO
                || modo == Modo.EDICAO
                || (emEdicao.getStatus() != null && emEdicao.getStatus().isRascunho());
        if (!podeRemover) {
            erro("Ação não permitida", "Só é possível remover itens em pedidos com status ABERTO ou em modo de edição/rascunho");
            return;
        }
        ItemPedido sel = tableItens.getSelectionModel().getSelectedItem();
        if (sel != null) {
            itens.remove(sel);
            if (emEdicao != null) {
                emEdicao.getItens().clear();
                emEdicao.getItens().addAll(itens);
                atualizarTotais(emEdicao);
            }
        }
    }

    @FXML private void onEditar() {
        if (tablePedidos.getSelectionModel().getSelectedItem() == null) { erro("Info","Selecione um pedido"); return; }
        Pedido sel = tablePedidos.getSelectionModel().getSelectedItem();
        // bloquear edição se cancelado/pago/finalizado
        if (sel.getStatus() != null && (sel.getStatus().isCancelado() || sel.getStatus().isPago() || sel.getStatus().isFinalizado())) {
            erro("Ação não permitida", "Não é possível editar um pedido que está cancelado, pago ou finalizado.");
            return;
        }
        emEdicao = sel;
        itens.setAll(emEdicao.getItens());
        aplicarModo(Modo.EDICAO);
    }

    private void editarQuantidadeItem(ItemPedido item) {
        if (emEdicao == null) { erro("Validação","Nenhum pedido em edição"); return; }
        if (emEdicao.getStatus() != null &&
                (emEdicao.getStatus().isCancelado() || emEdicao.getStatus().isPago() || emEdicao.getStatus().isFinalizado())) {
            erro("Validação","Ação nao permitida"); return;
        }
        boolean podeEditar = (emEdicao.getStatus() != null && emEdicao.getStatus().isAberto())
                || modo == Modo.NOVO
                || modo == Modo.EDICAO
                || (emEdicao.getStatus() != null && emEdicao.getStatus().isRascunho());
        if (!podeEditar) { erro("Validação","Ação nao permitida"); return; }
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