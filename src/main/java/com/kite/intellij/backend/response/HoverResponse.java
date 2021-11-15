package com.kite.intellij.backend.response;

import com.google.common.collect.Lists;
import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.model.ValueExt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 */
public class HoverResponse {
    private final String partOfSyntax;
    @Nonnull
    private final List<SymbolExt> symbols;
    private final Report report;

    public HoverResponse(String partOfSyntax, SymbolExt[] symbols, Report report) {
        this(partOfSyntax, symbols == null || symbols.length == 0 ? null : Lists.newArrayList(symbols), report);
    }

    public HoverResponse(String partOfSyntax, @Nullable List<SymbolExt> symbols, Report report) {
        this.partOfSyntax = partOfSyntax;
        this.symbols = symbols == null ? Collections.emptyList() : Lists.newArrayList(symbols);
        this.report = report;
    }

    public String getPartOfSyntax() {
        return partOfSyntax;
    }

    @Nonnull
    public List<SymbolExt> getSymbols() {
        return symbols;
    }

    public boolean hasSymbols() {
        return !symbols.isEmpty();
    }

    @Nullable
    public SymbolExt getFirstSymbol() {
        return !symbols.isEmpty() ? symbols.get(0) : null;
    }

    @Nullable
    public ValueExt getFirstValue() {
        SymbolExt firstSymbol = getFirstSymbol();
        return firstSymbol != null ? firstSymbol.getFirstValue() : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(partOfSyntax, symbols, report);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HoverResponse that = (HoverResponse) o;
        return Objects.equals(partOfSyntax, that.partOfSyntax) &&
                Objects.equals(symbols, that.symbols) &&
                Objects.equals(report, that.report);
    }

    @Override
    public String toString() {
        return "HoverResponse{" +
                "partOfSyntax='" + partOfSyntax + '\'' +
                ", symbols=" + symbols +
                ", report=" + report +
                '}';
    }

    public boolean hasReport() {
        return report != null;
    }

    public Report getReport() {
        return report;
    }

    public HoverResponse withReport(Report report) {
        return new HoverResponse(partOfSyntax, symbols, report);
    }
}
