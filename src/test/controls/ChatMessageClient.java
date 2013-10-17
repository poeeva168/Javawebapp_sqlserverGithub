package test.controls;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javax.servlet.ServletContext;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.ServerContext;
import org.directwebremoting.ServerContextFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.ServletContextAware;

import uk.ltd.getahead.dwr.WebContext;
import uk.ltd.getahead.dwr.WebContextFactory;
 
/**
 * <b>function:</b>监听客户端事件，想客户端推出消息
 * @author hoojo
 * @createDate 2011-6-7 上午11:33:08
 * @file SendMessageClient.java
 * @package com.hoo.util
 * @project DWRComet
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class ChatMessageClient implements ApplicationListener, ServletContextAware {
    
    private ServletContext ctx;
    public void setServletContext(ServletContext ctx) {
        this.ctx = ctx;
    }
    
    @SuppressWarnings("deprecation")
    public void onApplicationEvent(ApplicationEvent event) {
        //如果事件类型是ChatMessageEvent就执行下面操作
        if (event instanceof ChatMessageEvent) {
            Message msg = (Message) event.getSource();
            ServerContext context = ServerContextFactory.get();           
            //获得客户端所有chat页面script session连接数
            Collection<ScriptSession> sessions = context.getAllScriptSessions();
            for (ScriptSession session : sessions) {
                ScriptBuffer sb = new ScriptBuffer();
                Date time = msg.getTime();
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                      
                /*String s = time.getYear() + "-" + (time.getMonth() + 1) + "-" +  time.getDate() + " " 
                        +  time.getHours() + ":" + time.getMinutes() + ":" + time.getSeconds();*/
                //执行setMessage方法
                String s=sdf.format(time);
                sb.appendScript("index.showMessage({msg: '")
                .appendScript(msg.getMsg())
                .appendScript("', time: '")
                .appendScript(s)
                .appendScript("'})");
                
                //执行客户端script session方法，相当于浏览器执行JavaScript代码
                  //上面就会执行客户端浏览器中的showMessage方法，并且传递一个对象过去
 
                session.addScript(sb);
            }
        }
    }
}