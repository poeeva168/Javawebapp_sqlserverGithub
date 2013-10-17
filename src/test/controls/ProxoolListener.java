package test.controls;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;

/**
 * @author wangtao
 */

public class ProxoolListener  extends HttpServlet implements ServletContextListener 
{
	private static final Log LOG = LogFactory.getLog(ProxoolListener.class);
    
	private static final String XML_FILE_PROPERTY = "xmlFile";

    private static final String PROPERTY_FILE_PROPERTY = "contextConfigLocation";

    private static final String AUTO_SHUTDOWN_PROPERTY = "autoShutdown";
    
    @SuppressWarnings("unused")
	private boolean autoShutdown = true;
	
    public void contextDestroyed(ServletContextEvent arg0)
	{
    	if (autoShutdown) {
            ProxoolFacade.shutdown(0);
        }
		System.out.println("destroy database pool....");
	}

	public void contextInitialized(ServletContextEvent contextEvent)
	{
		ServletContext context = contextEvent.getServletContext(); 
		String appDir = contextEvent.getServletContext().getRealPath("/");
        Properties properties = new Properties();

        Enumeration names = context.getInitParameterNames();
        
        
       // System.out.println("PROPERTY_FILE_PROPERTY:"+PROPERTY_FILE_PROPERTY);
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = context.getInitParameter(name);
            //System.out.println("name:"+name);
            
            if (name.equals(XML_FILE_PROPERTY)) {
                try {
                    File file = new File(value);
                    if (file.isAbsolute()) {
                        JAXPConfigurator.configure(value, false);
                    } else {
                        JAXPConfigurator.configure(appDir + File.separator + value, false);
                    }
                } catch (ProxoolException e) {
                    LOG.error("Problem configuring " + value, e);
                }
            } else if (name.equals(PROPERTY_FILE_PROPERTY)) {
                try {
                //	System.out.println("PROPERTY_FILE_PROPERTY");
                    File file = new File(value);
                    if (file.isAbsolute()) {
                        PropertyConfigurator.configure(value);
                    } else {
                        PropertyConfigurator.configure(appDir + File.separator + value);
                    }
                } catch (ProxoolException e) {
                    LOG.error("Problem configuring " + value, e);
                }
            } else if (name.equals(AUTO_SHUTDOWN_PROPERTY)) {
                autoShutdown = Boolean.valueOf(value).booleanValue();
            } else if (name.startsWith("jdbc")) { 
                properties.setProperty(name, value);
            }
        }

        if (properties.size() > 0) {
            try {
                PropertyConfigurator.configure(properties);
            } catch (ProxoolException e) {
                LOG.error("Problem configuring using init properties", e);
            }
        }
	}

}
