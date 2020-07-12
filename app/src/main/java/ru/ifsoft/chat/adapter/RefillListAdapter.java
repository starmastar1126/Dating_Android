package ru.ifsoft.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ru.ifsoft.chat.R;
import ru.ifsoft.chat.constants.Constants;
import ru.ifsoft.chat.model.RefillItem;

public class RefillListAdapter extends BaseAdapter implements Constants {

	private Activity activity;
	private LayoutInflater inflater;
	private List<RefillItem> itemList;

	public RefillListAdapter(Activity activity, List<RefillItem> itemList) {

		this.activity = activity;
		this.itemList = itemList;
	}

	@Override
	public int getCount() {

		return itemList.size();
	}

	@Override
	public Object getItem(int location) {

		return itemList.get(location);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}
	
	static class ViewHolder {

        public TextView mRefillType;
        public TextView mRefillAmount;
        public TextView mRefillDate;
	        
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = null;

		if (inflater == null) {

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

		if (convertView == null) {
			
			convertView = inflater.inflate(R.layout.refill_row, null);
			
			viewHolder = new ViewHolder();

            viewHolder.mRefillType = (TextView) convertView.findViewById(R.id.refill_type);
            viewHolder.mRefillAmount = (TextView) convertView.findViewById(R.id.refill_amount);
			viewHolder.mRefillDate = (TextView) convertView.findViewById(R.id.refill_date);

            convertView.setTag(viewHolder);

		} else {
			
			viewHolder = (ViewHolder) convertView.getTag();
		}

        viewHolder.mRefillType.setTag(position);
        viewHolder.mRefillAmount.setTag(position);
        viewHolder.mRefillDate.setTag(position);
		
		final RefillItem item = itemList.get(position);

        viewHolder.mRefillType.setText(this.activity.getString(R.string.label_refill_type_google));

        viewHolder.mRefillAmount.setText(this.activity.getString(R.string.label_refill_amount) + " " + Integer.toString(item.getAmount()));

        viewHolder.mRefillDate.setText(item.getDate());

		return convertView;
	}
}