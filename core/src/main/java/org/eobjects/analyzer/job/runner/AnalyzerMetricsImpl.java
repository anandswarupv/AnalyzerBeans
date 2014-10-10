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
package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.job.AnalyzerJob;

final class AnalyzerMetricsImpl implements AnalyzerMetrics {

	private final RowProcessingMetrics _rowProcessingMetrics;
	private final AnalyzerJob _analyzerJob;

	public AnalyzerMetricsImpl(RowProcessingMetrics rowProcessingMetrics, AnalyzerJob analyzerJob) {
		_rowProcessingMetrics = rowProcessingMetrics;
		_analyzerJob = analyzerJob;
	}

	@Override
	public AnalyzerJob getAnalyzerJob() {
		return _analyzerJob;
	}

	@Override
	public RowProcessingMetrics getRowProcessingMetrics() {
		return _rowProcessingMetrics;
	}

}
