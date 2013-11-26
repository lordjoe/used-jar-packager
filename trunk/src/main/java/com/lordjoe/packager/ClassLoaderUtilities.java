package com.lordjoe.packager;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.packager.ClassLoaderUtilities
 * User: Steve
 * Date: 11/22/13
 */
public class ClassLoaderUtilities {



    public static Properties readProperties(String arg)    {
        try {
            File f = new File(arg) ;
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

            int bytesRead = 0;
            byte[] buffer = new byte[bufsize];
            while ((bytesRead = srcFile.read(buffer, 0, bufsize)) != -1) {
                dstFile.write(buffer, 0, bytesRead);
            }
            srcFile.close();
   //         dstFile.close();
            return true;
        } catch (IOException ex) {
            return (false);
        }
    }


}
