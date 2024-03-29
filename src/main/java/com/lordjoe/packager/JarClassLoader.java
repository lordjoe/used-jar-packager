package com.lordjoe.packager;

import java.io.*;
import java.net.*;
import java.util.jar.*;

/**
 * com.lordjoe.packager.JarClassLoader
 * User: Steve
 * Date: 11/22/13
 */
class JarClassLoader extends AbstractLoggingClassFinder {


    public JarClassLoader(LoggingClassLoader parent, File jf) {
        super(parent, jf);
    }

    @Override
    public boolean isJarLoader() {
        return true;
    }


    @Override
    protected InputStream getInputStream(final String className) {
        try {
            JarFile jarFile = new JarFile(getSource());
            JarEntry entry = jarFile.getJarEntry(className.replace(".", "/") + ".class");
            if (entry == null)
                return null;
            return jarFile.getInputStream(entry);
        }
        catch (IOException e) {
            return null;

        }
    }



    /**
     * for files return a file urs
     *
     * @param className
     * @return !null if present
     */
    protected URL findURl(String resourceName) {
        try {
            File source = getSource();

            JarFile jarFile = new JarFile(source);
            JarEntry entry = jarFile.getJarEntry(resourceName);
            if (entry == null)
                return null;

            URI src = source.toURI();

            String uri = "jar:file:/" + source.getAbsolutePath().replace("\\","/") + "!/" + resourceName;
            return new URL(uri);
        }
        catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

}
