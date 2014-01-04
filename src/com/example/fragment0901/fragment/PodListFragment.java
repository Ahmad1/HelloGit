package com.example.fragment0901.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fragment0901.R;
import com.example.fragment0901.adapter.ItemListAdapter;
import com.example.fragment0901.utils.CallBacksInterface;
import com.example.fragment0901.utils.ESLConstants;
import com.example.fragment0901.utils.PodCast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PodListFragment extends Fragment {
	private final String tag = ((Object) this).getClass().getSimpleName();
	private View view;
	private Context context;
	private ListView ListOfPodcast;
	private Button updateBtn;
	private ItemListAdapter mAdapter;
	private List<PodCast> PodcastList;
	private ProgressBar progressBar;
	private CallBacksInterface callBack;
	private SharedPreferences mSharedPref;
	private String sharedResponse;
	private String lastUpdated;
	private boolean DEBUG = PodListActivity.loggingEnabled();
    // private AdView adView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callBack = (CallBacksInterface) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = getActivity();
		mSharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

		view = inflater.inflate(R.layout.pod_list_fragment, container, false);
		ListOfPodcast = (ListView) view.findViewById(R.id.listView);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		updateBtn = (Button) view.findViewById(R.id.btn_update);
		updateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getConnectionStatus()) {
					ListOfPodcast.setVisibility(View.GONE);
					progressBar.setVisibility(View.VISIBLE);
					getDataFromInternet();
				} else {
					// show activity A noConnection Button
					Toast.makeText(context, "No Connection!", Toast.LENGTH_LONG).show();
				}
			}
		});

		if (getConnectionStatus()) {
			if (mSharedPref != null) {
				lastUpdated = mSharedPref.getString(ESLConstants.LAST_UPDATED, null);
				sharedResponse = mSharedPref.getString(ESLConstants.XML_RESPONSE_STRING, null);
			}
			if (sharedResponse != null) {
                if (DEBUG) Log.i(tag, "loading from shared prefs");
				if (PodcastList == null)
					PodcastList = getLatestItems(true);
				displayData();
			} else {
				if (DEBUG)
					Log.i(tag, "loading from internet");
				getDataFromInternet();
			}
		}

		ListOfPodcast.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				PodCast selectedItem = (PodCast) mAdapter.getItem(position);
                if (DEBUG) Log.i(tag, selectedItem.getTitle() + " clicked Item");
				callBack.onItemSelected(selectedItem);
			}
		});
		return view;
	}

	private boolean getConnectionStatus() {
		boolean found = false;
		ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(
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
				new MyAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
				if (DEBUG)
                    if (DEBUG) Log.i(tag, "Bulid version >= HoneyComb");
			} else {
				new MyAsyncTask().execute();
			}
		}
	}

	public class MyAsyncTask extends AsyncTask<Void, Void, Boolean> {

		protected Boolean doInBackground(Void... Params) {
			boolean status = false;
			if (DEBUG)
				Log.i(tag, "myAsyncTask doInBackground.");
			PodcastList = getLatestItems(false);
			if (DEBUG)
				Log.i(tag, "myAsyncTask getLatestItems(false).");

			if (PodcastList != null)
				status = true;

			return status;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (DEBUG)
				Log.i(tag, "myAsyncTask finished its task.");
			if (result)
				displayData();
		}

	}

	private List<PodCast> getLatestItems(boolean haveResponse) {
		List<PodCast> list = new ArrayList<PodCast>();

		try {

			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(false);
			XmlPullParser xpp = factory.newPullParser();

			if (haveResponse) {
				ByteArrayInputStream mInputStream = new ByteArrayInputStream(sharedResponse.getBytes());
				xpp.setInput(mInputStream, "UTF_8");
			} else {
				String connection = ESLConstants.URL;
				URL url = new URL(connection);
				if (DEBUG)
					Log.i(tag, "Try to open: " + connection);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				int responseCode = conn.getResponseCode();
				if (DEBUG)
					Log.i(tag, "Response code is: " + responseCode);
				if (responseCode != HttpURLConnection.HTTP_OK) {
					if (DEBUG)
						Log.e(tag, "Couldn't open connection in getLatestItems()");
					return null;
				}
				xpp.setInput(getInputStream(url), "UTF_8");
				// save response as string and use it until user wants to
				// update.
				DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                lastUpdated = df.format(Calendar.getInstance().getTime());
                if (DEBUG) {
                    Log.e(tag, "lastUpdated: " + lastUpdated);
                }
                lastUpdated = "Last Update:  " + lastUpdated;

                String xmlResponse = convertStreamToString(getInputStream(url));
                SharedPreferences.Editor editor = mSharedPref.edit();
				editor.putString(ESLConstants.XML_RESPONSE_STRING, xmlResponse);
				editor.putString(ESLConstants.LAST_UPDATED, lastUpdated);
				editor.commit();
			}

			// get titles and (links) out of xml
			String title = null;
			String summary = null;
			String link = null;
			String duration = null;
			String date = null;
            String shareLink = null;
			PodCast resultRow = new PodCast(title, summary, link, duration, date, shareLink);
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

						resultRow.setDate(xpp.nextText().toString().substring(0, 16));
					}
					if (name.equalsIgnoreCase("itunes:summary")) {
						resultRow.setSummary(xpp.nextText().toString());
					}
					if (name.equalsIgnoreCase("itunes:duration")) {
						resultRow.setDuration(xpp.nextText().toString());
					}
                    if (name.equalsIgnoreCase("link")) {
						resultRow.setShareLink(xpp.nextText().toString());
					}
					if (name.equalsIgnoreCase("media:content")) {
						// String tempLink = xpp.getAttributeValue(null, "url");
						resultRow.setLink(xpp.getAttributeValue(null, "url").toString());
					}
					// add resultRow to the list and reset it when hit END_TAG
				} else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
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
                    if (shareLink == null)
						shareLink = "";

					list.add(resultRow);
					resultRow = new PodCast(title, summary, link, duration, date, shareLink);
					insideItem = false;
				}

				eventType = xpp.next(); // move to next element
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			if (DEBUG)
				Log.i(tag, "Error in parsing xml...");
			e.printStackTrace();
		}
        if (DEBUG)
            Log.i(tag, "Done parsing xml..."+ list.size());
		return list;

	}

	private InputStream getInputStream(URL url) {
		try {
			return url.openConnection().getInputStream();
		} catch (IOException e) {
            if (DEBUG)
                Log.i(tag, "getInputStream Faild...");
			return null;
		}
	}

	private String convertStreamToString(java.io.InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder out = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		String line;
		while ((line = reader.readLine()) != null) {
			out.append(line);
			out.append(newLine);
		}
		return out.toString();
	}

	private void displayData() {
        if (DEBUG)
            Log.i(tag, "displaying Data... ");
		progressBar.setVisibility(View.INVISIBLE);
		mAdapter = new ItemListAdapter(context, PodcastList);
		ListOfPodcast.setAdapter(mAdapter);
		ListOfPodcast.setVisibility(View.VISIBLE);
		updateBtn.setText(lastUpdated);
        if (DEBUG)
            Log.i(tag, "displaying Data... Done! ");
	}

}
