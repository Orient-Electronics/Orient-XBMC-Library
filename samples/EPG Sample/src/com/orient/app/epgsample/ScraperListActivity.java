package com.orient.app.epgsample;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.orient.lib.xbmc.XBMC;
import com.orient.lib.xbmc.addons.ADDON_TYPE;
import com.orient.lib.xbmc.addons.AddonManager;
import com.orient.lib.xbmc.addons.Scraper;

public class ScraperListActivity extends ActionBarActivity {

	Context context;
	ListView listView ;
	RelativeLayout loadingState;
	ArrayList<Scraper> scrapers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scraper_list);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}

		setTitle("Scrapers");

		context = this;
		listView = (ListView) findViewById(R.id.scraperList);
		loadingState = (RelativeLayout)findViewById(R.id.loadingState);
		
		setLoadingState();

		new EpgTask().execute("");
	}


	private class EpgTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... scraper_id) {
			XBMC.getInstance().setAndroidContext(context);

			scrapers = AddonManager.GetScrapers(ADDON_TYPE.ADDON_SCRAPER_EPG);

			return null;
		}
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {

			setAdapter();
		}
	}

	private void setAdapter() {
		
		setListViewState();
		
		ArrayList<String> values = new ArrayList<String>();

		for(Scraper s : scrapers) {
			values.add(s.getName());
		}


		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, values);

		listView.setAdapter(adapter); 


		// ListView Item Click Listener
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// ListView Clicked item index
				int itemPosition = position;

				Intent myIntent = new Intent(ScraperListActivity.this, ChannelListActivity.class);
				myIntent.putExtra("scraper_id", scrapers.get(itemPosition).getId());
				myIntent.putExtra("scraper_name", scrapers.get(itemPosition).getName());
				startActivity(myIntent);
			}

		}); 

		//		adapter = new ChannelAdapter(ChannelListActivity.this,
		//				R.layout.channel_list_item, channels);
		//
		//		listView.setAdapter(adapter);
		//		lv.setOnItemClickListener(this);
	}

	private void setLoadingState() {
		loadingState.setVisibility(View.VISIBLE);
		listView.setVisibility(View.GONE);
	}
	
	private void setListViewState() {
		loadingState.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);
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
