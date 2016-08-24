package demo.lxz.eclipsetoas2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.BitSet;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException {
		EclipseToAs ea = new EclipseToAs();
		ea.toAsProject(new File("E:\\mywork\\AndroidWork\\xf_zhkt\\SmartClass"));
	}

	public static void log(String msg) {
		System.out.println(msg);
	}
}
