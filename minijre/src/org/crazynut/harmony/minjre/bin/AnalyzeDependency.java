package org.crazynut.harmony.minjre.bin;

import java.io.File;

import org.crazynut.harmony.minjre.StaticDependencyAnalyzer;

/**
 * The main class of the static dependency analyzer of MinJre Toolkit.<p>
 * 
 * @author <A HREF="mailto:daniel.gong.fudan@gmail.com">Ling-Hui Gong</A>
 *
 */
public class AnalyzeDependency {

	private static String jreLibPath = null;
	
	private static String[] rootPath = null;
	
	private static String aimFile = null;
	
	private static String msg = "";
	
	/**
	 * Parse the arguments.
	 * 
	 * @param args the arguments
	 * @return parsing result
	 */
	private static int parseArgs(final String[] args) {
		if (args.length < 3) {
			return -1;
		} else {
			String[] libArg = args[0].split("=");
			String[] pathArg = args[1].split("=");
			String[] aimArg = args[2].split("=");
			if (!libArg[0].equals("-jrelib") || !pathArg[0].equals("-classpath") || !aimArg[0].equals("-aim") 
					|| libArg.length != 2 || pathArg.length != 2 || aimArg.length != 2) {
				return -1;
			} else {
				File libFile = new File(libArg[1]);
				if (!libFile.exists() || libFile.isFile()) {
					msg = libArg[1] + " does not exists as a directory.";
					return 0;
				}
				jreLibPath = libArg[1];
				String[] paths = pathArg[1].split(";");
				for (int i = 0; i < paths.length; i++) {
					File file = new File(paths[i]);
					if (!file.exists() || (file.isFile() && !paths[i].endsWith(".jar"))) {
						msg = file + " is not a valid class path.";
						return 0;
					}
				}
				rootPath = paths;
			}
			if (aimArg[1].endsWith(".cns")) {
				aimFile = aimArg[1];
			} else {
				msg = aimArg[1] + " should be a cns file.";
				return 0;
			}
		}
		return 1;
	}
	
	/**
	 * 
	 */
	private static void printUsage() {
		System.out.println(
				"Usage: analyze -jrelib=<jre lib dir> -classpath=<root path list> -aim=<aim file path>\n" 
				+ "\tThe root path list is a ; seperated list of directories or jar files or just class files, which containing all classes in the application.\n" 
				+ "\tExample: analyze -jrelib=%JRE_HOME%\\lib -classpath=c:\\app\\module1;c:\\app\\lib\\lib1.jar c:\\app\\cns\\static.cns"
				);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int flag = parseArgs(args);
		switch (flag) {
			case -1:
				printUsage();
				break;
			case  0:
				System.out.println(msg);
				break;
			case  1:
				StaticDependencyAnalyzer ana = new StaticDependencyAnalyzer();
				ana.setJreLibPath(jreLibPath);
				for (int i = 0; i < rootPath.length; i++) {
					ana.addClassPath(rootPath[i]);
				}
				ana.getDependentClasses(aimFile);
				break;
		default:
			break;
		}
	}
}
