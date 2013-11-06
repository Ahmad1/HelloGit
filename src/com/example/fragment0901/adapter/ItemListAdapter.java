package com.example.fragment0901.adapter;

import java.util.List;

import com.example.fragment0901.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ItemListAdapter extends BaseAdapter {

	//private final String TAG = "** ListAdapter **";
	//private final int[] colors = new int[] { 0xFFE5E5E5, 0xFFFFFFFF };

	private LayoutInflater myInflater;
	private List<PodCast> ItemsList;

	public ItemListAdapter(Context context, List<PodCast> ItemsList) {
		myInflater = LayoutInflater.from(context);

		this.ItemsList = ItemsList;
		//Log.i(TAG, "Adapter has been setup successfully.");
	}

	public int getCount() {
		// return ItemsList.size();
		return ItemsList.size();
	}

	public PodCast getItem(int position) {
		// TODO Auto-generated method stub
		return ItemsList.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = myInflater.inflate(R.layout.row, null);
			holder = new ViewHolder();

			holder.rlContainer = (RelativeLayout) convertView
					.findViewById(R.id.rlContainer);
			holder.tvtitle = (TextView) convertView.findViewById(R.id.tvTitle);
			holder.tvtime = (TextView) convertView.findViewById(R.id.tvTime);
			holder.tvSum = (TextView) convertView.findViewById(R.id.tvSum);
			holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tvtitle.setText(ItemsList.get(position).getTitle());
		holder.tvtime.setText(ItemsList.get(position).getDuration());
		holder.tvSum.setText(ItemsList.get(position).getSummary());
		holder.tvDate.setText(ItemsList.get(position).getDate());

		return convertView;
	}

	static class ViewHolder {
		public TextView tvSum;
		public TextView tvtime;
		RelativeLayout rlContainer;
		TextView tvtitle;
		TextView tvDate;

	}

}
