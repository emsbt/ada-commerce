package br.com.adacommerce.controller;

import br.com.adacommerce.model.Pedido;
import br.com.adacommerce.model.PedidoStatus;
import br.com.adacommerce.service.PedidoService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashBoardController {

    @FXML private Label lblRascunhoCount;
    @FXML private Label lblAbertoCount;
    @FXML private Label lblConfirmadoCount;
    @FXML private Label lblAguardandoPagamentoCount;
    @FXML private Label lblPagoCount;
    @FXML private Label lblCanceladoCount;
    @FXML private Label lblFinalizadoCount;
    @FXML private Label lblTotal;

    private final PedidoService pedidoService = new PedidoService();

    @FXML
    public void initialize() {
        atualizar();
    }

    @FXML
    private void onRefresh() {
        atualizar();
    }

    private void atualizar() {
        try {
            List<Pedido> pedidos = pedidoService.listar();

            // inicializa mapa com 0 para todos os statuses
            Map<PedidoStatus, Long> contagens = pedidos.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getStatus() == null ? PedidoStatus.RASCUNHO : p.getStatus(),
                            () -> new EnumMap<>(PedidoStatus.class),
                            Collectors.counting()
                    ));

            // garante zeros quando não há entradas
            long rascunho = contagens.getOrDefault(PedidoStatus.RASCUNHO, 0L);
            long aberto = contagens.getOrDefault(PedidoStatus.ABERTO, 0L);
            long confirmado = contagens.getOrDefault(PedidoStatus.CONFIRMADO, 0L);
            long aguardando = contagens.getOrDefault(PedidoStatus.AGUARDANDO_PAGAMENTO, 0L);
            long pago = contagens.getOrDefault(PedidoStatus.PAGO, 0L);
            long cancelado = contagens.getOrDefault(PedidoStatus.CANCELADO, 0L);
            long finalizado = contagens.getOrDefault(PedidoStatus.FINALIZADO, 0L);
            long total = pedidos.size();

            lblRascunhoCount.setText(String.valueOf(rascunho));
            lblAbertoCount.setText(String.valueOf(aberto));
            lblConfirmadoCount.setText(String.valueOf(confirmado));
            lblAguardandoPagamentoCount.setText(String.valueOf(aguardando));
            lblPagoCount.setText(String.valueOf(pago));
            lblCanceladoCount.setText(String.valueOf(cancelado));
            lblFinalizadoCount.setText(String.valueOf(finalizado));
            lblTotal.setText(String.valueOf(total));

        } catch (SQLException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Falha ao carregar pedidos: " + e.getMessage());
            a.setHeaderText("Erro");
            a.showAndWait();
        }
    }
}