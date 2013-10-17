package com.authority.web.listener;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;

public class FileListener extends FileAlterationListenerAdaptor{

	@Override
	public void onDirectoryChange(File directory) {
		System.out.println("文件目录变更了:"+directory.getAbsolutePath());
	}

	@Override
	public void onDirectoryCreate(File directory) {
		System.out.println("文件目录创建了:"+directory.getAbsolutePath());
	}

	@Override
	public void onDirectoryDelete(File directory) {
		System.out.println("文件目录删除了:"+directory.getAbsolutePath());
	}

	@Override
	public void onFileChange(File file) {
		System.out.println("文件变更了:"+file.getAbsolutePath());
		if(file.getParent().endsWith("spring")){
			System.out.println("-----重新加载xml 文件-----开始");
			ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:config/spring/spring-common.xml");
			ConfigurableApplicationContext appContext = new FileSystemXmlApplicationContext(applicationContext);
			appContext.refresh(); 
			System.out.println("-----重新加载xml 文件-----结束");			
		}
	}

	@Override
	public void onFileCreate(File file) {
		System.out.println("文件创建了:"+file.getAbsolutePath());
	}

	@Override
	public void onFileDelete(File file) {
		System.out.println("文件删除了:"+file.getAbsolutePath());
	}

	@Override
	public void onStart(FileAlterationObserver observer) {
		System.out.println("开始监听:"+observer.getDirectory());
	}

	@Override
	public void onStop(FileAlterationObserver observer) {
		System.out.println("停止监听:"+observer.getDirectory());
	}

}


