package test.controls;

import java.rmi.RemoteException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import com.authority.common.jackjson.JackJson;


public class AxisWebservice {
	/**
     * <b>function:</b>jws axis WebService客户端
     * @author hoojo
     * @createDate 2010-12-15 下午05:10:28
     * @param args
     * @throws ServiceException 
     * @throws RemoteException 
     */
    public static void main(String[] args) throws ServiceException, RemoteException {
        //webService访问地址
        //String url = "http://localhost:8080/axis/HelloWorldService.jws";
        String url = "http://60.191.149.146/AppServices/services/SimpleWS";
        //创建服务
        Service service = new Service();
        //创建调用句柄
        Call call = (Call) service.createCall();
        //设置请求地址
        call.setTargetEndpointAddress(url);
        /**
         * 设置调用的方法和方法的命名空间；
         * 因为这里是手动发布到webroot目录下的，所以命名空间和请求地址一致
         * 当然null也可以，因为本身它就没有设置命名空间，一般方法的命名空间是
         * 包名倒写组成，如com.hoo.service,ns=http://service.hoo.com
         */
        call.setOperationName(new QName(null, "process"));
        
        /**
         * 用call调用sayHello方法，设置请求的参数，返回的就是返回值了
         */
       // {"deviceid":"111111111","customersid":"","PAGE_PAGESTART":"0","PAGE_PAGERECORDNUM":"10","SORT_ORDERBYFIELD":"b"}
        
        JackJson json = new JackJson();
        RequestBody rb = new RequestBody();
        rb.setDeviceid("111111111");
        rb.setCustomersid("");
        rb.setPAGE_PAGESTART("0");
        rb.setPAGE_PAGERECORDNUM("10");
        rb.setSORT_ORDERBYFIELD("b");
        
    //  call.addParameter("arg0", org.apache.axis.encoding.XMLType.XSD_BASE64, javax.xml.rpc.ParameterMode.IN);
    //  call.setReturnType(org.apache.axis.encoding.XMLType.XSD_BASE64);
        
        @SuppressWarnings("static-access")
		String ywbw = json.fromObjectToJson(rb);
        
        System.out.println("业务报文Json 格式："+ywbw);
        
        Object[] arg0 = new Object[]{ ywbw.getBytes()};
        Object obj = call.invoke(arg0);
        
        String result = (String) obj;
        System.out.println(result);
    }
}
