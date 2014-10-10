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
package org.eobjects.analyzer.descriptors;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.util.ClassLoaderUtils;

import junit.framework.TestCase;

public class ClasspathScanDescriptorProviderTest extends TestCase {

    private MultiThreadedTaskRunner taskRunner = new MultiThreadedTaskRunner(2);

    public void testScanOnlySingleJar() throws Exception {
        // File that only contains various transformers
        File pluginFile1 = new File("src/test/resources/AnalyzerBeans-basic-transformers.jar");

        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
        assertEquals(0, provider.getAnalyzerBeanDescriptors().size());
        Collection<TransformerBeanDescriptor<?>> transformerBeanDescriptors = provider.getTransformerBeanDescriptors();
        assertEquals(0, transformerBeanDescriptors.size());
        File[] files = new File[] { pluginFile1 };
        provider = provider.scanPackage("org.eobjects", true, ClassLoaderUtils.createClassLoader(files), false, files);
        assertEquals(0, provider.getAnalyzerBeanDescriptors().size());
        
        transformerBeanDescriptors = provider.getTransformerBeanDescriptors();
        assertEquals(27, transformerBeanDescriptors.size());

        transformerBeanDescriptors = new TreeSet<>(transformerBeanDescriptors);
        
        assertEquals("org.eobjects.analyzer.beans.coalesce.CoalesceDatesTransformer", transformerBeanDescriptors
                .iterator().next().getComponentClass().getName());
    }

    public void testScanNonExistingPackage() throws Exception {
        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);
        Collection<AnalyzerBeanDescriptor<?>> analyzerDescriptors = provider.scanPackage(
                "org.eobjects.analyzer.nonexistingbeans", true).getAnalyzerBeanDescriptors();
        assertEquals("[]", Arrays.toString(analyzerDescriptors.toArray()));

        assertEquals("[]", provider.getTransformerBeanDescriptors().toString());
        assertEquals("[]", provider.getRendererBeanDescriptors().toString());
    }

    public void testScanPackageRecursive() throws Exception {
        ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
        Collection<AnalyzerBeanDescriptor<?>> analyzerDescriptors = descriptorProvider.scanPackage(
                "org.eobjects.analyzer.beans.mock", true).getAnalyzerBeanDescriptors();
        Object[] array = analyzerDescriptors.toArray();
        assertEquals("[AnnotationBasedAnalyzerBeanDescriptor[org.eobjects.analyzer.beans.mock.AnalyzerMock]]",
                Arrays.toString(array));

        Collection<TransformerBeanDescriptor<?>> transformerBeanDescriptors = descriptorProvider
                .getTransformerBeanDescriptors();
        assertEquals("[AnnotationBasedTransformerBeanDescriptor[org.eobjects.analyzer.beans.mock.TransformerMock]]",
                Arrays.toString(transformerBeanDescriptors.toArray()));

        analyzerDescriptors = new ClasspathScanDescriptorProvider(taskRunner).scanPackage(
                "org.eobjects.analyzer.job.builder", true).getAnalyzerBeanDescriptors();
        assertEquals(0, analyzerDescriptors.size());
    }

    public void testScanRenderers() throws Exception {
        ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
        Collection<RendererBeanDescriptor<?>> rendererBeanDescriptors = descriptorProvider.scanPackage(
                "org.eobjects.analyzer.result.renderer", true).getRendererBeanDescriptors();
        assertEquals(
                "[AnnotationBasedRendererBeanDescriptor[org.eobjects.analyzer.result.renderer.CrosstabTextRenderer], "
                        + "AnnotationBasedRendererBeanDescriptor[org.eobjects.analyzer.result.renderer.MetricBasedResultTextRenderer], "
                        + "AnnotationBasedRendererBeanDescriptor[org.eobjects.analyzer.result.renderer.ToStringTextRenderer]]",
                new TreeSet<RendererBeanDescriptor<?>>(rendererBeanDescriptors).toString());
    }

    public void testScanJarFilesOnClasspath() throws Exception {
        // File that contains 24 transformers including XmlDecoderTransformer
        File pluginFile1 = new File("src/test/resources/AnalyzerBeans-basic-transformers.jar");
        // File that contains 2 writers including InsertIntoTableAnalyzer
        File pluginFile2 = new File("src/test/resources/AnalyzerBeans-writers-0.41.jar");

        File[] files = new File[] { pluginFile1, pluginFile2 };
        ClassLoader classLoader = ClassLoaderUtils.createClassLoader(files);

        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);

        assertEquals(0, provider.getAnalyzerBeanDescriptors().size());
        assertEquals(0, provider.getTransformerBeanDescriptors().size());

        provider = provider.scanPackage("org.eobjects", true, classLoader, true);
        assertEquals(27, provider.getTransformerBeanDescriptors().size());

        boolean foundXmlDecoderTransformer = false;
        for (TransformerBeanDescriptor<?> transformerBeanDescriptor : provider.getTransformerBeanDescriptors()) {
            if (transformerBeanDescriptor.getComponentClass().getName()
                    .equals("org.eobjects.analyzer.beans.codec.XmlDecoderTransformer")) {
                foundXmlDecoderTransformer = true;
                break;
            }
        }
        assertTrue(foundXmlDecoderTransformer);
    }

    public void testIsClassInPackageNonRecursive() throws Exception {
        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);

        assertTrue(provider.isClassInPackage("foo/bar/Baz.class", "foo/bar", false));
        assertTrue(provider.isClassInPackage("foo/bar/Foobar.class", "foo/bar", false));

        assertFalse(provider.isClassInPackage("foo/bar/baz/Baz.class", "foo/bar", false));

        assertFalse(provider.isClassInPackage("foo/baz/Baz.class", "foo/bar", false));
        assertFalse(provider.isClassInPackage("foo/Baz.class", "foo/bar", false));
    }

    public void testIsClassInPackageRecursive() throws Exception {
        ClasspathScanDescriptorProvider provider = new ClasspathScanDescriptorProvider(taskRunner);

        assertTrue(provider.isClassInPackage("foo/bar/Baz.class", "foo/bar", true));
        assertTrue(provider.isClassInPackage("foo/bar/Foobar.class", "foo/bar", true));

        assertTrue(provider.isClassInPackage("foo/bar/baz/Baz.class", "foo/bar", true));

        assertFalse(provider.isClassInPackage("foo/baz/Baz.class", "foo/bar", true));
        assertFalse(provider.isClassInPackage("foo/Baz.class", "foo/bar", true));
    }
}
