/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.summary.md;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TableRow<T> {
    private final List<T> columns;

    public TableRow() {
        this(new ArrayList<>());
    }

    public TableRow(List<T> columns) {
        this.columns = columns;
    }

    public String serialize() throws IllegalArgumentException {
        final StringBuilder sb = new StringBuilder();

        final Iterator<T> itr = columns.iterator();
        while (itr.hasNext()) {
            final T item = itr.next();
            final String asString = item.toString();
            if (asString.contains(Table.SEPARATOR)) {
                throw new IllegalArgumentException("Column contains separator char \"" + Table.SEPARATOR + "\"");
            }
            sb.append(Table.SEPARATOR);
            sb.append(Table.surroundWith(asString, Table.WHITESPACE));
            if (!itr.hasNext()) {
                sb.append(Table.SEPARATOR);
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return serialize();
    }

    public List<T> getColumns() {
        return columns;
    }
}
