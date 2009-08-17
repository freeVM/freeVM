package org.crazynut.harmony.minjre.anttask;

import java.io.File;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant.Reference;
import org.apache.tools.ant.types.Path;
import org.crazynut.harmony.minjre.JreGenerator;

/**
 * The ant task that can generate a new JRE from a set of cns files.
 * 
 * @author  <A HREF="mailto:daniel.gong.fudan@gmail.com">Ling-Hui Gong</A>
 *
 */
public class GenerateJreTask extends Task {
	
	private File origin = null;
	
	private File target = null;
	
	private String jre = null;
	
	private Path path = null;
	
	public void execute() {
		if (null == origin || null == target 
				|| null == jre || 0 == path.size()) {
			log("Incorrect use of this task.\n");
			throw new BuildException();
		}
		JreGenerator gen = new JreGenerator(jre);
		String[] list = path.list();
		for (int i = 0; i < list.length; i++) {
			File current = new File(list[i]);
			if (current.isFile()) {
				if (current.getName().endsWith(".cns")) {
					gen.addReservedClasses(current);
				} else {
					throw new BuildException(list[i] 
				        + " is not a valid cns file.");
				}
			} else if (current.isDirectory()) {
				File[] files = current.listFiles();
				for (int j = 0; j < files.length; j++) {
					if (files[j].isFile() && files[j].getName().endsWith(".cns")) {
						gen.addReservedClasses(files[j]);
					}
				}
			} else {
				throw new BuildException(list[i]
			        + " does not exist.");
			}
		}
		log("Start to generate JRE.\n");
		log("Generating ...\n");
		gen.generateMinJre(origin.getAbsolutePath(), target.getAbsolutePath());
		log("Generating complete.\n");
	}

	public void setJre(String jre) {
		this.jre = jre;
	}
	
	public void setOrigin(File origin) {
		if (origin.isDirectory()) {
			this.origin = origin;
		} else {
			throw new BuildException(origin.getAbsolutePath() 
					+ " is not a valid existing directory.");
		}
	}
	
	public void setTarget(File target) {
		if (target.isDirectory() || target.mkdir()) {
			this.target = target;
		} else {
			throw new BuildException(target.getAbsolutePath()
					+ " does not exist or can not be created.");
		}
	}
	
	public Path createCnspath() {
        if (path == null) {
            path = new Path(getProject());
        }
        return path.createPath();
	}

}
