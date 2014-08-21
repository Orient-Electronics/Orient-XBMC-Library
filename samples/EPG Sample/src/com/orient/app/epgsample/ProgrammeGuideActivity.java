package com.orient.app.epgsample;

import java.util.ArrayList;

import com.orient.lib.xbmc.XBMC;
import com.orient.lib.xbmc.addons.Scraper;
import com.orient.lib.xbmc.addons.ScraperError;
import com.orient.lib.xbmc.epg.Epg;
import com.orient.lib.xbmc.epg.ProgrammeInfoTag;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.os.Build;

public class ProgrammeGuideActivity extends ActionBarActivity {

	private ProgrammeGuideActivity context;
	private ListView listView;
	private LinearLayout networkErrorState;
	private RelativeLayout loadingState;
	ArrayList<ProgrammeInfoTag> programmeList;
	private Epg epg;
	private ProgrammeAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_programme_guide);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		
		////////Getting Intent
		Intent myIntent = getIntent(); // gets the previously created intent
		String scraper_id = myIntent.getStringExtra("scraper_id");
		String channel_id = myIntent.getStringExtra("channel_id");
		String channel_name = myIntent.getStringExtra("channel_name");

		//////// Setting variables
		context = this;
		listView = (ListView) findViewById(R.id.programmeList);
		networkErrorState = (LinearLayout)findViewById(R.id.programeGuideNetworkErrorState);
		loadingState = (RelativeLayout)findViewById(R.id.programeGuideLoadingState);
		
		
		setTitle(channel_name);
		setLoadingState();

		ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new EpgTask().execute(scraper_id, channel_id);
		} else {
			setNetworkErrorState();
		}
	}
	
	
	private class EpgTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			// fetch data
			XBMC.getInstance().setAndroidContext(context);

			epg = new Epg();
			Scraper scraper = new Scraper(params[0]);

			try {
				epg.downloadEpg(scraper, params[1], null);
			} catch (ScraperError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		
		adapter = new ProgrammeAdapter(ProgrammeGuideActivity.this,
				R.layout.programme_list_item, epg.getProgrammeList());

		listView.setAdapter(adapter);
//		lv.setOnItemClickListener(this);
	}

	private void setLoadingState() {
		loadingState.setVisibility(View.VISIBLE);
		listView.setVisibility(View.GONE);
		networkErrorState.setVisibility(View.GONE);
	}
	
	private void setListViewState() {
		loadingState.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);
		networkErrorState.setVisibility(View.GONE);
	}
	
	private void setNetworkErrorState() {
		loadingState.setVisibility(View.GONE);
		listView.setVisibility(View.GONE);
		networkErrorState.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.programme_guide, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_programme_guide,
					container, false);
			return rootView;
		}
	}

}
