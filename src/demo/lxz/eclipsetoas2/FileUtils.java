package demo.lxz.eclipsetoas2;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class FileUtils {
	
	/**
	 * 相对转绝对路径
	 * @param dir
	 * @param realPath
	 * @return
	 */
	public static String realToAbs(File dir, String childPath) {
		try {
			return new File(dir, childPath).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 绝对路径转相对路径
	 * @param basePath
	 * @param dstPath
	 * @return
	 */
	public static String absToReal(File basePath, File dstPath) {
		return absToReal(basePath.getPath(), dstPath.getPath());
	}
	
	public static String absToReal(String basePath, String dstPath) {
		basePath = replacePath(basePath);
		dstPath = replacePath(dstPath);
		
		String sp1[] = basePath.split("/");
		String sp2[] = dstPath.split("/");
		
		int lev = 0;
		for (int i = 0; i < sp1.length && i < sp2.length; i++) {
			//System.out.println("sp->" + sp1[i] + "->" + sp2[i]);
			lev = i;
			if (!sp1[i].equals(sp2[i])) break;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sp1.length - lev; i++) {
			sb.append("../");
		}
		
		for (int i = lev; i < sp2.length; i++) {
			sb.append(sp2[i]).append("/");
		}
		
		return sb.toString();
	}

	public static String readString(String f) {
		return readString(new File(f));
	}
	
	public static void writeString(String file, String string) {
		writeString(new File(file), string);
	}
	
	/**
	 * @param args
	 */
	public static String readString(File f) {
		BufferedSource source = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(f);
			source = Okio.buffer(Okio.source(in));
			return source.readUtf8();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(in);
			close(source);
		}
		return null;
	}
	
	
	private static void close(Closeable in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void  writeString(File file, String string) {
		FileOutputStream os = null;
		BufferedSink source = null;
		try {
			os = new FileOutputStream(file);
			source = Okio.buffer(Okio.sink(os));
			source.writeUtf8(string);
			source.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(os);
			close(source);
		}
	}
	
	/** 
	 * 复制一个目录及其子目录、文件到另外一个目录 
	 * @param src 
	 * @param dest 
	 * @throws IOException 
	 */  
	public static void copyFolder(File src, File dest) throws IOException {  
	    if (src.isDirectory()) {  
	        if (!dest.exists()) {  
	            dest.mkdir();  
	        }  
	        String files[] = src.list();  
	        for (String file : files) {  
	            File srcFile = new File(src, file);  
	            File destFile = new File(dest, file);  
	            // 递归复制  
	            copyFolder(srcFile, destFile);  
	        }  
	    } else {  
	        InputStream in = new FileInputStream(src);  
	        OutputStream out = new FileOutputStream(dest);  
	  
	        byte[] buffer = new byte[4096];  
	        int length;  
	        while ((length = in.read(buffer)) > 0) {  
	            out.write(buffer, 0, length);  
	        }  
	        in.close();  
	        out.close();  
	    }  
	}

	public static String replacePath(String path) {
		return path.replace("\\", "/")
					.replace("\\\\", "/")
					.replace("//", "/");
	}

	public static void rename(String path1, String path2) {
		File f1 = new File(path1);
		File f2 = new File(path2);
		
		if (f1.exists()) {
			if (f1.renameTo(f2)) {
				System.out.println("重命名成功");
			}
		}
	}  
	
	
	/** 
	 *  根据路径删除指定的目录或文件，无论存在与否 
	 *@param sPath  要删除的目录或文件 
	 *@return 删除成功返回 true，否则返回 false。 
	 */  
//	public static boolean deleteFolder(File file) {  
//	    // 判断目录或文件是否存在  
//	    if (!file.exists()) {  // 不存在返回 false  
//	        return false;  
//	    } else {  
//	        // 判断是否为文件  
//	        if (file.isFile()) {  // 为文件时调用删除文件方法  
//	            return file.delete();  
//	        } else {  // 为目录时调用删除目录方法  
//	            return deleteFolder(file);  
//	        }  
//	    }  
//	}  

}
