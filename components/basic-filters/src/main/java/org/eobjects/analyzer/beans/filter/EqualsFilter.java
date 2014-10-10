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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.QueryOptimizedFilter;
import org.eobjects.analyzer.beans.api.Validate;
import org.eobjects.analyzer.beans.categories.FilterCategory;
import org.eobjects.analyzer.beans.convert.ConvertToBooleanTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;

@FilterBean("Equals")
@Description("A filter that excludes values that are not equal (=) to specific set of valid values")
@Categorized(FilterCategory.class)
@Distributed(true)
public class EqualsFilter implements QueryOptimizedFilter<ValidationCategory> {

    @Inject
    @Configured(order = 1)
    @Alias("Column")
    @Description("The column to compare values of")
    InputColumn<?> inputColumn;

    @Inject
    @Configured(order = 21, required = false)
    @Alias("Values")
    @Description("Value(s) to compare with")
    String[] compareValues = new String[1];

    @Inject
    @Configured(order = 22, required = false)
    @Description("Column holding value to compare with")
    InputColumn<?> compareColumn;

    private Object[] operands;
    private boolean number = false;

    public EqualsFilter() {
    }

    public EqualsFilter(String[] values, InputColumn<?> column) {
        this();
        this.compareValues = values;
        this.inputColumn = column;
        init();
    }

    public EqualsFilter(InputColumn<?> inputColumn, InputColumn<?> valueColumn) {
        this();
        this.inputColumn = inputColumn;
        this.compareColumn = valueColumn;
        init();
    }

    public void setValues(String[] values) {
        this.compareValues = values;
    }

    public void setValueColumn(InputColumn<?> valueColumn) {
        this.compareColumn = valueColumn;
    }

    @Validate
    public void validate() {
        if (compareColumn == null) {
            if (compareValues == null || compareValues.length == 0) {
                throw new IllegalStateException("Either 'Values' or 'Value column' needs to be specified.");
            }
        }
    }

    @Initialize
    public void init() {
        Class<?> dataType = inputColumn.getDataType();
        if (ReflectionUtils.isNumber(dataType)) {
            number = true;
        }

        List<Object> operandList = new ArrayList<Object>();
        if (compareColumn != null) {
            operandList.add(null);
        }
        if (compareValues != null) {
            for (int i = 0; i < compareValues.length; i++) {
                final String value = compareValues[i];
                final Object operand = toOperand(value);
                operandList.add(operand);
            }
        }

        operands = operandList.toArray();
    }

    private Object toOperand(Object value) {
        Class<?> dataType = inputColumn.getDataType();
        if (ReflectionUtils.isBoolean(dataType)) {
            return ConvertToBooleanTransformer.transformValue(value, ConvertToBooleanTransformer.DEFAULT_TRUE_TOKENS,
                    ConvertToBooleanTransformer.DEFAULT_FALSE_TOKENS);
        } else if (ReflectionUtils.isDate(dataType)) {
            return ConvertToDateTransformer.getInternalInstance().transformValue(value);
        } else if (ReflectionUtils.isNumber(dataType)) {
            return ConvertToNumberTransformer.transformValue(value);
        } else if (ReflectionUtils.isString(dataType)) {
            return ConvertToStringTransformer.transformValue(value);
        } else {
            return value;
        }
    }

    @Override
    public ValidationCategory categorize(InputRow inputRow) {
        Object inputValue = inputRow.getValue(inputColumn);

        final Object compareValue;
        if (compareColumn == null) {
            compareValue = null;
        } else {
            compareValue = inputRow.getValue(compareColumn);
        }

        return filter(inputValue, compareValue);
    }

    public ValidationCategory filter(final Object v) {
        return filter(v, null);
    }

    public ValidationCategory filter(final Object v, final Object compareValue) {
        if (compareColumn != null) {
            operands[0] = toOperand(compareValue);
        }

        if (v == null) {
            for (Object obj : operands) {
                if (obj == null) {
                    return ValidationCategory.VALID;
                }
            }
            return ValidationCategory.INVALID;
        } else {
            for (Object operand : operands) {
                if (operand != null) {
                    
                    if (number) {
                        Number n1 = (Number) operand;
                        Number n2 = (Number) v;
                        if (equals(n1, n2)) {
                            return ValidationCategory.VALID;
                        }
                    }
                    if (operand.equals(v)) {
                        return ValidationCategory.VALID;
                    }
                }
            }
        }

        return ValidationCategory.INVALID;
    }

    private boolean equals(Number n1, Number n2) {
        if (n1 instanceof Short || n1 instanceof Integer || n1 instanceof Long || n2 instanceof Short
                || n2 instanceof Integer || n2 instanceof Long) {
            // use long comparision
            return n1.longValue() == n2.longValue();
        }
        return n1.doubleValue() == n2.doubleValue();
    }

    @Override
    public boolean isOptimizable(ValidationCategory category) {
        if (compareColumn != null && compareValues != null && compareValues.length > 0) {
            boolean hasCompareValues = false;
            for (String compareValue : compareValues) {
                if (compareValue != null) {
                    hasCompareValues = true;
                }
            }
            return !hasCompareValues;
        }
        return true;
    }

    @Override
    public Query optimizeQuery(Query q, ValidationCategory category) {
        final Column inputPhysicalColumn = inputColumn.getPhysicalColumn();
        if (category == ValidationCategory.VALID) {
            if (compareColumn == null) {
                final SelectItem selectItem = new SelectItem(inputPhysicalColumn);
                final List<FilterItem> filterItems = new ArrayList<FilterItem>();
                for (Object operand : operands) {
                    filterItems.add(new FilterItem(selectItem, OperatorType.EQUALS_TO, operand));
                }
                q.where(new FilterItem(filterItems.toArray(new FilterItem[filterItems.size()])));
            } else {
                final Column valuePhysicalColumn = compareColumn.getPhysicalColumn();
                q.where(inputPhysicalColumn, OperatorType.EQUALS_TO, valuePhysicalColumn);
            }
        } else {
            if (compareColumn == null) {
                for (Object operand : operands) {
                    q.where(inputColumn.getPhysicalColumn(), OperatorType.DIFFERENT_FROM, operand);
                }
            } else {
                final Column valuePhysicalColumn = compareColumn.getPhysicalColumn();
                q.where(inputPhysicalColumn, OperatorType.DIFFERENT_FROM, valuePhysicalColumn);
            }
        }
        return q;
    }
}
