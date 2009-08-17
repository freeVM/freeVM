package org.crazynut.harmony.minjre.anttask;

import java.util.Vector;
import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant.Reference;
import org.apache.tools.ant.types.Path;
import org.crazynut.harmony.minjre.StaticDependencyAnalyzer;

/**
 * The ant task that can analyze static dependency of a set of classes.
 * 
 * @author  <A HREF="mailto:daniel.gong.fudan@gmail.com">Ling-Hui Gong</A>
 *
 */
public class AnalyzeDependencyTask extends Task{
	
	private File aim = null;
	
	private File jrelib = null;
	
	private Path path = null;
	
	public void execute() {
		if (null == aim || null == jrelib || 0 == path.size()) {
			log("Incorrect use of this task.\n");
			throw new BuildException();
		}

		String[] list = path.list();
		for (int i = 0; i < list.length; i++) {
			File file = new File(list[i]);
			if (file.isFile() && (!file.getName().endsWith(".jar") || !file.getName().endsWith(".class"))) {
				throw new BuildException(list[i] 
				        + " is not a valid classpath.");
			}
		}
		log("Start to analyze static dependency.\n");
		log("Analyzing ...\n");
		StaticDependencyAnalyzer ana = new StaticDependencyAnalyzer();
		ana.setJreLibPath(jrelib.getAbsolutePath());
		for (int i = 0; i < list.length; i++) {
			ana.addClassPath(list[i]);
		}
		ana.getDependentClasses(aim.getAbsolutePath());
		log("Analyzing complete.\n");
	}
	
	public void setAim(File aim) {
		if (aim.isFile() || aim.getName().endsWith(".cns")) {
			this.aim = aim;
		} else {
			throw new BuildException(aim.getAbsolutePath() 
					+ " is not a valid cns file.");
		}
	}
	
	public void setJrelib(File jrelib) {
		if (jrelib.isDirectory()) {
			this.jrelib = jrelib;
		} else {
			throw new BuildException(jrelib.getAbsolutePath()
					+ " is not a valid directory.");
		}
	}

	public Path createClasspath() {
        if (path == null) {
            path = new Path(getProject());
        }
        return path.createPath();
	}

}
