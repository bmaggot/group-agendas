package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.data.Data;

public class ChatMessageAdapter extends AbstractAdapter<ChatMessageObject> {

	public ChatMessageAdapter(Context context, List<ChatMessageObject> list) {
		super(context, list);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.chat_message, null);
		}
		ChatMessageObject chatMessage = (ChatMessageObject) this.getItem(i);
		if(!chatMessage.deleted){
			TextView title = (TextView) view.findViewById(R.id.chat_message_body);
			title.setText(chatMessage.message);
			TextView chatTime = (TextView) view.findViewById(R.id.chat_message_time);
			chatTime.setText(chatMessage.dateTime);
			if(chatMessage.userId != Data.getAccount().user_id){
//				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
//				params.leftMargin += 100;
//				view.setLayoutParams(params);
			}
		}
		return view;
	}

}
