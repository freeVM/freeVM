package java.lang;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.util.StringTokenizer;

final class SystemClassLoader extends URLClassLoader {

    //private Object[] path;
    private String sep;

    SystemClassLoader() {
        super(new URL[0]);
    }


    private void setup() {
        String classpath = System.getProperty("java.class.path");
        sep = System.getProperty("file.separator");
        String pathSep = System.getProperty("path.separator");
        StringTokenizer tokens = new StringTokenizer(classpath, pathSep);

        while (tokens.hasMoreElements()) {
            String filename = tokens.nextToken();

            System.err.println("user.dir = " + System.getProperty("user.dir"));

            try {
                File f = new File(filename);
                if (f.isDirectory()) {
                    String u = "file://" + f.getAbsolutePath() + "/";
                    System.err.println("url = " + u);
                    addURL(new URL(u));
                } else {
                    String u = "file://" + f.getAbsolutePath();
                    System.err.println("url(jar) = " + u);
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
        URL resource = getSystemResource(resName);
        if (resource != null) return resource;

        if (sep == null) setup();
        return findResource(resName);
	}
}
