package com.lordjoe.packager;

import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;


/**
 * com.lordjoe.packager.LoggingClassLoader
 * User: Steve
 * Date: 11/21/13
 */
public class LoggingClassLoader extends ClassLoader {

    private List<AbstractLoggingClassFinder> classPath = new ArrayList<AbstractLoggingClassFinder>();
    private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
    private Map<String, URL> resources = new HashMap<String, URL>();
    private Set<AbstractLoggingClassFinder> usedJars = new HashSet<AbstractLoggingClassFinder>();
    private JarOutputStream loadedClassesStream;
    private Class<?> m_MainClass;
    private final Properties m_Properties;

    protected static ClassLoader getGoodParentLoader()
    {
        Thread thread = Thread.currentThread();
        ClassLoader contextClassLoader = thread.getContextClassLoader();
        return contextClassLoader.getParent();
    }


    public LoggingClassLoader(Properties props) {
        // this will cause my parent to be
        super(getGoodParentLoader());
        m_Properties = props;
        buildPathLoaders();
        String classeseOutFile = props.getProperty("used_classes_jar");
        try {
            if (classeseOutFile != null) {
                loadedClassesStream = new JarOutputStream(new FileOutputStream(classeseOutFile));

                createManifest(props.getProperty("mainclass"));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    protected Properties getProperties() {
        return m_Properties;
    }

    public Class<?> getMainClass() {
        return m_MainClass;
    }

    /**
     * make  class finders - one per directory
     * is private final since it is called in the constructor
     */
    @SuppressWarnings("FinalPrivateMethod")
    private final void buildPathLoaders() {
        String cpString = getProperties().getProperty("classpath");
        String[] items = cpString.split(System.getProperty("path.separator"));
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < items.length; i++) {
            String item = items[i].replace("\\", "/");
            if (item.contains("/jre/lib"))
                continue; // this is a jre jar
            manageClassPathElement(item);

        }
    }

    /**
     * is private final since it is called in the constructor
     *
     * @param fileName one classpath element
     */
    @SuppressWarnings("FinalPrivateMethod")
    private final void manageClassPathElement(String fileName) {
        File f = new File(fileName);
        if (!f.exists())
            return;
        if (f.isFile()) {
            classPath.add(new JarClassLoader(this, f));
        }
        else {
            classPath.add(new DirectoryClassLoader(this, f));

        }
    }

    public JarOutputStream getLoadedClasses() {
        return loadedClassesStream;
    }

    public void setLoadedClasses(final OutputStream os) {
        try {
            loadedClassesStream = new JarOutputStream(os);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public void finishLoad() {
        if (loadedClassesStream != null) {
            try {
                loadedClassesStream.close();
                loadedClassesStream = null;
            }
            catch (IOException e) {
                // do nothing
            }
        }
    }

    protected void maybeSaveClass(String name, byte[] classBytes) throws IOException {
        JarOutputStream saver = getLoadedClasses();
        if (saver == null)
            return;
        JarEntry je = new JarEntry(name.replace(".", "/") + ".class");
        saver.putNextEntry(je);
        saver.write(classBytes);
        saver.closeEntry();
    }

    public static final String MAINFEST_LINES = "Manifest-Version: 1.0\n" +
            "Main-Class: <main_class>\n";


    protected void createManifest(String mainClass) throws IOException {
        JarOutputStream saver = getLoadedClasses();
        if (saver == null)
            return;
        String str = MAINFEST_LINES.replace("<main_class>", mainClass);
        JarEntry je = new JarEntry("META-INF/Manifest.mf");
        //   JarEntry je = new JarEntry("manifest.txt");
        saver.putNextEntry(je);
        saver.write(str.getBytes());
        saver.closeEntry();
    }


    public void setLoadedClasses(final File f) {
        try {
            setLoadedClasses(new FileOutputStream(f));
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public Set<AbstractLoggingClassFinder> getUsedJars() {
        return new HashSet<AbstractLoggingClassFinder>(usedJars);
    }

    @Override
    protected Class<?> findClass(final String className) throws ClassNotFoundException {
        Class<?> ret = classes.get(className);
        if (ret != null)
            return ret;
        if(isClassHandledByExtLoader(className))   {
            return getParent().loadClass(className);
        }
        ret = loadFromFinders(className);
        if (ret != null) {
            classes.put(className, ret);
            return ret;
        }
        ret = super.findClass(className);
        return ret;

    }

    protected Class<?> loadFromFinders(final String className) {
        for (AbstractLoggingClassFinder classLoader : classPath) {
            byte[] bytes = classLoader.getBytes(className);
            if (bytes != null) {
                Class<?> ret = defineClass(className, bytes, 0, bytes.length, null);
                classes.put(className, ret);
                usedJars.add(classLoader);
                return ret;
            }
        }
        return null;
    }


    protected static boolean isClassHandledByExtLoader(final String className)
    {
        if(className.startsWith("java."))
            return true;
        if(className.startsWith("javax."))
            return true;
        if(className.startsWith("com.sun."))
            return true;
        if(className.startsWith("org.xml."))
            return true;

        return false;
    }

    @Override
    protected synchronized Class<?> loadClass(final String className, final boolean resolve) throws ClassNotFoundException {

        Class<?> ret = classes.get(className);
        if (ret != null)
            return ret;
        if(isClassHandledByExtLoader(className))   {
             return getParent().loadClass(className);
         }
        ret = loadFromFinders(className);
        if (ret == null)
            ret = super.loadClass(className, resolve); // it may be in rt or not be found
        return ret;
    }

    /**
     * Finds the resource with the given name.  A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     * <p/>
     * <p> The name of a resource is a '<tt>/</tt>'-separated path name that
     * identifies the resource.
     * <p/>
     * <p> This method will first search the parent class loader for the
     * resource; if the parent is <tt>null</tt> the path of the class loader
     * built-in to the virtual machine is searched.  That failing, this method
     * will invoke {@link #findResource(String)} to find the resource.  </p>
     *
     * @param name The resource name
     * @return A <tt>URL</tt> object for reading the resource, or
     *         <tt>null</tt> if the resource could not be found or the invoker
     *         doesn't have adequate  privileges to get the resource.
     * @since 1.1
     */
    @Override
    public URL getResource(final String name) {
        URL url = resources.get(name);
        if(url != null)
            return url;
        url = super.getResource(name);
        if(url != null) {
            resources.put(name, url);
            return url;
        }
        ClassLoader parent = getParent();
        URL resource = parent.getResource(name);
        return resource;
    }

    /**
     * Loads the class with the specified <a href="#name">binary name</a>.
     * This method searches for classes in the same manner as the {@link
     * #loadClass(String, boolean)} method.  It is invoked by the Java virtual
     * machine to resolve class references.  Invoking this method is equivalent
     * to invoking {@link #loadClass(String, boolean) <tt>loadClass(name,
     * false)</tt>}.  </p>
     *
     * @param name The <a href="#name">binary name</a> of the class
     * @return The resulting <tt>Class</tt> object
     * @throws ClassNotFoundException If the class was not found
     */
    @Override
    public Class<?> loadClass(final String className) throws ClassNotFoundException {
     //   return loadClass(className, false);
        return loadClass(className, true);
      }

    /**
     * Finds the resource with the given name. Class loader implementations
     * should override this method to specify where to find resources.  </p>
     *
     * @param name The resource name
     * @return A <tt>URL</tt> object for reading the resource, or
     *         <tt>null</tt> if the resource could not be found
     * @since 1.2
     */
    @Override
    protected URL findResource(final String name) {
        URL url = resources.get(name);
        if(url != null)
            return url;
        url = super.findResource(name);
        if(url != null) {
            resources.put(name, url);
            return url;
        }
         return url;
       }

    /**
     * Finds all the resources with the given name. A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     * <p/>
     * <p>The name of a resource is a <tt>/</tt>-separated path name that
     * identifies the resource.
     * <p/>
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param name The resource name
     * @return An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
     *         the resource.  If no resources could  be found, the enumeration
     *         will be empty.  Resources that the class loader doesn't have
     *         access to will not be in the enumeration.
     * @throws java.io.IOException If I/O errors occur
     * @see #findResources(String)
     * @since 1.2
     */
    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        return super.getResources(name);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Returns an enumeration of {@link java.net.URL <tt>URL</tt>} objects
     * representing all the resources with the given name. Class loader
     * implementations should override this method to specify where to load
     * resources from.  </p>
     *
     * @param name The resource name
     * @return An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
     *         the resources
     * @throws java.io.IOException If I/O errors occur
     * @since 1.2
     */
    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
        return super.findResources(name);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Returns an input stream for reading the specified resource.
     * <p/>
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param name The resource name
     * @return An input stream for reading the resource, or <tt>null</tt>
     *         if the resource could not be found
     * @since 1.1
     */
    @Override
    public InputStream getResourceAsStream(final String name) {
        URL url = getResource(name);
        if(url == null) {
            ClassLoader parent = getParent();
            InputStream resourceAsStream = parent.getResourceAsStream(name);
            return resourceAsStream;
        }
        try {
             return url != null ? url.openStream() : null;
         } catch (IOException e) {
             return null;
         }
    }

    public void buildHadoopJar() {
        String hadoop_jar = getProperties().getProperty("hadoop_jar");
        JarOutputStream out = null;
        try {
            out = new JarOutputStream(new FileOutputStream(hadoop_jar));

            for (AbstractLoggingClassFinder loader : getUsedJars()) {
                if (loader.isJarLoader()) {
                    File source = loader.getSource();
                    String name = source.getName();
                    JarEntry next = new JarEntry("lib/" + name);
                    out.putNextEntry(next);
                    ClassLoaderUtilities.copyFile(source, out);
                }
                else {

                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    // ignore

                }
            }
        }
    }

    public Class<?> loadState() {

        try {
            //   Thread.currentThread().setContextClassLoader(this);
            Properties prop = getProperties();
            String mainclass = prop.getProperty("mainclass");
            m_MainClass = Class.forName(mainclass, true, this);
            // load other classes
            String otherClasses = prop.getProperty("load_by_name");
            if(otherClasses != null)    {
                String[] others = otherClasses.split(";") ;
                for (String other : others) {
                    Class.forName(other, true, this);
                }
            }
            finishLoad(); // close files
            String hadoop_jar = prop.getProperty("hadoop_jar");

            if (hadoop_jar != null)
                buildHadoopJar();

            return m_MainClass;
        }
        catch (ClassNotFoundException e) {
            return null;

        }

    }

    public Method getMainMethod() {
        if (getMainClass() == null)
            return null;
        try {
            Method mainMethid = getMainClass().getMethod("main", String[].class);
            return mainMethid;
        }
        catch (NoSuchMethodException e) {
            return null;

        }

    }

    public static void usage() {
        System.out.println("Usage <propertyfile>");
        System.out.println("Like");
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public static final int REQUIRED_ARGS = 1;


    public static void main(String[] args) throws Exception {
        if (args.length < REQUIRED_ARGS) {
            usage();
            return;
        }

        Properties prop = ClassLoaderUtilities.readProperties(args[0]);
        LoggingClassLoader loader = new LoggingClassLoader(prop);

        Class<?> mailCls = loader.loadState();

        String passedargs = prop.getProperty("arguments");
        String[] argsArray =   passedargs.trim().split(" ");

        System.setProperty("user.dir", prop.getProperty("user_dir"));

        String classPath = System.getProperty("java.class.path");
        String[] items = classPath.split(System.getProperty("path.separator")) ;
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            System.out.println(item);
        }

        File test = new File(args[0]);
        boolean moved = !test.exists();

        Method mainMethod = loader.getMainMethod();

        Object[] passedArgs = { argsArray };
        try {
            mainMethod.invoke(null, passedArgs);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);

        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(e);

        }
        catch (InvocationTargetException e) {
            Throwable ex = e;
            Throwable cause = ex.getCause();
            while(cause != null && cause != ex) {
                ex = cause;
                cause = ex.getCause();
            }
            ex.printStackTrace();

        }


    }


}
