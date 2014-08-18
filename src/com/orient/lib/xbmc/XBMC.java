package com.orient.lib.xbmc;

import com.orient.lib.xbmc.addons.AddonManager;

import android.content.Context;

public class XBMC {
	
	private static XBMC instance = null;
	private Context androidContext;

	public static XBMC getInstance() {
		if (instance == null) {
			instance = new XBMC();
		}
		return instance;
	}

	public XBMC() {
		// Exists only to defeat instantiation.
	}
	
	public Settings getSettings() {
		return Settings.getInstance();
	}

	public Context getAndroidContext() {
		return androidContext;
	}

	public void setAndroidContext(Context androidContext) {
		
		if (androidContext != null)
			;
		
		this.androidContext = androidContext;
	}
	
	public boolean isAndroid() {
		return androidContext != null;
	}
	
	public AddonManager getAddonManager() {
		return AddonManager.getInstance();
	}
}
