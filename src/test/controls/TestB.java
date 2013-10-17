package test.controls;

public class TestB {	
	private TestA obj;
	public TestA getObj() {
		return obj;
	}
	public void setObj(TestA obj) {
		this.obj = obj;
	}	
	public static void main(String[] args) {		
		TestA a=new TestA();		
		a.setName("test");		
		TestB b=new TestB();
		b.setObj(a);
		
		String s=b.getObj().getName();		
	}	
}
