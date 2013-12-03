package com.lordjoe.packager;

import com.sun.istack.internal.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.jar.*;

/**
 * com.lordjoe.packager.ClassLoaderUtilities
 * User: Steve
 * Date: 11/22/13
 */
public class ClassLoaderUtilities {


    public static Properties readProperties(String arg) {
        try {
            File f = new File(arg);
            Properties ret = new Properties();
            ret.load(new FileInputStream(f));
            return ret;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    /**
     * { method
     *
     * @param TheFile the file stream
     * @param len     the file length or a good guess
     * @return StringBuilder holding file bytes
     *         }
     * @name readInFile
     * @function reads all the data in a file into a StringBuilder
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String readInFile(InputStream TheFile) {
        BufferedReader TheStream = null;
        StringBuilder s = new StringBuilder();
        char[] buffer = new char[4096];
        int NRead;
        try {
            InputStreamReader streamReader = new InputStreamReader(TheFile);
            TheStream = new BufferedReader(streamReader);
            NRead = TheStream.read(buffer, 0, 4096);
            while (NRead != -1) {
                s.append(buffer, 0, NRead);
                NRead = TheStream.read(buffer, 0, 4096);
                // ought to look at non-printing chars
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        finally {
            if (TheStream != null) {
                try {
                    TheStream.close();
                }
                catch (IOException e) {
                    // forgive I guess
                }
            }
        }
        return (s.toString());
    }


    /**
     * { method
     *
     * @param dst destination file name
     * @param src source file name
     * @return true for success
     *         }
     * @name copyFile
     * @function copy file named src into new file named dst
     */
    public static boolean copyFile(File src, OutputStream dstFile) {
        int bufsize = 1024;
        try {
            RandomAccessFile srcFile = new RandomAccessFile(src, "r");
            long len = srcFile.length();
            if (len > 0x7fffffff) {
                return (false);
            }
            // too large
            int l = (int) len;
            if (l == 0) {
                return (false);
            }

            int bytesRead;
            byte[] buffer = new byte[bufsize];
            while ((bytesRead = srcFile.read(buffer, 0, bufsize)) != -1) {
                dstFile.write(buffer, 0, bytesRead);
            }
            srcFile.close();
            //         dstFile.close();
            return true;
        }
        catch (IOException ex) {
            return (false);
        }
    }

    /**
     * several classloader methods return  Enumeration<URL>  </URL>
     * @param first first element
     * @param others any other elements
     * @param <T>
     * @return
     */
    public static <T> Enumeration<T> fromItems(@NotNull T first, T... others) {
        Class<?> tCLass = first.getClass();
        //noinspection unchecked
        T[] items = (T[]) Array.newInstance(tCLass, 1 + others.length);
        items[0] = first;
          System.arraycopy(others, 0, items, 1, others.length);
        //noinspection unchecked
        return new ArrayEnumeration(items);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void copyDirectoryToJar(final File pSource, final JarOutputStream pOut,Set<String> existingEntries ) {
          if(!pSource.exists() || !pSource.isDirectory())
            throw new IllegalArgumentException("bad directory " + pSource);
        String top = pSource.getAbsolutePath().replace("\\","/");
        copyDirectoryToJar(  top,  pSource, pOut,existingEntries);

    }

    protected static void copyDirectoryToJar(String top,final File pSource, final JarOutputStream pOut,Set<String> existingEntries ) {
         File[] files = pSource.listFiles();
         if(files == null)
             return;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file.isDirectory()) {
                copyDirectoryToJar(top,file,pOut,existingEntries);
            }
            else {
                copyFileToJar(top, file, pOut,existingEntries);

            }
        }

    }


    protected static void copyFileToJar(String top,final File pSource, final JarOutputStream pOut,Set<String> existingEntries ) {
        String path = pSource.getAbsolutePath().replace("\\","/");
        path = path.substring(top.length());

        // disallow duplicates
        if(existingEntries.contains(path))
            return;
        existingEntries.add(path);
        JarEntry je = new JarEntry(path);

        try {
            pOut.putNextEntry(je);
            copyFile(pSource, pOut);
            pOut.closeEntry();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * return all items in the current classpath
     * @return  all classpath items
     */
    public static @NotNull String[] getClassPathItems()
    {
        String class_paths = System.getProperty("java.class.path");
         String[] cp = class_paths.split(System.getProperty("path.separator"));
        for (int i = 0; i < cp.length; i++) {
            cp[i] = cp[i].replace("\\","/");

        }
         return cp;
    }

    /**
     * An array enumeration. This allows me to return an Enumeration from an Arrey
     *
     * @author Andy Clark, IBM
     */
    private static class ArrayEnumeration<T> implements Enumeration<T> {
        /**
         * Array.
         */
        private T[] array;
        /**
         * Index.
         */
        private int index;

        //
        // Constructors
        //

        /**
         * Constructs an array enumeration.
         */
        public ArrayEnumeration(T[] array) {
            this.array = array;
        }
        //
        // Enumeration methods
        //

        /**
         * Tests if this enumeration contains more elements.
         *
         * @return <code>true</code> if this enumeration contains more elements;
         *         <code>false</code> otherwise.
         * @since JDK1.0
         */
        public boolean hasMoreElements() {
            return index < array.length;
        } // hasMoreElement():boolean

        /**
         * Returns the next element of this enumeration.
         *
         * @return the next element of this enumeration.
         * @throws NoSuchElementException if no more elements exist.
         * @since JDK1.0
         */
        public T nextElement() {
            if (index < array.length) {
                return array[index++];
            }
            throw new NoSuchElementException();
        } // nextElement():Object

    } // class ArrayEnumeration

}
