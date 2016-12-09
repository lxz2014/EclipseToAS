package demo.lxz.eclipsetoas2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.Random;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException {
//		EclipseToAs ea = new EclipseToAs();
//		ea.toAsProject(new File("E:\\mywork\\xf_zhkt\\OriginMicroClass\\ForTeacher\\OnClassworkMain"));
		
		EclipseToAs2 ea = new EclipseToAs2();
		ea.toAsProject(new File("E:\\mywork\\xf_zhkt\\OriginMicroClass\\ForTeacher\\OnClassworkMain"));
		
		Random rand = new Random();
//		for (int i = 0; i < 100; i++) {
//			System.out.println("--> " + r.nextInt(2));
//		}
		
//		int max = 1000 / 100;
//		
//		for (int i = 0; i < 100; i++) {
//	    	int index = rand.nextInt(max + 1);
//	    	long randtime = index * 100;
//	    	System.out.println("--> " + randtime);
//		}
	}

	public static void log(String msg) {
		System.out.println(msg);
	}
}
