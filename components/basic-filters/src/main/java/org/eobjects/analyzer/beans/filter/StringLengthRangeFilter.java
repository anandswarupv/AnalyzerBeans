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
package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.NumberProperty;
import org.eobjects.analyzer.beans.categories.FilterCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@FilterBean("String length range")
@Description("Filter rows based on the length of strings.")
@Categorized(FilterCategory.class)
public class StringLengthRangeFilter implements Filter<RangeFilterCategory> {

	@Configured(order = 1)
	InputColumn<String> column;

	@Configured(order = 2)
	@NumberProperty(negative = false)
	int minimumLength = 0;

	@Configured(order = 3)
	@NumberProperty(negative = false)
	int maximumLength = 10;

	@Override
	public RangeFilterCategory categorize(InputRow inputRow) {
		String value = inputRow.getValue(column);
		if (value == null) {
			return RangeFilterCategory.LOWER;
		}

		int length = value.length();
		if (length < minimumLength) {
			return RangeFilterCategory.LOWER;
		}

		if (length > maximumLength) {
			return RangeFilterCategory.HIGHER;
		}

		return RangeFilterCategory.VALID;
	}

}
