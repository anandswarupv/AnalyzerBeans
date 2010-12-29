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
package org.eobjects.analyzer.reference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dk.eobjects.metamodel.data.Row;

final class DataStoreBasedSynonym implements Synonym, Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String _masterTerm;
	private final String[] _synonyms;
	private final boolean _caseSensitive;

	public DataStoreBasedSynonym(Row row, boolean caseSensitive, String masterTerm) {
                _synonyms = populateSynonyms(row);
		_caseSensitive = caseSensitive;
		_masterTerm = masterTerm;
	}

	private String[] populateSynonyms(Row row) {
	    List<String> values = new ArrayList<String>();
	    for(Object cell:row.getValues()){
	        values.add((String)cell);
	    }
            return values.toArray(new String[0]);
        }

        @Override
	public String getMasterTerm() {
		return _masterTerm;
	}

	@Override
	public ReferenceValues<String> getSynonyms() {
		return new SimpleStringReferenceValues(_synonyms, _caseSensitive);
	}

}