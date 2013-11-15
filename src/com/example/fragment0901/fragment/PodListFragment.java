package com.example.fragment0901.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.example.fragment0901.R;
import com.example.fragment0901.adapter.ItemListAdapter;
import com.example.fragment0901.utils.CallBacksInterface;
import com.example.fragment0901.utils.PodCast;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;


public class PodListFragment extends Fragment {
	private final String tag = ((Object)this).getClass().getSimpleName();
	private View view;
	private Context context;
	private ListView ListOfPodcast;
	private ItemListAdapter mAdapter;
	private static List<PodCast> PodcastList;
	private ProgressBar progressBar;
	private CallBacksInterface callBack;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callBack = (CallBacksInterface) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity();
		view = inflater.inflate(R.layout.pod_list_fragment, container, false);
		ListOfPodcast = (ListView) view.findViewById(R.id.listView);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

       /* // Look up the AdView as a resource and load a request.
        AdView adView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4").build();
        adView.loadAd(adRequest);*/

		// TODO figureOut the visibility of progress bar container

		if (getConnectionStatus()){
            // TODO Show progressDialog instead of simple progress bar
            getDataFromInternet();
        }
		ListOfPodcast.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long arg3) {
                PodCast selectedItem = (PodCast) mAdapter.getItem(position);
                Log.i(tag, selectedItem.getTitle() + " clicked Item");
                callBack.onItemSelected(selectedItem);
            }
        });
		return view;
	}

	private boolean getConnectionStatus() {
		boolean found = false;
		ConnectivityManager cm = (ConnectivityManager) context
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			found = true;
		}
		return found;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void getDataFromInternet() {
		if (getConnectionStatus()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				new MyAsyncTask().executeOnExecutor(
						AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
				// Log.i(TAG, "Bulid version >= HoneyComb");
			} else {
				new MyAsyncTask().execute();
			}
		}
	}

	public class MyAsyncTask extends AsyncTask<Void, Void, Boolean> {

		protected Boolean doInBackground(Void... Params) {
			boolean status = false;
			if (PodcastList == null)
				PodcastList = getLatestItems();

			if (PodcastList != null)
				status = true;

			return status;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// Log.i(TAG, "myAsyncTask finished its task.");

			progressBar.setVisibility(View.INVISIBLE);

			if (result)
				displayData();
		}

	}

	private List<PodCast> getLatestItems() {
		List<PodCast> list = new ArrayList<PodCast>();
		String connection = PodCast.URL;
		// TODO make a new parser class
		try {
			URL url = new URL(connection);
			// Log.i(TAG, "Try to open: " + connection);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			int responseCode = conn.getResponseCode();
			// Log.i(TAG, "Response code is: " + responseCode);

			if (responseCode != HttpURLConnection.HTTP_OK) {
				// Log.e(TAG, "Couldn't open connection in getLatestItems()");
				return null;
			}

			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(false);
			XmlPullParser xpp = factory.newPullParser();

			// We will get the XML from an input stream
			xpp.setInput(getInputStream(url), "UTF_8");

			// get titles and (links) out of xml
			String title = null;
			String summary = null;
			String link = null;
			String duration = null;
			String date = null;
			PodCast resultRow = new PodCast(title, summary, link, duration,
					date);
			boolean insideItem = false;

			// Returns the type of current event: START_TAG, END_TAG, etc..
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				if (eventType == XmlPullParser.START_TAG) {
					String name = xpp.getName();

					// check if we are inside Item or not
					if (name.equalsIgnoreCase("item")) {
						insideItem = true;
					}
					if (name.equalsIgnoreCase("title")) {
						if (insideItem)
							resultRow.setTitle(xpp.nextText().toString());
					}
					if (name.equalsIgnoreCase("pubDate")) {

						resultRow.setDate(xpp.nextText().toString()
								.substring(0, 16));
					}
					if (name.equalsIgnoreCase("itunes:summary")) {
						resultRow.setSummary(xpp.nextText().toString());
					}
					if (name.equalsIgnoreCase("itunes:duration")) {
						resultRow.setDuration(xpp.nextText().toString());
					}
					if (name.equalsIgnoreCase("media:content")) {
						// String tempLink = xpp.getAttributeValue(null, "url");
						resultRow.setLink(xpp.getAttributeValue(null, "url")
								.toString());
					}
					// add resultRow to the list and reset it when hit END_TAG
				} else if (eventType == XmlPullParser.END_TAG
						&& xpp.getName().equalsIgnoreCase("item")) {
					// if any one of the attributes were null by this point,
					// just leave it blank.(prevent null pointer exception!)
					if (title == null)
						title = "";
					if (summary == null)
						summary = "";
					if (link == null)
						link = "";
					if (date == null)
						date = "";
					if (duration == null)
						duration = "";

					list.add(resultRow);
					resultRow = new PodCast(title, summary, link, duration,
							date);
					insideItem = false;
				}

				eventType = xpp.next(); // move to next element
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// Log.i(TAG, "Error in parsing xml...");
			e.printStackTrace();
		}

		return list;

	}

	private InputStream getInputStream(URL url) {
		try {
			return url.openConnection().getInputStream();
		} catch (IOException e) {
			return null;
		}
	}

	private void displayData() {
		mAdapter = new ItemListAdapter(context, PodcastList);
		ListOfPodcast.setAdapter(mAdapter);
	}

}
