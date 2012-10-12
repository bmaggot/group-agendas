package com.groupagendas.groupagenda.calendar.adapters;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatThreadObject;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class ChatThreadAdapter extends AbstractAdapter<ChatThreadObject> {

	private Account account;
	
	public ChatThreadAdapter(Context context, List<ChatThreadObject> list) {
		super(context, list);
		account = new Account(context);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.chat_message, null);
		}
		ChatThreadObject chatThread = (ChatThreadObject) this.getItem(i);
		TextView title = (TextView) view.findViewById(R.id.chat_message_body);
		title.setText(chatThread.getTitle());
		TextView chatTime = (TextView) view.findViewById(R.id.chat_message_time);
		Calendar timeStart = Calendar.getInstance();
		timeStart.setTimeInMillis(Utils.unixTimestampToMilis(chatThread.getTimeStart()));
		DateTimeUtils dtUtils = new DateTimeUtils(getContext());
		chatTime.setText(dtUtils.formatDateTime(timeStart));
		return view;
	}

}
