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
package org.eobjects.analyzer.metadata;

import java.util.Map;

/**
 * {@link MetadataAnnotationAdaptor} for {@link LocalizedName}.
 */
public class LocalizedNameAnnotationAdaptor implements MetadataAnnotationAdaptor<LocalizedName> {

    @Override
    public String getAnnotationName() {
        return "LocalizedName";
    }

    @Override
    public LocalizedName convertFromAnnotation(MetadataAnnotation annotation) {
        final Map<String, String> parameters = annotation.getParameters();
        return new LocalizedName(parameters);
    }

    @Override
    public MetadataAnnotation convertToAnnotation(LocalizedName object) {
        final Map<String, String> map = object.getDisplayNamesAsMap();
        return new MetadataAnnotationImpl(getAnnotationName(), map);
    }

}