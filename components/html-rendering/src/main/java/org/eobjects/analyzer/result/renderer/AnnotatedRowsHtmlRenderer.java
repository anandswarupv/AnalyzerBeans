/**
 * eobjects.org AnalyzerBeans
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.analyzer.result.renderer;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.html.HtmlFragment;
import org.eobjects.analyzer.result.html.SimpleHtmlFragment;

@RendererBean(HtmlRenderingFormat.class)
public class AnnotatedRowsHtmlRenderer implements Renderer<AnnotatedRowsResult, HtmlFragment> {

    @Override
    public RendererPrecedence getPrecedence(AnnotatedRowsResult renderable) {
        return RendererPrecedence.MEDIUM;
    }

    @Override
    public HtmlFragment render(final AnnotatedRowsResult result) {
        SimpleHtmlFragment htmlFragment = new SimpleHtmlFragment();

        InputColumn<?>[] highlightedColumns = result.getHighlightedColumns();
        int[] highlightedIndexes = new int[highlightedColumns.length];
        for (int i = 0; i < highlightedColumns.length; i++) {
            highlightedIndexes[i] = result.getColumnIndex(highlightedColumns[i]);
        }

        htmlFragment.addBodyElement(new TableBodyElement(result.toTableModel(), "annotatedRowsTable",
                highlightedIndexes));
        return htmlFragment;
    }

}