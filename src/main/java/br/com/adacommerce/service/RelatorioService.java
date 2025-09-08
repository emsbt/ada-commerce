package br.com.adacommerce.service;

import br.com.adacommerce.report.ReportService;
import br.com.adacommerce.report.ReportType;
import br.com.adacommerce.report.ReportRow;

import java.time.LocalDate;
import java.util.List;

/**
 * @deprecated Use {@link br.com.adacommerce.report.ReportService}.
 * Classe mantida apenas para compatibilidade temporária.
 */
@Deprecated
public class RelatorioService {

    private final ReportService delegate = new ReportService();

    public List<ReportRow> gerar(ReportType tipo,
                                 LocalDate inicio,
                                 LocalDate fim,
                                 Double valorOpcional,
                                 Integer limite) throws Exception {
        return delegate.generate(tipo, inicio, fim, valorOpcional, limite);
    }
}