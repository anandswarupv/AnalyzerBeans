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
package org.eobjects.analyzer.connection;

import java.io.Closeable;

import org.apache.metamodel.UpdateableDataContext;

public class UpdateableDatastoreConnectionImpl<E extends UpdateableDataContext> extends DatastoreConnectionImpl<E> implements
		UpdateableDatastoreConnection {

	public UpdateableDatastoreConnectionImpl(E dataContext, Datastore datastore, Closeable... closeables) {
		super(dataContext, datastore, closeables);
	}

	@Override
	public UpdateableDataContext getUpdateableDataContext() {
		E dc = getDataContext();
		return dc;
	}

}
