package test.controls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

/**
 * HTTP请求对象
 * 
 * @author YYmmiinngg
 */
public class HttpRequester
{
	private String defaultContentEncoding;

	public HttpRequester()
	{
		this.defaultContentEncoding = Charset.defaultCharset().name();
		isMultipart = false;
	}

	public HttpRequester(boolean multipart)
	{
		this.defaultContentEncoding = Charset.defaultCharset().name();
		isMultipart = multipart;
	}

	private boolean isMultipart = false;

	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpRespons sendGet(String urlString) throws IOException
	{
		return this.send(urlString, "GET", null, null);
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpRespons sendGet(String urlString, Map<String, Object> params)
			throws IOException
	{
		return this.send(urlString, "GET", params, null);
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @param propertys
	 *            请求属性
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpRespons sendGet(String urlString, Map<String, Object> params,
			Map<String, String> propertys) throws IOException
	{
		return this.send(urlString, "GET", params, propertys);
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpRespons sendPost(String urlString) throws IOException
	{
		return this.send(urlString, "POST", null, null);
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpRespons sendPost(String urlString, Map<String, Object> params)
			throws IOException
	{
		return this.send(urlString, "POST", params, null);
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString
	 *            URL地址
	 * @param params
	 *            参数集合 
	 * @param propertys
	 *            请求属性
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpRespons sendPost(String urlString, Map<String, Object> params,
			Map<String, String> propertys) throws IOException
	{
		return this.send(urlString, "POST", params, propertys);
	}

	/**
	 * 发送HTTP请求
	 * 
	 * @param urlString
	 * @return 响映对象
	 * @throws IOException
	 */
	private HttpRespons send(String urlString, String method,
			Map<String, Object> parameters, Map<String, String> propertys)
			throws IOException
	{

		if (method.equalsIgnoreCase("GET") && parameters != null)
		{
			StringBuffer param = new StringBuffer();
			int i = 0;
			for (String key : parameters.keySet())
			{
				if (i == 0)
					param.append("?");
				else
					param.append("&");
				param.append(key).append("=").append(parameters.get(key));
				i++;
			}
			urlString += param;
		}
		URL url = new URL(urlString);
		connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod(method);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);

		if (propertys != null)
			for (String key : propertys.keySet())
			{
				connection.addRequestProperty(key, propertys.get(key));
			}

		if (!isMultipart)
		{
			if (method.equalsIgnoreCase("POST") && parameters != null)
			{
				StringBuffer param = new StringBuffer();
				for (String key : parameters.keySet())
				{
					param.append("&");
					String val="";
					if(parameters.get(key)!=null)
					{
						val=parameters.get(key).toString();
					}
					param.append(key).append("=").append(val);
				}
				connection.getOutputStream().write(param.toString().getBytes());
				connection.getOutputStream().flush();
				connection.getOutputStream().close();
			}
		} else
		{

			connection.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);
			
			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestProperty("Accept-Language", "zh-cn");
			
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");
			
			
			connection.setAllowUserInteraction(true);
			
			for (String key : parameters.keySet())
			{
				Object obj=parameters.get(key);
				if(obj==null)
				{
					this.setParameter(key, "");
					continue;
				}
				if(obj instanceof File)
				{
					File f=(File) obj;
					if(f.exists() && f.isFile())
					{
						this.setParameter(key, f);
					}
					else
					{
						this.setParameter(key, f.getAbsolutePath());
					}
				}
				else
				{
					this.setParameter(key, obj.toString());
				}
			}
			boundary();
			writeln("--");
			os.close();
			//return connection.getInputStream();

		}

		return this.makeContent(urlString);
	}

	OutputStream os = null;
	StringBuffer stringBuffer = new StringBuffer("");

	protected void connect() throws IOException
	{
		if (os == null)
			os = connection.getOutputStream();
	}

	protected void write(char c) throws IOException
	{
		stringBuffer.append(c);
		connect();
		os.write(c);
		os.flush();
	}

	protected void write(String s) throws IOException
	{
		stringBuffer.append(s);
		connect();
		os.write(s.getBytes());
		os.flush();
	}

	protected void newline() throws IOException
	{
		stringBuffer.append("\r\n");
		connect();
		write("\r\n");
	}

	protected void writeln(String s) throws IOException
	{
		connect();
		write(s);
		newline();
	}

	private void writeName(String name) throws IOException
	{
		newline();
		write("Content-Disposition: form-data; name=\"");
		write(name);
		write('"');
	}

	private void setParameter(String name, String value) throws IOException
	{
		boundary();
		writeName(name);
		newline();
		newline();
		writeln(value);
	}

	private void boundary() throws IOException
	{
		write("--");
		write(boundary);
	}

	private void setParameter(String name, String filename, InputStream is)
			throws IOException
	{
		boundary();
		writeName(name);
		write("; filename=\"");
		write(filename);
		write('"');
		newline();
		write("Content-Type: ");
		String type = connection.guessContentTypeFromName(filename);
		if (type == null)
			type = "application/octet-stream";
		writeln(type);
		newline();
		pipe(is, os);
		newline();
	}

	private static void pipe(InputStream in, OutputStream out)
			throws IOException
	{
		byte[] buf = new byte[500000];
		int nread;
		int navailable;
		int total = 0;
		synchronized (in)
		{
			while ((nread = in.read(buf, 0, buf.length)) >= 0)
			{
				out.write(buf, 0, nread);
				total += nread;
			}
		}
		out.flush();
		buf = null;
	}

	/**
	 * adds a file parameter to the request
	 * 
	 * @param name
	 *            parameter name
	 * @param file
	 *            the file to upload
	 * @throws IOException
	 */
	private void setParameter(String name, File file) throws IOException
	{
		setParameter(name, file.getPath(), new FileInputStream(file));
	}

	private static Random random = new Random();

	protected static String randomString()
	{
		return Long.toString(random.nextLong(), 36);
	}

	String boundary = "---------------------------" + randomString()
			+ randomString() + randomString();
	private HttpURLConnection connection;

	/**
	 * 得到响应对象
	 * 
	 * @param connection
	 * @return 响应对象
	 * @throws IOException
	 */
	private HttpRespons makeContent(String urlString) throws IOException
	{
		HttpRespons httpResponser = new HttpRespons();
		try
		{
			InputStream in = connection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(in));
			httpResponser.contentCollection = new Vector<String>();
			StringBuffer temp = new StringBuffer();
			String line = bufferedReader.readLine();
			while (line != null)
			{
				httpResponser.contentCollection.add(line);
				temp.append(line).append("\r\n");
				line = bufferedReader.readLine();
			}
			bufferedReader.close();

			String ecod = connection.getContentEncoding();
			if (ecod == null)
				ecod = this.defaultContentEncoding;

			httpResponser.urlString = urlString;

			httpResponser.defaultPort = connection.getURL().getDefaultPort();
			httpResponser.file = connection.getURL().getFile();
			httpResponser.host = connection.getURL().getHost();
			httpResponser.path = connection.getURL().getPath();
			httpResponser.port = connection.getURL().getPort();
			httpResponser.protocol = connection.getURL().getProtocol();
			httpResponser.query = connection.getURL().getQuery();
			httpResponser.ref = connection.getURL().getRef();
			httpResponser.userInfo = connection.getURL().getUserInfo();

			httpResponser.content = new String(temp.toString().getBytes(), ecod);
			httpResponser.contentEncoding = ecod;
			httpResponser.code = connection.getResponseCode();
			httpResponser.message = connection.getResponseMessage();
			httpResponser.contentType = connection.getContentType();
			httpResponser.method = connection.getRequestMethod();
			httpResponser.connectTimeout = connection.getConnectTimeout();
			httpResponser.readTimeout = connection.getReadTimeout();

			return httpResponser;
		} catch (IOException e)
		{
			throw e;
		} finally
		{
			if (connection != null)
				connection.disconnect();
		}
	}

	/**
	 * 默认的响应字符集
	 */
	public String getDefaultContentEncoding()
	{
		return this.defaultContentEncoding;
	}

	/**
	 * 设置默认的响应字符集
	 */
	public void setDefaultContentEncoding(String defaultContentEncoding)
	{
		this.defaultContentEncoding = defaultContentEncoding;
	}

}