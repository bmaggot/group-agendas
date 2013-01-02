package com.groupagendas.groupagenda.https;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.groupagendas.groupagenda.data.Data;

public class WebService {
	private ClientConnectionManager clientConnectionManager;
	private Context appContext;
	private HttpContext context;
	private HttpParams params;
	private HttpClient client;
	
	@Deprecated
	public WebService(){
		setup();
	}
	
	public WebService(Context context){
		setup();
		this.appContext = context;
	}
	
	private void setup(){
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
		
		params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		//set the user credentials for our site "example.com"
		credentialsProvider.setCredentials(new AuthScope(Data.getServerUrl(), AuthScope.ANY_PORT),
				new UsernamePasswordCredentials("UserNameHere", "UserPasswordHere"));
		clientConnectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);

		context = new BasicHttpContext();
		context.setAttribute("http.auth.credentials-provider", credentialsProvider);
	}
	
	public HttpResponse getResponseFromHttpPost(HttpPost httpPost) throws ClientProtocolException, IOException{
		httpPost.setHeader("User-Agent", "Linux; GroupAgendas version: " + getApplicationVersion() + "; AndroidPhone " + android.os.Build.VERSION.RELEASE);
		httpPost.setHeader("Accept", "*/*");
		client = new DefaultHttpClient(clientConnectionManager, params);
		HttpResponse response = client.execute(httpPost, context);
		return response;
	}
	
	public HttpResponse getResponseFromHttpGet(HttpGet get) throws ClientProtocolException, IOException{
		client = new DefaultHttpClient(clientConnectionManager, params);
		HttpResponse response = client.execute(get, context);
		return response;
	}
	
	public HttpResponse getResponseFromUrl(String url) throws ClientProtocolException, IOException{
		client = new DefaultHttpClient(clientConnectionManager, params);
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get, context);
		return response;
	}
	
	public String getApplicationVersion() {
		PackageManager pacMan;
		PackageInfo info;
		String version = "";
		
		try {
			pacMan = appContext.getPackageManager();
			info = pacMan.getPackageInfo(appContext.getPackageName(), 0);
			version = info.versionName;
		} catch (Exception e) {
			Log.e("WebService.getApplicationVersion()", e.getMessage());
		}
		
		return version;
	}
}
