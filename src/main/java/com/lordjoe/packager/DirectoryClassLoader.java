package com.lordjoe.packager;

import java.io.*;

/**
* com.lordjoe.packager.DirectoryClassLoader
* User: Steve
* Date: 11/22/13
*/
public class DirectoryClassLoader extends AbstractLoggingClassFinder {
    public DirectoryClassLoader(LoggingClassLoader parent, File jf) {
        super(parent, jf);
    }

    @Override
    public boolean isJarLoader() {
        return false;
    }

    @Override
    protected InputStream getInputStream(final String className) {
        File f = new File(getSource(), className.replace(".", "/") + ".class");
        if (!f.exists())
            return null;
        try {
            //noinspection UnnecessaryLocalVariable
            InputStream is = new FileInputStream(f);
            return is;
        }
        catch (FileNotFoundException e) {
            return null;

        }
    }

}
