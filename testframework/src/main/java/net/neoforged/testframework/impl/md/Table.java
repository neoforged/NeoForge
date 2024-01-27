/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.md;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class Table {

    public static final String SEPARATOR = "|";
    public static final String WHITESPACE = " ";
    public static final String DEFAULT_TRIMMING_INDICATOR = "~";
    public static final int DEFAULT_MINIMUM_COLUMN_WIDTH = 3;

    private final List<TableRow<?>> rows;
    private final List<Alignment> alignments;
    private final boolean firstRowIsHeader;
    private final int minimumColumnWidth;
    private final String trimmingIndicator;

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private List<TableRow<?>> rows = new ArrayList<>();
        private List<Alignment> alignments = new ArrayList<>();
        private boolean firstRowIsHeader = true;
        private int minimumColumnWidth = DEFAULT_MINIMUM_COLUMN_WIDTH;
        private String trimmingIndicator = DEFAULT_TRIMMING_INDICATOR;
        private int rowLimit;

        public Builder withRows(List<TableRow<?>> tableRows) {
            rows = tableRows;
            return this;
        }

        public Builder addRow(TableRow<?> tableRow) {
            rows.add(tableRow);
            return this;
        }

        @SafeVarargs
        public final <T> Builder addRow(T... objects) {
            TableRow<?> tableRow = new TableRow<>(Arrays.asList(objects));
            rows.add(tableRow);
            return this;
        }

        public Builder withAlignments(List<Alignment> alignments) {
            this.alignments = alignments;
            return this;
        }

        public Builder withAlignments(Alignment... alignments) {
            return withAlignments(Arrays.asList(alignments));
        }

        public Builder withAlignment(Alignment alignment) {
            return withAlignments(Collections.singletonList(alignment));
        }

        public Builder withRowLimit(int rowLimit) {
            this.rowLimit = rowLimit;
            return this;
        }

        public Builder withTrimmingIndicator(String trimmingIndicator) {
            this.trimmingIndicator = trimmingIndicator;
            return this;
        }

        public Builder withMinimumColumnWidth(int minimumColumnWidth) {
            this.minimumColumnWidth = minimumColumnWidth;
            return this;
        }

        public Builder useFirstRowAsHeader(boolean firstRowIsHeader) {
            this.firstRowIsHeader = firstRowIsHeader;
            return this;
        }

        public Table build() {
            final Table table = new Table(rows, alignments, firstRowIsHeader, minimumColumnWidth, trimmingIndicator);
            if (rowLimit > 0) {
                return table.trim(rowLimit);
            }
            return table;
        }

        @Override
        public String toString() {
            return build().toString();
        }
    }

    public Table(List<TableRow<?>> rows, List<Alignment> alignments, boolean firstRowIsHeader, int minimumColumnWidth, String trimmingIndicator) {
        this.rows = rows;
        this.alignments = alignments;
        this.firstRowIsHeader = firstRowIsHeader;
        this.minimumColumnWidth = minimumColumnWidth;
        this.trimmingIndicator = trimmingIndicator;
    }

    public String serialize() {
        final Map<Integer, Integer> columnWidths = getColumnWidths(rows, minimumColumnWidth);

        final StringBuilder sb = new StringBuilder();

        final String headerSeparator = generateHeaderSeparator(columnWidths, alignments);
        boolean headerSeparatorAdded = !firstRowIsHeader;
        if (!firstRowIsHeader) {
            sb.append(headerSeparator).append(System.lineSeparator());
        }

        final Iterator<TableRow<?>> itr = rows.iterator();
        while (itr.hasNext()) {
            final TableRow<?> row = itr.next();
            for (int columnIndex = 0; columnIndex < columnWidths.size(); columnIndex++) {
                sb.append(SEPARATOR);

                String value = "";
                if (row.getColumns().size() > columnIndex) {
                    value = row.getColumns().get(columnIndex).toString();
                }

                if (value.equals(trimmingIndicator)) {
                    value = Alignment.LEFT.fillAligned(value, trimmingIndicator, columnWidths.get(columnIndex));
                } else {
                    final Alignment alignment = getAlignment(alignments, columnIndex);
                    value = alignment.fillAligned(value, WHITESPACE, columnWidths.get(columnIndex) + 2);
                }

                sb.append(value);

                if (columnIndex == row.getColumns().size() - 1) {
                    sb.append(SEPARATOR);
                }
            }

            if (itr.hasNext() || rows.size() == 1) {
                sb.append(System.lineSeparator());
            }

            if (!headerSeparatorAdded) {
                sb.append(headerSeparator).append(System.lineSeparator());
                headerSeparatorAdded = true;
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return serialize();
    }

    /**
     * Removes {@linkplain TableRow rows} from the center of the table until only the requested amount of
     * rows is left.
     *
     * @param rowsToKeep the amount of rows that should be kept
     * @return the trimmed table
     */
    public Table trim(int rowsToKeep) {
        if (getRows().size() <= rowsToKeep) {
            return this;
        }

        final Table table = copy();
        final int trimmedEntriesCount = table.getRows().size() - (table.getRows().size() - rowsToKeep);
        final int trimmingStartIndex = Math.round(trimmedEntriesCount / 2f) + 1;
        int trimmingStopIndex = table.getRows().size() - trimmingStartIndex;

        final List<TableRow<?>> trimmedRows = new ArrayList<>();
        for (int i = trimmingStartIndex; i <= trimmingStopIndex; i++) {
            trimmedRows.add(table.getRows().get(i));
        }

        table.getRows().removeAll(trimmedRows);

        final TableRow<String> trimmingIndicatorRow = new TableRow<>();
        for (int columnIndex = 0; columnIndex < table.getRows().get(0).getColumns().size(); columnIndex++) {
            trimmingIndicatorRow.getColumns().add(trimmingIndicator);
        }
        table.getRows().add(trimmingStartIndex, trimmingIndicatorRow);

        return table;
    }

    /**
     * {@return a copy of this table}
     */
    public Table copy() {
        return new Table(new ArrayList<>(getRows()), new ArrayList<>(alignments), firstRowIsHeader, minimumColumnWidth, trimmingIndicator);
    }

    public static String generateHeaderSeparator(Map<Integer, Integer> columnWidths, List<Alignment> alignments) {
        final StringBuilder sb = new StringBuilder();
        for (int columnIndex = 0; columnIndex < columnWidths.entrySet().size(); columnIndex++) {
            sb.append(SEPARATOR);
            sb.append(getAlignment(alignments, columnIndex).align("-".repeat(columnWidths.get(columnIndex))));
            if (columnIndex == columnWidths.entrySet().size() - 1) {
                sb.append(SEPARATOR);
            }
        }
        return sb.toString();
    }

    public static Map<Integer, Integer> getColumnWidths(List<TableRow<?>> rows, int minimumColumnWidth) {
        final Map<Integer, Integer> columnWidths = new HashMap<>();
        if (rows.isEmpty()) return columnWidths;
        for (int columnIndex = 0; columnIndex < rows.get(0).getColumns().size(); columnIndex++) {
            columnWidths.put(columnIndex, getMaximumItemLength(rows, columnIndex, minimumColumnWidth));
        }
        return columnWidths;
    }

    public static int getMaximumItemLength(List<TableRow<?>> rows, int columnIndex, int minimumColumnWidth) {
        int maximum = minimumColumnWidth;
        for (final TableRow<?> row : rows) {
            if (row.getColumns().size() < columnIndex + 1) continue;
            final Object value = row.getColumns().get(columnIndex);
            maximum = Math.max(value.toString().length(), maximum);
        }
        return maximum;
    }

    public static Alignment getAlignment(List<Alignment> alignments, int columnIndex) {
        if (alignments.isEmpty()) return Alignment.LEFT;
        if (columnIndex >= alignments.size()) {
            columnIndex = alignments.size() - 1;
        }
        return alignments.get(columnIndex);
    }

    public List<TableRow<?>> getRows() {
        return rows;
    }

    public static String surroundWith(String value, String surrounding) {
        return surrounding + value + surrounding;
    }

}
