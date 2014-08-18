package com.orient.app.epgsample;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orient.lib.xbmc.XBMC;
import com.orient.lib.xbmc.addons.ADDON_TYPE;
import com.orient.lib.xbmc.addons.AddonManager;
import com.orient.lib.xbmc.addons.Scraper;

public class ScraperListActivity extends ActionBarActivity {

	 ListView listView ;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scraper_list);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		XBMC.getInstance().setAndroidContext(this);
		
		ArrayList<Scraper> scrapers = AddonManager.GetScrapers(ADDON_TYPE.ADDON_SCRAPER_EPG);
		
		ArrayList<String> values = new ArrayList<String>();
		
		for(Scraper s : scrapers) {
			values.add(s.getName());
		}
		
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	              android.R.layout.simple_list_item_1, android.R.id.text1, values);
		
		listView = (ListView) findViewById(R.id.scraperList);
		listView.setAdapter(adapter); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scraper_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_scraper_list,
					container, false);
			return rootView;
		}
	}

}
