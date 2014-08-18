package com.orient.lib.xbmc.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.res.AssetManager;

import com.orient.lib.xbmc.XBMC;

public class FileUtils {
	
	/**
	 * Returns an array of files and folders in the given directory path
	 *  
	 * @param path
	 * @return
	 */
	public static String[] list(String path) {
		
		if (XBMC.getInstance().isAndroid()) {
			AssetManager am = XBMC.getInstance().getAndroidContext().getAssets();
			
			try {
				return am.list(path);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		else {
			File dir = new File(path);
			
			return dir.list();
		}
	}
	
	
	/**
	 * Returns an array of folders in the given directory path
	 *  
	 * @param path
	 * @return
	 */
	public static String[] listDirectories(String path) {
		
		if (XBMC.getInstance().isAndroid()) {
			AssetManager am = XBMC.getInstance().getAndroidContext().getAssets();
			
			String[] list;
			
			try {
				list = am.list(path);
			} catch (IOException e) {
				return null;
			}
			
			List<String> dirList = new ArrayList<String>();
			
			for (int i = 0; i<list.length; i++) {
				
				try {
					am.list(path + "/" + list[i]);
				} catch (IOException e) {
					continue;
				}
				
				dirList.add(list[i]);
			}
			
			if (dirList.isEmpty())
				return null;
			
			return dirList.toArray(new String[dirList.size()]);
		}
		else {
			File dir = new File(path);
			
			return dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});
		}
	}
	
	/**
	 * Checks if a given file or folder exists
	 * 
	 * @param path
	 * @return
	 */
	public static boolean exists(String path) {
		
		if (XBMC.getInstance().isAndroid()) {
			AssetManager am = XBMC.getInstance().getAndroidContext().getAssets();
			
			try {
				am.list(path);
			} catch (IOException e) {
				return false;
			}
			
			return true;
		}
		else {
			File file = new File(path);
			return file.exists();
		}
	}
	
	public static String getContents(String path) {
		
		String content = null;
		
		if (XBMC.getInstance().isAndroid()) {
			AssetManager am = XBMC.getInstance().getAndroidContext().getAssets();
			try {
				InputStream contentIn = am.open(path);
				
				StringWriter writer = new StringWriter();
				IOUtils.copy(contentIn, writer);
				content = writer.toString();
			} catch (IOException e) {
				return null;
			}
		}
		else {
			try {
				content = org.apache.commons.io.FileUtils.readFileToString(new File(path));
			} catch (IOException e) {
				return null;
			}
		}
		
		return content;
	}
}
