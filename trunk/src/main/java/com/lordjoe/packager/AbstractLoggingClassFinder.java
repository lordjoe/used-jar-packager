package com.lordjoe.packager;

import java.io.*;
import java.net.*;
import java.util.*;

/**
* com.lordjoe.packager.AbstractLoggingClassLoader
* User: Steve
* Date: 11/22/13
*/
public abstract class AbstractLoggingClassFinder {
    private final File source;
    private final LoggingClassLoader parent;



    AbstractLoggingClassFinder(final LoggingClassLoader parent, final File pSource) {
        this.parent = parent;
         source = pSource;
    }


    public boolean isJarLoader() {
         return false;
     }


    /**
     * for files return a file urs
     * @param className
     * @return !null if present
     */
    protected abstract URL findURl(String className);

    protected abstract InputStream getInputStream(String className);


    protected File getSource() {
        return source;
    }

    protected byte[] getBytes(String className )   {
        try {
            InputStream is =  getInputStream(  className);
            if(is == null)
                return null;
            final byte[] classByte;
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            int nextValue = is.read();
            while (-1 != nextValue) {
                byteStream.write(nextValue);
                nextValue = is.read();
            }

            classByte = byteStream.toByteArray();
            parent.maybeSaveClass(className,classByte);
            return classByte;
        }
        catch (IOException e) {
            return null; // not found
        }
    }

    @Override
    public String toString() {
        return   source.getName();
    }
}
