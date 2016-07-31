package demo.lxz.eclipsetoas2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 转换工具
 * @author lxz
 */
public class EclipseToAs {
	final static String runDir = System.getProperty("user.dir");
	
	// 记录所有的eclipse工程目录
	private Map<String, EProjectInfo> eprojects = new HashMap<String, EProjectInfo>();
	
	public void toAsProject(File rootFile) {
		// step1 遍历所有的工程目录
		traverseDir(rootFile, 0);
		
		// step2 找出所有依赖并生成脚本
		createGraldeScript();
	}
	
	/**
	 * 遍历所有的eclipse的项目目录
	 * @param rootFile
	 */
	private void traverseDir(File rootFile, int layer) {
		if (isFilter(rootFile)) {
			return;
		}
		
		// 如果根目录就是叶子节点(TODO eclipse项目目录即是叶子节点)
		if (layer == 0 && EProjectInfo.isProject(rootFile)) {
			eprojects.put(rootFile.getPath(), EProjectInfo.createBy(rootFile));
			return;
		}
		
		if (rootFile.exists() && rootFile.isDirectory()) {
			File [] childs = rootFile.listFiles();
			for (File file : childs) {
				
				if (file.isDirectory()) {
					if (EProjectInfo.isProject(file)) {
						EProjectInfo info = EProjectInfo.createBy(file);
						eprojects.put(file.getPath(), info);
						
						info.log();
					}
					else {
						traverseDir(file, layer + 1);
					}
				}
			}
		}
	}

	/**
	 * 构建依赖关系
	 */
	private void createGraldeScript() {
		Set<Entry<String, EProjectInfo>> entrys = eprojects.entrySet();
		for (Entry<String, EProjectInfo> en : entrys) {
			EProjectInfo info = en.getValue();
			// 主项目
			if (!info.isLibrary()) {
				System.out.println(String.format("主项目%s--- start ", info.getName()));
				// 生成 settings.gradle
				createSettingsGradle(info);
				
				// 生成 build.gradle
				createBuildGradle(info);
				
				// copy gradle file
				copyGradle(info);
				
				System.out.println(String.format("主项目%s--- end\n\n", info.getName()));
			}
			// 库项目
			else {
				createBuildGradle(info);
				// 重新生成AndroidManifest.xml
				createAndroidManifestXml(info);
			}
		}		
	}
	
	private  static Pattern regex =Pattern.compile("package=\"(.*)\"");

	/**
	 * 重新创建
	 */
	private void createAndroidManifestXml(EProjectInfo info) {
		// 备份
		String path = info.getDirPath() + "/AndroidManifest.xml";
		FileUtils.rename(path, path + ".bak");
		String packName = readPackName(path + ".bak");
		System.out.println("\n解析出包名称:" + packName + "\n");
		
		String templet = FileUtils.readString(runDir + "/res/AndroidManifest.xml");

		// 重命名包名称和编译版本
		FileUtils.writeString(path, String.format(templet, packName, 11, 21));
	}


	private String readPackName(String path) {
		Matcher mat = regex.matcher(FileUtils.readString(path));
		 if ( mat.find()) {
			 return mat.group(1);
         }
		return "";
	}

	/**
	 * 构建 build.gradle 脚本
	 * @param info
	 * @return
	 */
	private void createBuildGradle(EProjectInfo info) {
		String templet = readBuildGradleTemplet(info);
		
		StringBuilder sb = new StringBuilder();
		for (String dependpath : info.getDependentPath()) {
			EProjectInfo p = eprojects.get(dependpath);
			if (p != null) {
				sb.append(String.format("\tcompile project(':%s')\n", p.getName()));
			}
			else {
				System.out.println("失效的依赖项目" + dependpath);
			}
		}
		
		String jni = info.hasJni() ? "jniLibs.srcDirs = ['libs']" : "";
		// 写入脚本
		FileUtils.writeString(info.getDirPath() + "/build.gradle", String.format(templet, sb.toString(), jni));
	}
	
	/**
	 * 拷贝gradle文件
	 * @param info
	 */
	private void copyGradle(EProjectInfo info) {
		File srcFile = new File(runDir + "/res/gradle");
		File dstFile = new File(info.getDirPath() + "/gradle");
		try {
			FileUtils.copyFolder(srcFile, dstFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("copy gralde dir error");
		}		
	}
	
	/**
	 * 读取build.gradle的脚本模板
	 * @param info
	 * @return
	 */
	private String readBuildGradleTemplet(EProjectInfo info) {
		return FileUtils.readString(runDir + "/res/build.gradle" + (info.isLibrary() ? ".lib" : ".main"));
	}
	
	/**
	 * 创建主项目的 setting.gradle
	 * 该文件主要构成gralde所有依赖项目
	 * @param info
	 */
	private void createSettingsGradle(EProjectInfo info) {
		List<String> alldependlibpaths = new ArrayList<String>();
		findAlldDependLib(info, alldependlibpaths);
		
		StringBuilder sb = new StringBuilder();
		for (String libpath : alldependlibpaths) {
			EProjectInfo dependlib = eprojects.get(libpath);
			
			String libName = dependlib.getName();
			String libRealPath = FileUtils.absToReal(info.getDirPath(), dependlib.getDirPath());
			
			sb.insert(0, String.format("include ':%s'\n", libName));
			sb.append(String.format("project(':%s').projectDir = new File('%s')\n"
									, libName
									, libRealPath ));
			
			log(libRealPath);
		}
		
		FileUtils.writeString(info.getDirPath() + "/settings.gradle", sb.toString());
	}

	/**
	 * 找出所有的依赖库项目
	 * @param info
	 * @param allDepends
	 */
	private void findAlldDependLib(EProjectInfo info, List<String> allDepends) {
		List<String> dependlibs = info.getDependentPath();
		if (dependlibs.size() == 0) {
			return;
		}
		
		for (String path : dependlibs) {
			EProjectInfo libInfo = eprojects.get(path);
			if (libInfo != null) {
				if (!allDepends.contains(libInfo.getDirPath())) {
					allDepends.add(libInfo.getDirPath());
					findAlldDependLib(libInfo, allDepends);
				}
				else {
					System.out.println("貌似重复依赖了:" + path);
				}
			}
			else {
				System.out.println("失效的依赖库项目:" + path);
			}
		}
	}
	
	// 过滤
	private boolean isFilter(File rootFile) {
		if (rootFile.isHidden()) {
			return true;
		}
		
		if (rootFile.isFile()) {
			return true;
		}
		
		if (isEmpty(rootFile.list())) {
			return true;
		}
		
//		if ("build".equals(rootFile.getName()) && rootFile.isDirectory()) {
//			return true;
//		}
		
		return false;
	}
	
	private boolean isEmpty(String[] list) {
		return list != null && list.length == 0;
	}
	
	private void log(String string) {
		System.out.println(string);
	}

}
