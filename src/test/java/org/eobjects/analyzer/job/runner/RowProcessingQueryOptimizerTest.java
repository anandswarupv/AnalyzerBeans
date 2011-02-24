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
package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.lifecycle.AnalyzerBeanInstance;
import org.eobjects.analyzer.lifecycle.FilterBeanInstance;
import org.eobjects.analyzer.lifecycle.TransformerBeanInstance;
import org.eobjects.analyzer.test.TestHelper;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;

public class RowProcessingQueryOptimizerTest extends TestCase {

	private JdbcDatastore datastore;
	private AnalyzerBeansConfiguration conf;
	private AnalysisJobBuilder ajb;
	private FilterJobBuilder<MaxRowsFilter, ValidationCategory> maxRowsBuilder;
	private RowProcessingAnalyzerJobBuilder<StringAnalyzer> stringAnalyzerBuilder;
	private DataContextProvider dcp;
	private Column lastnameColumn;
	private InputColumn<?> lastNameInputColumn;
	private ArrayList<RowProcessingConsumer> consumers;
	private Query baseQuery;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// set up a common fixture with a simple Max rows filter and a String
		// analyzer on the LASTNAME
		// column
		datastore = TestHelper.createSampleDatabaseDatastore("mydb");
		conf = TestHelper.createAnalyzerBeansConfiguration(datastore);
		ajb = new AnalysisJobBuilder(conf);
		ajb.setDatastore(datastore);
		maxRowsBuilder = ajb.addFilter(MaxRowsFilter.class);
		stringAnalyzerBuilder = ajb.addRowProcessingAnalyzer(StringAnalyzer.class);
		stringAnalyzerBuilder.setRequirement(maxRowsBuilder, ValidationCategory.VALID);
		dcp = conf.getDatastoreCatalog().getDatastore("mydb").getDataContextProvider();
		lastnameColumn = dcp.getSchemaNavigator().convertToColumn("EMPLOYEES.LASTNAME");
		ajb.addSourceColumn(lastnameColumn);
		lastNameInputColumn = ajb.getSourceColumnByName("lastname");
		stringAnalyzerBuilder.addInputColumn(lastNameInputColumn);

		consumers = new ArrayList<RowProcessingConsumer>();
		consumers.add(new FilterConsumer(null, new FilterBeanInstance(maxRowsBuilder.getDescriptor()), maxRowsBuilder
				.toFilterJob(), new InputColumn[0], null));
		consumers.add(new AnalyzerConsumer(null, new AnalyzerBeanInstance(stringAnalyzerBuilder.getDescriptor()),
				stringAnalyzerBuilder.toAnalyzerJob(), new InputColumn[] { lastNameInputColumn }, null));

		baseQuery = dcp.getDataContext().query().from("EMPLOYEES").select("LASTNAME").toQuery();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		dcp.close();
	}

	public void testSimpleOptimization() throws Exception {
		RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(consumers, baseQuery);

		assertTrue(optimizer.isOptimizable());

		Query optimizedQuery = optimizer.getOptimizedQuery();
		Integer maxRows = optimizedQuery.getMaxRows();
		assertNotNull("No max rows specified!", maxRows);
		assertEquals(1000, maxRows.intValue());
	}

	public void testOptimizedChainedTransformer() throws Exception {
		TransformerJobBuilder<EmailStandardizerTransformer> emailStdBuilder = ajb
				.addTransformer(EmailStandardizerTransformer.class);
		Column emailColumn = dcp.getSchemaNavigator().convertToColumn("EMPLOYEES.EMAIL");
		ajb.addSourceColumn(emailColumn);
		InputColumn<?> emailInputColumn = ajb.getSourceColumnByName("email");
		emailStdBuilder.addInputColumn(emailInputColumn);

		// reconfigure the string analyzer to depend on transformed columns
		stringAnalyzerBuilder.clearInputColumns();
		List<MutableInputColumn<?>> outputColumns = emailStdBuilder.getOutputColumns();
		stringAnalyzerBuilder.addInputColumns(outputColumns);

		// remove the string analyzer and add the transformer in between
		consumers.remove(1);
		consumers.add(new TransformerConsumer(null, new TransformerBeanInstance(emailStdBuilder.getDescriptor()),
				emailStdBuilder.toTransformerJob(), new InputColumn[] { emailInputColumn }, null));
		consumers.add(new AnalyzerConsumer(null, new AnalyzerBeanInstance(stringAnalyzerBuilder.getDescriptor()),
				stringAnalyzerBuilder.toAnalyzerJob(), outputColumns.toArray(new InputColumn[0]), null));

		RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(consumers, baseQuery);

		// not optimizable because the transformer doesn't have the requirement
		assertFalse(optimizer.isOptimizable());

		consumers.remove(2);
		consumers.remove(1);
		emailStdBuilder.setRequirement(maxRowsBuilder, ValidationCategory.VALID);
		consumers.add(new TransformerConsumer(null, new TransformerBeanInstance(emailStdBuilder.getDescriptor()),
				emailStdBuilder.toTransformerJob(), new InputColumn[] { emailInputColumn }, null));
		consumers.add(new AnalyzerConsumer(null, new AnalyzerBeanInstance(stringAnalyzerBuilder.getDescriptor()),
				stringAnalyzerBuilder.toAnalyzerJob(), outputColumns.toArray(new InputColumn[0]), null));

		optimizer = new RowProcessingQueryOptimizer(consumers, baseQuery);
		assertTrue(optimizer.isOptimizable());

		// even without the requirement, the string analyzer should still be
		// optimizable, because of it's dependency to the email standardizer
		stringAnalyzerBuilder.setRequirement(null);
		consumers.remove(2);
		consumers.add(new AnalyzerConsumer(null, new AnalyzerBeanInstance(stringAnalyzerBuilder.getDescriptor()),
				stringAnalyzerBuilder.toAnalyzerJob(), outputColumns.toArray(new InputColumn[0]), null));

		optimizer = new RowProcessingQueryOptimizer(consumers, baseQuery);
		assertTrue(optimizer.isOptimizable());
	}

	public void testDontOptimizeWhenComponentsHaveNoRequirements() throws Exception {
		RowProcessingAnalyzerJobBuilder<PatternFinderAnalyzer> patternFinderBuilder = ajb
				.addRowProcessingAnalyzer(PatternFinderAnalyzer.class);
		patternFinderBuilder.addInputColumn(lastNameInputColumn);
		consumers.add(new AnalyzerConsumer(null, new AnalyzerBeanInstance(patternFinderBuilder.getDescriptor()),
				patternFinderBuilder.toAnalyzerJob(), new InputColumn[] { lastNameInputColumn }, null));

		RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(consumers, baseQuery);
		assertFalse(optimizer.isOptimizable());
	}
}
