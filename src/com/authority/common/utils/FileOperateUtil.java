package com.authority.common.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class FileOperateUtil {
	
	/**  
     *  新建目录  
     *  @param  folderPath  String  如  c:/fqf  
     *  @return  boolean  
     */  
   public  void  newFolder(String  folderPath)  {  
       try  {  
           String  filePath  =  folderPath;  
           filePath  =  filePath.toString();  
           java.io.File  myFilePath  =  new  java.io.File(filePath);  
           if  (!myFilePath.exists())  {  
               myFilePath.mkdir();  
           }  
       }  
       catch  (Exception  e)  {  
           System.out.println("新建目录操作出错");  
           e.printStackTrace();  
       }  
   }  
 
   /**  
     *  新建文件  
     *  @param  filePathAndName  String  文件路径及名称  如c:/fqf.txt  
     *  @param  fileContent  String  文件内容  
     *  @return  boolean  
     */  
   public  void  newFile(String  filePathAndName,  String  fileContent)  {  
 
       try  {  
           String  filePath  =  filePathAndName;  
           filePath  =  filePath.toString();  //取的路径及文件名
           File  myFilePath  =  new  File(filePath);  
           /**如果文件不存在就建一个新文件*/
           if  (!myFilePath.exists())  {  
               myFilePath.createNewFile();  
           }  
           FileWriter  resultFile  =  new  FileWriter(myFilePath);  //用来写入字符文件的便捷类, 在给出 File 对象的情况下构造一个 FileWriter 对象
           PrintWriter  myFile  =  new  PrintWriter(resultFile);  //向文本输出流打印对象的格式化表示形式,使用指定文件创建不具有自动行刷新的新 PrintWriter。
           String  strContent  =  fileContent;  
           myFile.println(strContent);  
           resultFile.close();  
 
       }  
       catch  (Exception  e)  {  
           System.out.println("新建文件操作出错");  
           e.printStackTrace();  
 
       }  
 
   }
	/**
	 * 删除文件
	 * 
	 * @param filePathAndName
	 *            String 文件路径及名称 如c:/fqf.txt
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public static boolean delFile(String filePathAndName) {
		File myDelFile = new java.io.File(filePathAndName);
		if (!myDelFile.exists()) {
			return true;
		}
		return myDelFile.delete();
	}

	/**
	 * 删除指定文件路径下面的所有文件和文件夹
	 * 
	 * @param file
	 */
	public static boolean delFiles(File file) {
		boolean flag = false;
		try {
			if (file.exists()) {
				if (file.isDirectory()) {
					String[] contents = file.list();
					for (int i = 0; i < contents.length; i++) {
						File file2X = new File(file.getAbsolutePath() + "/" + contents[i]);
						if (file2X.exists()) {
							if (file2X.isFile()) {
								flag = file2X.delete();
							} else if (file2X.isDirectory()) {
								delFiles(file2X);
							}
						} else {
							throw new RuntimeException("File not exist!");
						}
					}
				}
			//	System.out.println(file.getAbsolutePath());
				flag = file.delete();
			} else {
				throw new RuntimeException("File not exist!");
			}
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 判断文件名是否已经存在，如果存在则在后面家(n)的形式返回新的文件名，否则返回原始文件名 例如：已经存在文件名 log4j.htm
	 * 则返回log4j(1).htm
	 * 
	 * @param fileName
	 *            文件名
	 * @param dir
	 *            判断的文件路径
	 * @return 判断后的文件名
	 */
	public static String checkFileName(String fileName, String dir) {
		if(!dir.endsWith(File.separator))
			dir=dir+File.separator;			
		boolean isDirectory = new File(dir + fileName).isDirectory();
		if (FileOperateUtil.isFileExist(fileName, dir)) {
			int index = fileName.lastIndexOf(".");
			StringBuffer newFileName = new StringBuffer();
			String name = isDirectory ? fileName : fileName.substring(0, index);
			String extendName = isDirectory ? "" : fileName.substring(index);
			int nameNum = 1;
			while (true) {
				newFileName.append(name).append("(").append(nameNum).append(")");
				if (!isDirectory) {
					newFileName.append(extendName);
				}
				if (FileOperateUtil.isFileExist(newFileName.toString(), dir)) {
					nameNum++;
					newFileName = new StringBuffer();
					continue;
				}
				return newFileName.toString();
			}
		}
		return fileName;
	}
	
	/**  
     *  复制单个文件  
     *  @param  oldPath  String  原文件路径  如：c:/fqf.txt  
     *  @param  newPath  String  复制后路径  如：f:/fqf.txt  
     *  @param  cover    Boolean 是否覆盖 true or false(rename)
     *  @return  boolean  
     */  
   public  void  copyFile(String  oldPath,  String  newPath, Boolean cover )  {
       try  {  
//           int  bytesum  =  0;  
           int  byteread  =  0;  
           File  oldfile  =  new  File(oldPath);  
           if  (oldfile.exists())  {  //文件存在时  
               InputStream  inStream  =  new  FileInputStream(oldPath);  //读入原文件  
               
               if(isFileExist(newPath, "")&&!cover){
            	   File tmp=new File(newPath);
            	   newPath=tmp.getParent()+File.separator+checkFileName(tmp.getName(),tmp.getParent());
            	   tmp=null;
               }
               
               FileOutputStream  fs  =  new  FileOutputStream(newPath);  
               byte[]  buffer  =  new  byte[1444];  
//               int  length;  
               while  (  (byteread  =  inStream.read(buffer))  !=  -1)  {  
//                   bytesum  +=  byteread;  //字节数  文件大小  
//                   System.out.println(bytesum);  
                   fs.write(buffer,  0,  byteread);  
               }
               fs.close();
               inStream.close();  
           }  
       }  
       catch  (Exception  e)  {  
           System.out.println("复制单个文件操作出错");  
           e.printStackTrace();  
 
       }  
 
   }  
 
   /**  
    *  复制整个文件夹内容  
    *  @param  oldPath  String  原文件路径  如：c:/fqf  
    *  @param  newPath  String  复制后路径  如：f:/fqf/ff  
    *  @param  cover    Boolean 是否覆盖 true or false 
    *  @return  boolean  
    */  
  public  void  copyFolder(String  oldPath,  String  newPath, Boolean cover)  {

      try  {  
          (new  File(newPath)).mkdirs();  //如果文件夹不存在  则建立新文件夹  
          File  a=new  File(oldPath);  
          String[]  file=a.list();  
          File  temp=null;  
          for  (int  i  =  0;  i  <  file.length;  i++)  {  
              if(oldPath.endsWith(File.separator)){  
                  temp=new  File(oldPath+file[i]);  
              }  
              else{  
                  temp=new  File(oldPath+File.separator+file[i]);  
              }  

              if(temp.isFile()){
            	  
                  if(newPath.endsWith(File.separator))
                	  copyFile(temp.getAbsolutePath(),newPath+temp.getName(),cover);
                  else
                	  copyFile(temp.getAbsolutePath(),newPath+File.separator+temp.getName(),cover);
                  
              }  
              if(temp.isDirectory()){//如果是子文件夹  
                  copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i],cover);  
              }  
          }  
      }  
      catch  (Exception  e)  {  
          System.out.println("复制整个文件夹内容操作出错");  
          e.printStackTrace();  

      }  

  }
  /**  
   *  移动文件到指定目录  
   *  @param  oldPath  String  如：c:/fqf.txt  
   *  @param  newPath  String  如：d:/fqf.txt  
   *  @param  cover    Boolean 是否覆盖
   */  
	 public  void  moveFile(String  oldPath,  String  newPath, Boolean cover)  {
	     copyFile(oldPath,  newPath, cover);  
	     delFile(oldPath);  
	
	 }
	
 /**  
	 *  移动文件到指定目录  
	 *  @param  oldPath  String  如：c:/fqf.txt  
	 *  @param  newPath  String  如：d:/fqf.txt  
	 *  @param  cover    Boolean 是否覆盖
	 */  
	   public  void  moveFolder(String  oldPath,  String  newPath, Boolean cover)  {  
	       copyFolder(oldPath,  newPath, cover);  
	       delFiles(new File(oldPath));  
	 
	   }
	/**
	 * 返回上传的结果，成功与否
	 * 
	 * @param uploadFileName
	 * @param savePath
	 * @param uploadFile
	 * @return
	 */
	public static boolean upload(String uploadFileName, String savePath, File uploadFile) {
		boolean flag = false;
		try {
			uploadForName(uploadFileName, savePath, uploadFile);
			flag = true;
		} catch (IOException e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * 上传文件并返回上传后的文件名
	 * 
	 * @param uploadFileName
	 *            被上传的文件名称
	 * @param savePath
	 *            文件的保存路径
	 * @param uploadFile
	 *            被上传的文件
	 * @return 成功与否
	 * @throws IOException
	 */
	public static String uploadForName(String uploadFileName, String savePath, File uploadFile) throws IOException {
		String newFileName = checkFileName(uploadFileName, savePath);
		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			fos = new FileOutputStream(savePath + newFileName);
			fis = new FileInputStream(uploadFile);
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				throw e;
			}
		}
		return newFileName;
	}

	/**
	 * 根据路径创建一系列的目录
	 * 
	 * @param path
	 */
	public static boolean mkDirectory(String path) {
		File file = null;
		try {
			file = new File(path);
			if (!file.exists()) {
				return file.mkdirs();
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		} finally {
			file = null;
		}
		return false;
	}

	/**
	 * 将对象数组的每一个元素分别添加到指定集合中,调用Apache commons collections 中的方法
	 * 
	 * @param collection
	 *            目标集合对象
	 * @param arr
	 *            对象数组
	 */
	public static void addToCollection(Collection collection, Object[] arr) {
		if (null != collection && null != arr) {
			CollectionUtils.addAll(collection, arr);
		}
	}

	/**
	 * 将字符串已多个分隔符拆分为数组,调用Apache commons lang 中的方法
	 * 
	 * <pre>
	 *Example:
	 * String[] arr = StringUtils.split(&quot;a b,c d,e-f&quot;, &quot; ,&quot;);
	 * System.out.println(arr.length);//输出6
	 * </pre>
	 * 
	 * @param str
	 *            目标字符串
	 * @param separatorChars
	 *            分隔符字符串
	 * @return 字符串数组
	 */
	public static String[] split(String str, String separatorChars) {
		return StringUtils.split(str, separatorChars);
	}

	/**
	 * 调用指定字段的setter方法
	 * 
	 * <pre>
	 *Example:
	 * User user = new User();
	 * MyUtils.invokeSetMethod(&quot;userName&quot;, user, new Object[] {&quot;张三&quot;});
	 * </pre>
	 * 
	 * @param fieldName
	 *            字段(属性)名称
	 * @param invokeObj
	 *            被调用方法的对象
	 * @param args
	 *            被调用方法的参数数组
	 * @return 成功与否
	 */
	public static boolean invokeSetMethod(String fieldName, Object invokeObj, Object[] args) {
		boolean flag = false;
		Field[] fields = invokeObj.getClass().getDeclaredFields(); // 获得对象实体类中所有定义的字段
		Method[] methods = invokeObj.getClass().getDeclaredMethods(); // 获得对象实体类中所有定义的方法
		for (Field f : fields) {
			String fname = f.getName();
			if (fname.equals(fieldName)) {// 找到要更新的字段名
				String mname = "set" + (fname.substring(0, 1).toUpperCase() + fname.substring(1));// 组建setter方法
				for (Method m : methods) {
					String name = m.getName();
					if (mname.equals(name)) {
						// 处理Integer参数
						if (f.getType().getSimpleName().equalsIgnoreCase("integer") && args.length > 0) {
							args[0] = Integer.valueOf(args[0].toString());
						}
						// 处理Boolean参数
						if (f.getType().getSimpleName().equalsIgnoreCase("boolean") && args.length > 0) {
							args[0] = Boolean.valueOf(args[0].toString());
						}
						try {
							m.invoke(invokeObj, args);
							flag = true;
						} catch (IllegalArgumentException e) {
							flag = false;
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							flag = false;
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							flag = false;
							e.printStackTrace();
						}
					}
				}
			}
		}
		return flag;
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param fileName
	 * @param dir
	 * @return
	 */
	public static boolean isFileExist(String fileName, String dir) {
		File files = new File(dir + fileName);
		return (files.exists()) ? true : false;
	}

	/**
	 * 获得随机文件名,保证在同一个文件夹下不同名
	 * 
	 * @param fileName
	 * @param dir
	 * @return
	 */
	public static String getRandomName(String fileName, String dir) {
		String[] split = fileName.split("\\.");// 将文件名已.的形式拆分
		String extendFile = "." + split[split.length - 1].toLowerCase(); // 获文件的有效后缀

		Random random = new Random();
		int add = random.nextInt(1000000); // 产生随机数10000以内
		String ret = add + extendFile;
		while (isFileExist(ret, dir)) {
			add = random.nextInt(1000000);
			ret = fileName + add + extendFile;
		}
		return ret;
	}

	/**
	 * 创建缩略图
	 * 
	 * @param file
	 *            上传的文件流
	 * @param height
	 *            最小的尺寸
	 * @throws IOException
	 */
	public static void createMiniPic(File file, float width, float height) throws IOException {
		Image src = javax.imageio.ImageIO.read(file); // 构造Image对象
		int old_w = src.getWidth(null); // 得到源图宽
		int old_h = src.getHeight(null);
		int new_w = 0;
		int new_h = 0; // 得到源图长
		float tempdouble;
		if (old_w >= old_h) {
			tempdouble = old_w / width;
		} else {
			tempdouble = old_h / height;
		}

		if (old_w >= width || old_h >= height) { // 如果文件小于锁略图的尺寸则复制即可
			new_w = Math.round(old_w / tempdouble);
			new_h = Math.round(old_h / tempdouble);// 计算新图长宽
			while (new_w > width && new_h > height) {
				if (new_w > width) {
					tempdouble = new_w / width;
					new_w = Math.round(new_w / tempdouble);
					new_h = Math.round(new_h / tempdouble);
				}
				if (new_h > height) {
					tempdouble = new_h / height;
					new_w = Math.round(new_w / tempdouble);
					new_h = Math.round(new_h / tempdouble);
				}
			}
			BufferedImage tag = new BufferedImage(new_w, new_h, BufferedImage.TYPE_INT_RGB);
			tag.getGraphics().drawImage(src, 0, 0, new_w, new_h, null); // 绘制缩小后的图
			FileOutputStream newimage = new FileOutputStream(file); // 输出到文件流
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newimage);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(tag);
			param.setQuality((float) (100 / 100.0), true);// 设置图片质量,100最大,默认70
			encoder.encode(tag, param);
			encoder.encode(tag); // 将JPEG编码
			newimage.close();
		}
	}

	/**
	 * 判断文件类型是否是合法的,就是判断allowTypes中是否包含contentType
	 * 
	 * @param contentType
	 *            文件类型
	 * @param allowTypes
	 *            文件类型列表
	 * @return 是否合法
	 */
	public static boolean isValid(String contentType, String[] allowTypes) {
		if (null == contentType || "".equals(contentType)) {
			return false;
		}
		for (String type : allowTypes) {
			if (contentType.equals(type)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * 多文件压缩
	 * 
	 * <pre>
	 *    Example : 
	 *    ZipOutputStream zosm = new ZipOutputStream(new FileOutputStream(&quot;c:/b.zip&quot;));
	 *    zipFiles(zosm, new File(&quot;c:/com&quot;), &quot;&quot;);
	 *    zosm.close();
	 * </pre>
	 * 
	 * @param zosm
	 * @param file
	 * @param basePath
	 * @throws IOException
	 */
	public static void compressionFiles(ZipOutputStream zosm, File file, String basePath) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			try {
				zosm.putNextEntry(new ZipEntry(basePath + "/"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			basePath = basePath + (basePath.length() == 0 ? "" : "/") + file.getName();
			if(files.length==0){
				try {
					zosm.putNextEntry(new ZipEntry(basePath+ "/"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (File f : files) {
				compressionFiles(zosm, f, basePath);
			}
		} else {
			FileInputStream fism = null;
			BufferedInputStream bism = null;
			try {
				byte[] bytes = new byte[1024];
				fism = new FileInputStream(file);
				bism = new BufferedInputStream(fism, 1024);
				basePath = basePath + (basePath.length() == 0 ? "" : "/") + file.getName();
				zosm.putNextEntry(new ZipEntry(basePath));
				int count;
				while ((count = bism.read(bytes, 0, 1024)) != -1) {
					zosm.write(bytes, 0, count);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (bism != null) {
					try {
						bism.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fism != null) {
					try {
						fism.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 解压缩zip文件
	 * 
	 * @param zipFileName
	 *            压缩文件
	 * @param extPlace
	 *            解压的路径
	 */
	public static boolean decompressionZipFiles(String zipFileName, String extPlace) {
		boolean flag = false;
		try {
			unZip(zipFileName,extPlace);
			flag = true;
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return flag;
//		java.util.zip.ZipInputStream in = null; 
//		java.util.zip.ZipEntry entry = null;
//		FileOutputStream os = null;
//		try {
//			in = new java.util.zip.ZipInputStream(new FileInputStream(zipFileName));
//			while ((entry = in.getNextEntry()) != null) {
//				String entryName = entry.getName();
//				int end = entryName.lastIndexOf("/");
//				String name = "";
//				if (end != -1) {
//					name = entryName.substring(0, end);
//				}
//				File file = new File(extPlace + name);
//				if (!file.exists()) {
//					file.mkdirs();
//				}
//				if (entry.isDirectory()) {
//					in.closeEntry();
//					continue;
//				} else {
//					os = new FileOutputStream(extPlace + entryName);
//					byte[] buf = new byte[1024];
//					int len;
//					while ((len = in.read(buf)) > 0) {
//						os.write(buf, 0, len);
//					}
//					in.closeEntry();
//				}
//			}
//			flag = true;
//		} catch (FileNotFoundException e1) {
//			flag = false;
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			flag = false;
//			e1.printStackTrace();
//		} finally {
//			if (in != null) {
//				try {
//					in.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			if (os != null) {
//				try {
//					os.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}

	/**
	 * 解压缩rar文件
	 * 
	 * @param rarFileName
	 * @param extPlace
	 */
	public static boolean decompressionRarFiles(String rarFileName, String extPlace) {
		boolean flag = false;
		Archive archive = null;
		File out = null;
		File file = null;
		File dir = null;
		FileOutputStream os = null;
		FileHeader fh = null;
		String path, dirPath = "";
		try {
			file = new File(rarFileName);
			archive = new Archive(file);
		} catch (RarException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (file != null) {
				file = null;
			}
		}
		if (archive != null) {
			try {
				fh = archive.nextFileHeader();
				while (fh != null) {
					path = (extPlace + fh.getFileNameString().trim()).replaceAll("\\\\", "/");
					int end = path.lastIndexOf("/");
					if (end != -1) {
						dirPath = path.substring(0, end);
					}
					try {
						dir = new File(dirPath);
						if (!dir.exists()) {
							dir.mkdirs();
						}
					} catch (RuntimeException e1) {
						e1.printStackTrace();
					} finally {
						if (dir != null) {
							dir = null;
						}
					}
					if (fh.isDirectory()) {
						fh = archive.nextFileHeader();
						continue;
					}
					out = new File(extPlace + fh.getFileNameString().trim());
					try {
						os = new FileOutputStream(out);
						archive.extractFile(fh, os);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (RarException e) {
						e.printStackTrace();
					} finally {
						if (os != null) {
							try {
								os.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if (out != null) {
							out = null;
						}
					}
					fh = archive.nextFileHeader();
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
			} finally {
				fh = null;
				if (archive != null) {
					try {
						archive.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			flag = true;
		}
		return flag;
	}

	private static void getDir(String directory, String subDirectory){
	     String dir[];
	     File fileDir = new File(directory);
	     try {
	      if (subDirectory == "" && fileDir.exists() != true)
	       fileDir.mkdir();
	      else if (subDirectory != "") {
	       dir = subDirectory.replace('\\', '/').split("/");
	       for (int i = 0; i < dir.length; i++) {
	        File subFile = new File(directory + File.separator + dir[i]);
	        if (subFile.exists() == false)
	         subFile.mkdir();
	        directory += File.separator + dir[i];
	       }
	      }
	     }catch (Exception ex) {
	       System.out.println(ex.getMessage());
	     }
	 }
	 /**
	  *
	  * @param zipFileNaame      being unzip file including  file name and path ;   
	  * @param outputDirectory    unzip files to this directory
	  *
	  */ 
	 public static  void unZip(String zipFileName, String outputDirectory){
	     try {
	      ZipFile zipFile = new ZipFile(zipFileName,"gbk"); //防止压缩包内文件乱码
	      java.util.Enumeration e = zipFile.getEntries();
	      ZipEntry zipEntry = null;
	      getDir(outputDirectory, "");
	      while (e.hasMoreElements()) {
	       zipEntry = (ZipEntry) e.nextElement();
	       
	       System.out.println("unziping " + zipEntry.getName());
	         if (zipEntry.isDirectory()) {                //如果得到的是个目录，
	          String name = zipEntry.getName();         //  就创建在指定的文件夹下创建目录
	           name = name.substring(0, name.length() - 1);
	          File f = new File(outputDirectory + File.separator + name);
	          f.mkdir();
	          //System.out.println("创建目录：" + outputDirectory + File.separator + name);
	         }
	         else {
	          String fileName = zipEntry.getName();
	          fileName = fileName.replace('\\', '/');
	           //System.out.println("测试文件1：" +fileName);
	          if (fileName.indexOf("/") != -1){
	           getDir(outputDirectory,
	                               fileName.substring(0, fileName.lastIndexOf("/")));
	           //System.out.println("文件的路径："+fileName);
	           fileName=fileName.substring(fileName.lastIndexOf("/")+1,fileName.length());
	              
	          }

	             File f = new File(outputDirectory + File.separator + zipEntry.getName());

	             f.createNewFile();
	             InputStream in = zipFile.getInputStream(zipEntry);
	             FileOutputStream out=new FileOutputStream(f);

	             byte[] by = new byte[1024];
	             int c;
	             while ( (c = in.read(by)) != -1) {
	              out.write(by, 0, c);
	           }
	           out.close();
	           in.close();
	         }
	       }
	     }catch (Exception ex) {
	       System.out.println(ex.getMessage());
	     }
	        
	 }
	 /**
	  * this mothed will unzip all the files  which in your specifeid  folder;
	  * @param filesFolder   
	  * @param outputDirectory       
	  */
	 public static  void unzipFiles(String filesFolder ,String outputDirectory){
	  File zipFolder=new File (filesFolder);
	  String zipFiles [];
	  String zipFileAbs;
	  try{
	   zipFiles=zipFolder.list();
	   for(int i=0;i<zipFiles.length;i++){
	    if(zipFiles[i].length()==(zipFiles[i].lastIndexOf(".zip")+4)){//判断是不是zip包 
	     zipFileAbs=filesFolder+File.separator+zipFiles[i];
	     unZip(zipFileAbs,outputDirectory);
	    }
	   }
	  }catch (SecurityException ex){
	   ex.printStackTrace();
	  }
	    
	 }
	 
	 
	 
	public static void main(String[] ar) throws RarException, IOException {
				
		unzipFiles("D:\\test_old","D:\\test_old");
		
		FileOperateUtil fop=new FileOperateUtil();
		
		File file=new File("E:\\BCKF\\apache-tomcat-6.0.29\\webapps\\JavaWebapp\\upload\\2012-08\\15\\人员信息.txt");
		if(file.exists())
			file.delete();
		else
			System.out.println("not exists");
		
		/** 复制文件*/
		String oldPath="D:\\1.txt";
		String newPath="D:\\2.txt";
		
		if(isFileExist(newPath, "")){
			File tmp=new File(newPath);
			System.out.println(tmp.getName());
			System.out.println(tmp.getParent());
			System.out.println(checkFileName(tmp.getName(),tmp.getParent()));
		}
		
	//	fop.copyFile(oldPath,newPath,true);		
		
	//	fop.copyFolder("D:\\test_old","D:\\test_new",false);
	
	//	fop.moveFolder("D:\\test_old", "D:\\test_new", true);
		
	//	delFiles(new File("D:\\test_old"));
		
		FileOutputStream fos=new FileOutputStream("D:\\test.zip");		
		ZipOutputStream zosm = new ZipOutputStream(fos);
		compressionFiles(zosm, new File("D:\\test_old"), "");		
		zosm.setEncoding("GBK"); //解决压缩中文乱码问题
		zosm.close();
		fos.close();
		
		
		
	}

}
