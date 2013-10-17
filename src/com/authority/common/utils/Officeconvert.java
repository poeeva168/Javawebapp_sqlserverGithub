package com.authority.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;





/**
 * 将Office文档转换为PDF文档 再转成SWF
 * 
 */
public class Officeconvert {
	 Log log = LogFactory.getLog(Officeconvert.class); 
	 private final String CONVERTFILETYPE = "pdf,jpg,jpeg,font,gif,png,wav"; 
	/**
	 * 环境变量下面的url.properties的绝对路径
	 */
	private static final String RUL_PATH = Thread.currentThread()
			.getContextClassLoader().getResource("").getPath()
			.replace("%20", " ")
			+ "config/others/config.properties";

	/**
	 * 将Office文档转换为PDF. 运行该函数需要用到OpenOffice, OpenOffice下载地址为
	 * http://www.openoffice.org/
	 * 
	 * <pre>
	 * 方法示例:
	 * String sourcePath = "F:\\office\\source.doc";
	 * String destFile = "F:\\pdf\\dest.pdf";
	 * Converter.office2PDF(sourcePath, destFile);
	 * </pre>
	 * 
	 * @param sourceFile
	 *            源文件, 绝对路径. 可以是Office2003-2007全部格式的文档, Office2010的没测试. 包括.doc,
	 *            .docx, .xls, .xlsx, .ppt, .pptx等. 示例: F:\\office\\source.doc
	 * @param destFile
	 *            目标文件. 绝对路径. 示例: F:\\pdf\\dest.pdf
	 * @return 操作成功与否的提示信息. 如果返回 -1, 表示找不到源文件, 或url.properties配置错误; 如果返回 0,
	 *         则表示操作成功; 返回1, 则表示转换失败
	 */
	public static int office2PDF(String sourceFile, String destFile) {
		try {
			File inputFile = new File(sourceFile);
			if (!inputFile.exists()) {
				return -1;// 找不到源文件, 则返回-1
			}

			// 如果目标路径不存在, 则新建该路径
			File outputFile = new File(destFile);
			if (!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}

			/*
			 * 从url.properties文件中读取OpenOffice的安装根目录, OpenOffice_HOME对应的键值.
			 * 修改url.properties文件中的 OpenOffice_HOME的键值 OpenOffice 安装目录.
			 * 但是需要注意的是：要用"\\"代替"\",用"\:"代替":" . 如果大家嫌麻烦,
			 * 可以直接给OpenOffice_HOME变量赋值为自己OpenOffice的安装目录
			 */
			Properties prop = new Properties();
			FileInputStream fis = null;
			
			fis = new FileInputStream(RUL_PATH);// 属性文件输入流
			prop.load(fis);// 将属性文件流装载到Properties对象中
			fis.close();// 关闭流

			String OpenOffice_HOME = prop.getProperty("OpenOffice_HOME");
			if (OpenOffice_HOME == null)
				return -1;
			// 如果从文件中读取的URL地址最后一个字符不是 '\'，则添加'\'
			if (OpenOffice_HOME.charAt(OpenOffice_HOME.length() - 1) != '\\') {
				OpenOffice_HOME += "\\";
			}
			// 启动OpenOffice的服务
			String command = OpenOffice_HOME
					+ "program\\soffice.exe -headless -accept=\"socket,host=127.0.0.1,port=8100;urp;\" -nofirststartwizard";
			Process pro = Runtime.getRuntime().exec(command);
			// connect to an OpenOffice.org instance running on port 8100
			OpenOfficeConnection connection = new SocketOpenOfficeConnection(
					"127.0.0.1", 8100);
			connection.connect();

			// convert
			DocumentConverter converter = new OpenOfficeDocumentConverter(
					connection);
			converter.convert(inputFile, outputFile);

			// close the connection
			connection.disconnect();
			// 关闭OpenOffice服务的进程
			pro.destroy();

			return 0;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (ConnectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 1;
	}
	
	/**
	 * 将PDF文档转换为swf格式的FLASH文件. 运行该函数需要用到SWFTools, 下载地址为
	 * http://www.swftools.org/download.html
	 * 
	 * <pre>
	 * 示例:
	 * String sourcePath = "F:\\PDF\\source.pdf";
	 * String destFile = "F:\\SWF\\dest.swf";
	 * try {
	 * 	Converter.pdf2SWF(sourcePath, destFile);
	 * } catch (IOException e) {
	 * 	e.printStackTrace();
	 * }
	 * </pre>
	 * 
	 * @param sourceFile
	 *            源文件(即PDF文档)路径, 包括源文件的文件名. 示例: D:\\PDF\\source.pdf
	 * @param destFile
	 *            目标文件路径, 即需要保存的文件路径(包括文件名). 示例: D:\\SWF\\dest.swf
	 * @return 操作成功与否的提示信息. 如果返回 -1, 表示找不到源PDF文件, 或配置文件url.properties配置错误; 如果返回
	 *         0, 则表示操作成功; 返回1或其他, 则表示转换失败
	 */
	public static int pdf2SWF(String sourceFile, String destFile) {

		// 目标路径不存在则建立目标路径
		File dest = new File(destFile);
		if (!dest.getParentFile().exists())
			dest.getParentFile().mkdirs();
		if(dest.exists()){
			dest.delete();
		}
		// 源文件不存在则返回 -1
		File source = new File(sourceFile);
		if (!source.exists())
			return -1;

		// 从url.properties文件中读取SWFTools的安装根目录, SWFTools_HOME对应的键值
		Properties prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(RUL_PATH);// 属性文件输入流
			prop.load(fis);// 将属性文件流装载到Properties对象中
			fis.close();// 关闭流
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}

		String t = prop.getProperty("SWFTools_HOME");
		if(t == null)
			return -1;
		// 如果从文件中读取的URL地址最后一个字符不是 '\'，则添加'\'
		if (t.charAt(t.length() - 1) != '\\') {
			t += "\\";
		}
		
		try {
			// 调用pdf2swf命令进行转换swfextract -i - sourceFilePath.pdf -o destFilePath.swf
			String command = t + "pdf2swf.exe  -i -T 9 " + sourceFile + " -o "
					+ destFile;
			Process pro = Runtime.getRuntime().exec(command);

			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(pro.getInputStream()));
			while (bufferedReader.readLine() != null) {

			}
			pro.waitFor();
			return pro.exitValue();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return 1;
	}
	
	
	/**
     * 把文件转化为swf格式支持"pdf,jpg,jpeg,font,gif,png,wav"
     * 
     * @param sourceFilePath
     *            要进行转化为swf文件的地址
     * @param swfFilePath
     *            转化后的swf的文件地址
     * @return
     */ 
    public  boolean convertFileToSwf(String sourceFilePath, String swfFilePath) { 
        log.info("开始转化文件到swf格式"); 
     // 从url.properties文件中读取SWFTools的安装根目录, SWFTools_HOME对应的键值
     		Properties prop = new Properties();
     		FileInputStream fis = null;
     		try {
     			fis = new FileInputStream(RUL_PATH);// 属性文件输入流
     			prop.load(fis);// 将属性文件流装载到Properties对象中
     			fis.close();// 关闭流
     		} catch (FileNotFoundException e) {
     			e.printStackTrace();
     			return false;
     		} catch (IOException e) {
     			e.printStackTrace();
     			return false;
     		}

     		String swftoolsPath = prop.getProperty("SWFTools_HOME");
        if (swftoolsPath == null || swftoolsPath == "") { 
            if (log.isWarnEnabled()) { 
                log.warn("未指定要进行swf转化工具的地址！！！"); 
            } 
            return false; 
        } 
        String filetype = sourceFilePath.substring(sourceFilePath 
                .lastIndexOf(".") + 1); 
        // 判读上传文件类型是否符合转换为pdf 
        log.info("判断文件类型通过"); 
        if (CONVERTFILETYPE.indexOf(filetype.toLowerCase()) == -1) { 
            if (log.isWarnEnabled()) { 
                log.warn("当前文件不符合要转化为SWF的文件类型！！！"); 
            } 
            return false; 
        } 
        File sourceFile = new File(sourceFilePath); 
 
        if (!sourceFile.exists()) { 
            if (log.isWarnEnabled()) { 
                log.warn("要进行swf的文件不存在！！！"); 
            } 
            return false; 
        } 
        log.info("准备转换的文件路径存在"); 
        if (!swftoolsPath.endsWith(File.separator)) { 
            swftoolsPath += File.separator; 
        } 
        StringBuilder commandBuidler = new StringBuilder(swftoolsPath); 
        File swfFile = new File(swfFilePath); 
        if (!swfFile.getParentFile().exists()) { 
            swfFile.getParentFile().mkdirs(); 
        } 
        if (filetype.toLowerCase().equals("jpg")) { 
            filetype = "jpeg"; 
        } 
        List<String>  command = new   ArrayList<String>();   
            command.add(swftoolsPath+"\\"+filetype.toLowerCase()+"2swf.exe");//从配置文件里读取     
            command.add("-z");     
            command.add("-s");     
            command.add("flashversion=9");     
            command.add("-s");     
            command.add("poly2bitmap");//加入poly2bitmap的目的是为了防止出现大文件或图形过多的文件转换时的出错，没有生成swf文件的异常     
            command.add(sourceFilePath);     
            command.add("-o");     
            command.add(swfFilePath);     
        try { 
            ProcessBuilder processBuilder = new ProcessBuilder();     
            processBuilder.command(command);     
            Process process = processBuilder.start();     
            log.info("开始生成swf文件.."); 
            dealWith(process); 
            try {     
                process.waitFor();//等待子进程的结束，子进程就是系统调用文件转换这个新进程     
            } catch (InterruptedException e) {     
                e.printStackTrace();     
            }     
            File swf = new File(swfFilePath); 
            if (!swf.exists()) { 
                return false; 
            } 
            log.info("转化SWF文件成功!!!"); 
        } catch (IOException e) { 
            // TODO Auto-generated catch block 
            log.error("转化为SWF文件失败!!!"); 
            e.printStackTrace(); 
            return false; 
        } 
 
        return true; 
    } 
    
    private void dealWith(final Process pro){     
        // 下面是处理堵塞的情况     
        try {     
            new Thread(){     
                public void run(){     
                    BufferedReader br1 = new BufferedReader(new InputStreamReader(pro.getInputStream()));     
                    String text;     
                    try {     
                        while ( (text = br1.readLine()) != null) {     
                            System.out.println(text);     
                        }     
                    } catch (IOException e) {     
                        e.printStackTrace();     
                    }     
                }     
            }.start();     
        } catch (Exception e) {     
            e.printStackTrace();     
        }     
             
        try {     
            new Thread(){     
                public void run(){     
                    BufferedReader br2 = new BufferedReader(new InputStreamReader(pro.getErrorStream()));//这定不要忘记处理出理时产生的信息，不然会堵塞不前的     
                    String text;     
                    try {     
                        while( (text = br2.readLine()) != null){     
                            System.err.println(text);     
                        }     
                    } catch (IOException e) {     
                        e.printStackTrace();     
                    }     
                }     
            }.start();     
        } catch (Exception e) {     
            e.printStackTrace();     
        }     
    }     
	
	public static void main(String[] args){
		
		String sourceFile="D:\\1.docx";
		String pdfdestFile = "D:\\1.pdf";
		String swfdestFile ="D:\\test.swf";
		Officeconvert officeconvert = new Officeconvert();
		officeconvert.office2PDF(sourceFile, pdfdestFile);
		officeconvert.convertFileToSwf(pdfdestFile, swfdestFile); 
		//Officeconvert.png2swf(sourceFile, destFile);
		
		
	}
}