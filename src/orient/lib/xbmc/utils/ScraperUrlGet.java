package orient.lib.xbmc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import orient.lib.xbmc.Settings;

public class ScraperUrlGet {

//	private String CACHE_FILE_NAME = "response.txt";
//	private String CACHE_CONTEXT;
	private File cacheDir;
	private UrlEntry scrURL;

	public ScraperUrlGet(UrlEntry scrURL, String cacheContext) {
		this.scrURL = scrURL;
//		CACHE_FILE_NAME = scrURL.m_cache;
//		CACHE_CONTEXT = cacheContext;

		Settings settings = Settings.getInstance();
		cacheDir = settings.getCacheDir();
	}

	
	public String get() {

		enableHttpResponseCache();
		
		String result;
		try {
			result = run(scrURL);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		return result;
	}
	 
	private String run(UrlEntry scrURL) throws IOException{
		InputStream is = null;
	        
	    try {
	        URL url = new URL(scrURL.url);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        
	        if (scrURL.isPost)
	        	conn.setRequestMethod("POST");
	        else
	        	conn.setRequestMethod("GET");
	        
	        if (scrURL.spoof != null)
	        	conn.addRequestProperty("Referer", scrURL.spoof);
	        
	        if (scrURL.isGZip)
	        	conn.addRequestProperty("Accept-Encoding", "gzip");
	        
	        conn.setDoInput(true);
	        // Starts the query
	        conn.connect();
	        
	        
//	        int response = conn.getResponseCode();
	        is = conn.getInputStream();

	        // Convert the InputStream into a string
	        String contentAsString = processHttpResponse(is, conn.getContentEncoding());
	        return contentAsString;
	        
		} finally {
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
	        if (is != null) {
	            is.close();
	        } 
	    }
	}
	
	private String processHttpResponse(InputStream instream, String contentEncoding)
			throws IllegalStateException, IOException {

		String responseStr;

		// GZIP
		if (contentEncoding != null
				&& contentEncoding.equalsIgnoreCase("gzip")) {

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(instream)));

			StringBuilder sb = new StringBuilder();
			String inputLine;

			while ((inputLine = rd.readLine()) != null) {
				sb.append(inputLine);
			}

			responseStr = sb.toString();
		} else {

			StringWriter writer = new StringWriter();
		    IOUtils.copy(instream, writer, "UTF-8");
		    responseStr = writer.toString();
		}

		return responseStr;
	}
	
	public void SetCacheDir(File dir) {
		cacheDir = dir;
	}

	public File getCacheDir() {
		return cacheDir;
	}
	
	// TODO move this to app level
	private void enableHttpResponseCache() {
		try {
			long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
			File httpCacheDir = new File(getCacheDir(), "http");
			Class.forName("android.net.http.HttpResponseCache")
			.getMethod("install", File.class, long.class)
			.invoke(null, httpCacheDir, httpCacheSize);
		} catch (Exception httpResponseCacheNotAvailable) {
//			Log.d(TAG, "HTTP response cache is unavailable.");
		}
	}
}
