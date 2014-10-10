/**
 * AnalyzerBeans
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.beans;

import java.util.Collection;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnalyzerResultReducer;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

/**
 * Reducer of {@link CompletenessAnalyzerResult}s
 */
public class CompletenessAnalyzerResultReducer implements AnalyzerResultReducer<CompletenessAnalyzerResult> {

    @Inject
    @Provided
    RowAnnotationFactory _rowAnnotationFactory;

    @Override
    public CompletenessAnalyzerResult reduce(Collection<? extends CompletenessAnalyzerResult> results) {
        final CompletenessAnalyzerResult firstResult = results.iterator().next();

        final RowAnnotation annotation = _rowAnnotationFactory.createAnnotation();
        final InputColumn<?>[] highlightedColumns = firstResult.getHighlightedColumns();

        int totalRowCount = 0;
        for (CompletenessAnalyzerResult result : results) {
            final InputRow[] rows = result.getRows();
            final int invalidRowCount = result.getInvalidRowCount();
            if (invalidRowCount == rows.length) {
                // if the rows are included for preview/sampling - then
                // re-annotate them in the master result
                _rowAnnotationFactory.annotate(rows, annotation);
            } else {
                // else we just transfer annotation counts
                _rowAnnotationFactory.transferAnnotations(result.getAnnotation(), annotation);
            }

            totalRowCount += result.getTotalRowCount();
        }

        return new CompletenessAnalyzerResult(totalRowCount, annotation, _rowAnnotationFactory, highlightedColumns);
    }

}
