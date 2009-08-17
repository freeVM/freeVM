package org.crazynut.harmony.minjre.bin;

import java.io.File;

import org.crazynut.harmony.minjre.JreGenerator;

/**
 * The main class of the JRE generator of MinJre Toolkit.<p>
 * 
 * @author <A HREF="mailto:daniel.gong.fudan@gmail.com">Ling-Hui Gong</A>
 *
 */
public class GenerateJre {

	private static String jre = null;
	
	private static File[] cnsPath = null;
	
	private static String toPath = null;
	
	private static String fromPath = null;
	
	private static String msg = "";
	
	/**
	 * Parse the arguments.
	 * 
	 * @param args the arguments
	 * @return parsing result
	 */
	private static int parseArgs(final String[] args) {
		if (args.length < 4) {
			return -1;
		} else {
			String[] typeArg = args[0].split("=");
			String[] pathArg = args[1].split("=");
			String[] originArg = args[2].split("=");
			String[] targetArg = args[3].split("=");
			if (!typeArg[0].equals("-jre") || !pathArg[0].equals("-cnspath") || !originArg[0].equals("-origin") || !targetArg[0].equals("-target") 
					|| typeArg.length != 2 || pathArg.length != 2 || originArg.length != 2 || targetArg.length != 2) {
				return -1;
			} else {
				//File ini = new File("conf" + File.separator + typeArg[1] + ".cns");
				//iniFile = ini.getAbsolutePath();
				jre = typeArg[1];
				String[] paths = pathArg[1].split(";");
				cnsPath = new File[paths.length];
				for (int i = 0; i < paths.length; i++) {
					cnsPath[i] = new File(paths[i]);
					if (!cnsPath[i].exists()) {
						msg = paths[i] + " does not exist.";
						return 0;
					}
					if (cnsPath[i].isFile() && !cnsPath[i].getName().endsWith(".cns")) {
						msg = paths[i] + " is not a cns file.";
						return 0;
					}
				}
			}
			if (new File(originArg[1]).isDirectory()) {
				fromPath = originArg[1];
			} else {
				msg = originArg[1] + " does not exist as a directory.";
				return 0;
			}
			if (new File(targetArg[1]).isDirectory()) {
				toPath = targetArg[1];
			} else {
				msg = targetArg[1] + " does not exist as a directory.";
				return 0;
			}
		}
		return 1;
	}
	
	/**
	 * Print out usage of the program.
	 */
	private static void printUsage() {
		System.out.println(
				"Usage: jregen -jre=<jretype> -cnspath=<cns path list> -origin=<originjre> -target=<targetdir>\n" 
				+ "\tThe cns path list is a ; seperated list of cns files and directories which containing cns files needed to generate the jre.\n" 
				+ "\tExample: jregen -jre=harmony -cnspath=c:\\app\\module1\\cns;c:\\app\\module2\\cns -origin=%JRE_HOME% -target=c:\\java\\minjre"
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
				JreGenerator gen = new JreGenerator(jre);			
				for (int i = 0; i < cnsPath.length; i++) {
					if (cnsPath[i].isDirectory()) {
						File[] list = cnsPath[i].listFiles();
						for (int j = 0; j < list.length; j++) {
							if (list[j].getName().endsWith(".cns")) {
								gen.addReservedClasses(list[j]);
							}
						}
					} else if (cnsPath[i].isFile()) {
							gen.addReservedClasses(cnsPath[i]);
					}
				}
				gen.generateMinJre(fromPath, toPath);
				break;
		default:
			break;
		}
	}
}
