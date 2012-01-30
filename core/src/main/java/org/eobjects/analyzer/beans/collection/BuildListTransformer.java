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
package org.eobjects.analyzer.beans.collection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformer for building lists based on values in a row.
 * 
 * @author Kasper Sørensen
 * @author Shekhar Gulati
 * @author Saurabh Gupta
 */
@TransformerBean("Build list")
public class BuildListTransformer implements Transformer<List<?>> {

	private static final Logger logger = LoggerFactory.getLogger(BuildMapTransformer.class);

	@Inject
	@Configured
	InputColumn<?>[] values;

	@Inject
	@Configured
	boolean includeNullValues;

	@Override
	public OutputColumns getOutputColumns() {
		StringBuilder sb = new StringBuilder("List: ");
		for (int i = 0; i < values.length; i++) {
			String key = values[i].getName();
			sb.append(key);
			if (sb.length() > 30) {
				sb.append("...");
				break;
			}

			if (i + 1 < values.length) {
				sb.append(",");
			}
		}
		OutputColumns outputColumns = new OutputColumns(new String[] { sb.toString() }, new Class[] { List.class });
		return outputColumns;
	}

	@Override
	public List<?>[] transform(InputRow row) {
		final List<Object> list = new ArrayList<Object>(values.length);
		for (InputColumn<?> column : values) {
			final Object value = row.getValue(column);
			if (!includeNullValues && value == null) {
				logger.debug("Ignoring null value for {} in row: {}", column.getName(), row);
			} else {
				list.add(value);
			}
		}

		final List<?>[] result = new List[] { list };
		return result;
	}

}