package com.kite.intellij.backend.response;

import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.model.SymbolExt;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;

/**
 * The reponse of a symbol report request.
 *
  */
@Immutable
@ThreadSafe
public class SymbolReportResponse {
    @Nonnull
    private final SymbolExt symbol;
    private final Report report;

    public SymbolReportResponse(@Nonnull SymbolExt symbol, Report report) {
        this.symbol = symbol;
        this.report = report;
    }

    @Nonnull
    public SymbolExt getSymbol() {
        return symbol;
    }

    public Report getReport() {
        return report;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, report);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SymbolReportResponse that = (SymbolReportResponse) o;
        return Objects.equals(symbol, that.symbol) &&
                Objects.equals(report, that.report);
    }

    @Override
    public String toString() {
        return "SymbolReportResponse{" +
                "symbol=" + symbol +
                ", report=" + report +
                '}';
    }

    public SymbolReportResponse withReport(Report report) {
        return new SymbolReportResponse(symbol, report);
    }
}
