package java.lang;

import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;
import java.util.StringTokenizer;


// The class is needed to support
// org.apache.harmony.kernel.vm.VM.bootCallerClassLoader()
// which is required for support of ResourceBundles in system classes. 
// FIXME: permissions
public final class BootstrapClassLoader extends URLClassLoader {

    //private Object[] path;
    private String sep;
    private static ClassLoader cl;

    BootstrapClassLoader() {
        super(new URL[0]);
    }

    public static synchronized ClassLoader getBootstrapClassLoader() {
        if (cl == null) cl = new BootstrapClassLoader();
        return cl;
    }


    private void setup() {
        String classpath = System.getProperty("java.boot.class.path");
        sep = System.getProperty("file.separator");
        String pathSep = System.getProperty("path.separator");
        StringTokenizer tokens = new StringTokenizer(classpath, pathSep);

        while (tokens.hasMoreElements()) {
            String filename = tokens.nextToken();

            //System.err.println("user.dir = " + System.getProperty("user.dir"));

            try {
                File f = new File(filename);
                if (f.isDirectory()) {
                    String u = "file://" + f.getAbsolutePath() + "/";
                    //System.err.println("url = " + u);
                    addURL(new URL(u));
                } else {
                    String u = "file://" + f.getAbsolutePath();
                    //System.err.println("url(jar) = " + u);
                    addURL(new URL(u));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }


	protected Class findClass(String className) throws ClassNotFoundException {
        if (sep == null) setup();
        return super.findClass(className);
    }

	public URL getResource(String resName) {
        if (sep == null) setup();
        return findResource(resName);
	}
}
