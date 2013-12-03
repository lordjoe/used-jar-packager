package com.lordjoe.packager;

import org.junit.*;

import java.net.*;

/**
 * com.lordjoe.packager.LoggingClassLoaderTest
 * User: Steve
 * Date: 12/2/13
 */
public class LoggingClassLoaderTest {


    public static final String TEST_JAVA_RESOURCE = "com/sun/org/apache/xalan/internal/res/XSLTInfo.properties";
    public static final String TEST_JAVA_RESOURCE2 = "com/sun/java/swing/plaf/windows/icons/Computer.gif";

    @Test
    public void testResources() throws Exception {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
         URL res = contextClassLoader.getResource(TEST_JAVA_RESOURCE);
        Assert.assertNotNull(res);
         URL res2 = contextClassLoader.getResource(TEST_JAVA_RESOURCE2);
         Assert.assertNotNull(res2);
     }

    @Test
    public void testJarResource() throws Exception {
//        String TEST_RESOURCE = "/org/systemsbiology/hadoop/excludedLibraries.properties";
//        URL resource = loader.getResource(TEST_RESOURCE);
//        InputStream is = loader.getResourceAsStream(TEST_RESOURCE);
//        String testx = ClassLoaderUtilities.readInFile(is);
//
//
//        String TEST_RESOURCE2 = "META-INF/maven/commons-cli/commons-cli/pom.properties";
//        URL resource2 = loader.getResource(TEST_RESOURCE2);
//        InputStream  is2 = loader.getResourceAsStream(TEST_RESOURCE2);
//        testx = ClassLoaderUtilities.readInFile(is2);


    }
}
