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
package org.eobjects.analyzer.beans.convert;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;

import junit.framework.TestCase;

public class ConvertToDateTransformerTest extends TestCase {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public void testConvertFromNumber() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1971);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DATE, 1);

		assertTrue(cal.getTime().getTime() > 5000000);

		ConvertToDateTransformer transformer = new ConvertToDateTransformer();
		transformer.init();

		assertEquals("1971-01-01", format(transformer.transformValue(cal)));
		assertEquals("1971-01-01", format(transformer.transformValue(cal.getTime())));

		assertEquals("1970-04-03", format(transformer.convertFromNumber(8000000000l)));
		assertEquals("1997-05-19", format(transformer.convertFromNumber(10000)));
		assertEquals("1997-05-19", format(transformer.convertFromNumber(19970519)));
		assertEquals("1997-05-19", format(transformer.convertFromNumber(970519)));
	}

	public void testConvertFromString() throws Exception {
		ConvertToDateTransformer transformer = new ConvertToDateTransformer();
		transformer.init();

		assertEquals("1999-04-20", format(transformer.convertFromString("1999-04-20")));
		assertEquals("1999-04-20", format(transformer.convertFromString("04/20/1999")));
		assertEquals("1999-04-20", format(transformer.convertFromString("1999/04/20")));
		assertEquals("2008-07-11", format(transformer.convertFromString("2008-07-11 00:00:00")));

		Date result = transformer.convertFromString("2008-07-11 14:05:13");
		assertEquals("2008-07-11 14:05:13", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(result));
	}

	private String format(Date date) {
		assertNotNull("date is null", date);
		return dateFormat.format(date);
	}
}
