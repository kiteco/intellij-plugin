package com.kite.intellij.backend.response;

import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.model.ValueExt;

import java.util.Objects;

/**
 */
public class ValueReportResponse {
    private final ValueExt value;
    private final Report report;

    public ValueReportResponse(ValueExt value, Report report) {
        this.value = value;

        this.report = report;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, report);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValueReportResponse that = (ValueReportResponse) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(report, that.report);
    }

    @Override
    public String toString() {
        return "ReportResponse{" +
                "value=" + value +
                ", report=" + report +
                '}';
    }

    public ValueExt getValue() {

        return value;
    }

    public Report getReport() {
        return report;
    }

    public ValueReportResponse withReport(Report report) {
        return new ValueReportResponse(value, report);
    }
}
