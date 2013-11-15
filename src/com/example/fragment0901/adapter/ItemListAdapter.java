package com.example.fragment0901.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fragment0901.R;
import com.example.fragment0901.utils.PodCast;

import java.util.List;

public class ItemListAdapter extends BaseAdapter {

	//private final String TAG = "** ListAdapter **";
	//private final int[] colors = new int[] { 0xFFE5E5E5, 0xFFFFFFFF };

	private LayoutInflater myInflater;
	private List<PodCast> ItemsList;
    private Context mContext;

	public ItemListAdapter(Context context, List<PodCast> ItemsList) {
		myInflater = LayoutInflater.from(context);
        mContext = context;
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

			holder.rlContainer = (RelativeLayout) convertView.findViewById(R.id.rlContainer);
			holder.tvtitle = (TextView) convertView.findViewById(R.id.tvTitle);
			holder.tvtime = (TextView) convertView.findViewById(R.id.tvTime);
			holder.tvSum = (TextView) convertView.findViewById(R.id.tvSum);
			holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
            holder.menu = (ImageView) convertView.findViewById(R.id.menu);
            holder.englishCafe = (ImageView) convertView.findViewById(R.id.cafe);
            holder.favorite = (ImageView) convertView.findViewById(R.id.favorite_button);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tvtitle.setText(ItemsList.get(position).getTitle());
		holder.tvtime.setText(ItemsList.get(position).getDuration());
		holder.tvSum.setText(ItemsList.get(position).getSummary());
		holder.tvDate.setText(ItemsList.get(position).getDate());
        if (ItemsList.get(position).getTitle().toLowerCase().contains("english cafe")) {
            holder.englishCafe.setVisibility(View.VISIBLE);
            holder.tvSum.setMaxLines(4);
        } else {
            holder.englishCafe.setVisibility(View.GONE);
            holder.tvSum.setMaxLines(2);
        }
        holder.favorite.setBackgroundResource(position%2 == 0? android.R.drawable.btn_star_big_on: android.R.drawable.btn_star_big_off);
        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Favorite btn clicked ", Toast.LENGTH_SHORT).show();
            }
        });
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout row = (RelativeLayout) v.getParent();
                String ttl = ((TextView) row.findViewById(R.id.tvTitle)).getText().toString();
                // TODO show menu Items such as Download, Delete, Mark heard/Unheard
                Toast.makeText(mContext, "Some menu Items for Item " + ttl, Toast.LENGTH_SHORT).show();
            }
        });
		return convertView;
	}

	static class ViewHolder {
		TextView tvSum;
		TextView tvtime;
		RelativeLayout rlContainer;
		TextView tvtitle;
		TextView tvDate;
        ImageView englishCafe,menu, favorite;
	}

}
