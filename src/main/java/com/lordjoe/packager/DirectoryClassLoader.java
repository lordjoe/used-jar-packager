package com.lordjoe.packager;

import com.sun.jndi.toolkit.url.*;

import java.io.*;
import java.net.*;

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

    /**
      * for files return a file urs
      * @param className
      * @return !null if present
      */
     protected   URL findURl(String className)
     {
           if(className.startsWith("/"))
              className = className.substring(1);
          File f = new File(getSource(),className);
          if (!f.exists())
              return null;
          try {
              URL uri = f.toURI().toURL();
             return uri;
         }
         catch (MalformedURLException e) {
             throw new RuntimeException(e);

         }
     }


}
