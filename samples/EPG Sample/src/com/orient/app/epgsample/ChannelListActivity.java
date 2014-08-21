package com.orient.app.epgsample;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.orient.lib.xbmc.XBMC;
import com.orient.lib.xbmc.addons.Scraper;
import com.orient.lib.xbmc.addons.ScraperError;
import com.orient.lib.xbmc.epg.ChannelInfoTag;
import com.orient.lib.xbmc.epg.Epg;

public class ChannelListActivity extends ActionBarActivity {

	Context context;
	ListView listView ;
	ChannelAdapter adapter;
	RelativeLayout loadingState;
	ArrayList<ChannelInfoTag> channels;
	private LinearLayout networkErrorState;
	private String scraper_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_list);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}

		//////// Getting Intent
		Intent myIntent = getIntent(); // gets the previously created intent
		scraper_id = myIntent.getStringExtra("scraper_id"); // will return "FirstKeyValue"
		String scraper_name = myIntent.getStringExtra("scraper_name"); // will return "FirstKeyValue"

		//////// Setting variables
		context = this;
		listView = (ListView) findViewById(R.id.channelList);
		networkErrorState = (LinearLayout)findViewById(R.id.networkErrorState);
		loadingState = (RelativeLayout)findViewById(R.id.channelListLoadingState);

		setTitle(scraper_name);
		setLoadingState();

		ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new EpgTask().execute(scraper_id);
		} else {
			setNetworkErrorState();
		}
	}


	// Uses AsyncTask to create a task away from the main UI thread. This task takes a 
	// URL string and uses it to create an HttpUrlConnection. Once the connection
	// has been established, the AsyncTask downloads the contents of the webpage as
	// an InputStream. Finally, the InputStream is converted into a string, which is
	// displayed in the UI by the AsyncTask's onPostExecute method.
	private class EpgTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... scraper_id) {
			// fetch data
			XBMC.getInstance().setAndroidContext(context);

			Epg epg = new Epg();
			Scraper scraper = new Scraper(scraper_id[0]);

			try {
				channels = epg.findChannel(scraper, null);
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
		
		adapter = new ChannelAdapter(ChannelListActivity.this,
				R.layout.channel_list_item, channels);

		listView.setAdapter(adapter);
//		lv.setOnItemClickListener(this);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// ListView Clicked item index
				int itemPosition = position;

				Intent myIntent = new Intent(ChannelListActivity.this, ProgrammeGuideActivity.class);
				myIntent.putExtra("scraper_id", scraper_id);
				myIntent.putExtra("channel_id", channels.get(itemPosition).id);
				myIntent.putExtra("channel_name", channels.get(itemPosition).name);
				startActivity(myIntent);
			}

		}); 
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
		getMenuInflater().inflate(R.menu.channel_list, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_channel_list,
					container, false);
			return rootView;
		}
	}

}
