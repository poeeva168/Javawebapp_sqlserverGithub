package test.controls;

import com.authority.web.listener.*;


public class TestFileMonitor {

	public static void main(String args[]){
		FileObserver ob = new FileObserver("D:\\test");
		FileListener listener = new FileListener();
		ob.addListener(listener);
		FileMonitor monitor = new FileMonitor(ob);
		monitor.start();
	}
}


