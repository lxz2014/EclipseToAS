package demo.lxz.eclipsetoas2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EProjectInfo {
	// 标记是否为库项目
	private boolean isLibrary;
	
	// 工程名称
	private String name;
	
	// 工程目录
	private File dirFile;
	
	// 依赖的路径
	private List<String> dependentPaths = new ArrayList<String>();

	private boolean hasJni = false;

	//private String packName;
	
	/**
	 * 通过eclipse工程目录创建模型
	 * @param eclipseDirFile
	 * @return
	 */
	public static EProjectInfo createBy(File eclipseDirFile) {
		if (!isProject(eclipseDirFile)) {
			throw new RuntimeException("不是eclipse工程");
		}
		
		EProjectInfo p = new EProjectInfo();
		p.name = eclipseDirFile.getName();
		p.dirFile = eclipseDirFile;
		p.isLibrary = false;
		p.hasJni  = hasJni(eclipseDirFile);
		
		String ppstring = FileUtils.readString(new File(eclipseDirFile, "/project.properties"));
		if (!StringUtils.isEmpty(ppstring)) {
			ppstring = ppstring.replace(" ", "");
			String [] sp = ppstring.split("\\n");
			if (sp != null) {
				for (String line : sp) {
					if (line == null || line.startsWith("#")) {
						// 跳过注释行和空的行
						continue;
					}
					
					if (line.startsWith("android.library.reference.")) {
						String map[] = line.split("=");
						
						if (map.length == 2) {
							String dpath = FileUtils.checkPath(map[1]);
							p.addDependPath(FileUtils.realToAbs(p.getDirFile(), dpath));
						}
						else {
							System.err.println("依赖项目不正确:" + line);
						}
					}
					else if (line.startsWith("android.library=true")) {
						p.isLibrary = true;
					}
				}
			}
		}
		
		return p;
	}
	
	private static boolean hasJni(File f) {
		boolean isJni = new File(f, "libs/armeabi").exists();
		if (isJni) {
			System.out.println("有jni " + f.getPath());
			return true;
		}
		else {
			return false;
		}
	}

	private void addDependPath(String dpath) {
		if (!dependentPaths.contains(dpath)) {
			dependentPaths.add(dpath);
		}		
	}

	public boolean isLibrary() {
		return isLibrary;
	}
	
	public List<String> getDependentPath() {
		return dependentPaths;
	}
	
	public String getName() {
		return name;
	}
	
	public File getDirFile() {
		return dirFile;
	}
	
	/**
	 * 判断是否为一个eclipse项目
	 * @param f
	 * @return
	 */
	public static boolean isProject(File f) {
		File[] list = f.listFiles();
		
		int count = 0;
		for (File path : list) {
			
			if(".project".equals(path.getName())) {
				count ++;
			}
			if(".classpath".equals(path.getName())) {
				count ++;
			}
			if ("project.properties".equals(path.getName())) {
				count ++;
			}
			if ("AndroidManifest.xml".equals(path.getName())) {
				count ++;
			}
		}
		return count == 4;
	}
	
	@Override
	public String toString() {
		return String.format("name=%s, isLibrary=%b, dependentPath=%s"
				, name
				, isLibrary
				, Arrays.toString(dependentPaths.toArray()));
	}
	
	public void log() {
		System.out.println("->" + toString());
	}

	public String getDirPath() {
		return dirFile.getPath();
	}

	public boolean hasJni() {
		return hasJni;
	}

//	public String getPackName() {
//		return packName;
//	}
}
