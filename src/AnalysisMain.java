import java.io.File;

import soot.PackManager;
import soot.Transform;

public class AnalysisMain {
	static String codepath = "code";//"/Users/zqh/Downloads/project1/code";
	// args[0] = "/root/workspace/code"
	// args[1] = "test.Hello"	
	public static void main(String[] args) {		
		//String jdkLibPath = System.getProperty("java.home")+"/lib/"; // "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/";
		String classpath = codepath 
				+ File.pathSeparator + codepath + File.separator + "rt.jar"
				+ File.pathSeparator + codepath + File.separator + "jce.jar";	
		System.out.println(classpath);
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.mypta", new Transformer()));
		soot.Main.main(new String[] {
			"-w",
			"-p", "cg.spark", "enabled:true",
			"-p", "wjtp.mypta", "enabled:true",
			"-soot-class-path", classpath,
			"test.FieldSensitivity"//args[1]				
		});
	}

}