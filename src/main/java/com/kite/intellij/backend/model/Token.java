package com.kite.intellij.backend.model;

import java.util.Objects;

/**
 */
public final class Token {
    private final int beginBytes;
    private final int endBytes;
    private final int beginRunes;
    private final int endRunes;
    private final String partOfSyntax;
    private final Symbol symbol;

    public Token(int beginBytes, int endBytes, int beginRunes, int endRunes, String partOfSyntax, Symbol symbol) {
        this.beginBytes = beginBytes;
        this.endBytes = endBytes;
        this.beginRunes = beginRunes;
        this.endRunes = endRunes;
        this.partOfSyntax = partOfSyntax;
        this.symbol = symbol;
    }

    public int getBeginBytes() {
        return beginBytes;
    }

    public int getEndBytes() {
        return endBytes;
    }

    public int getBeginRunes() {
        return beginRunes;
    }

    public int getEndRunes() {
        return endRunes;
    }

    public String getPartOfSyntax() {
        return partOfSyntax;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginBytes, endBytes, beginRunes, endRunes, partOfSyntax, symbol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Token token = (Token) o;
        return beginBytes == token.beginBytes &&
                endBytes == token.endBytes &&
                beginRunes == token.beginRunes &&
                endRunes == token.endRunes &&
                Objects.equals(partOfSyntax, token.partOfSyntax) &&
                Objects.equals(symbol, token.symbol);
    }
}
