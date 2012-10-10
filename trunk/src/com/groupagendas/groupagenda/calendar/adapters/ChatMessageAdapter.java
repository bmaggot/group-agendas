package com.groupagendas.groupagenda.calendar.adapters;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatMessageActivity;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.data.ChatManagement;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class ChatMessageAdapter extends AbstractAdapter<ChatMessageObject> {

	ChatMessageActivity context;
	
	public ChatMessageAdapter(ChatMessageActivity context, List<ChatMessageObject> list) {
		super(context, list);
		this.context = context;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.chat_message, viewGroup, false);
		}

		final ChatMessageObject chatMessage = (ChatMessageObject) this.getItem(i);
		if (!chatMessage.isDeleted()) {
			Account account = new Account(context);
			TextView messageBody = (TextView) view.findViewById(R.id.chat_message_body);
			messageBody.setText(chatMessage.getMessage());
			TextView chatTime = (TextView) view.findViewById(R.id.chat_message_time);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(Utils.unixTimestampToMilis(chatMessage.getCreated()));
			DateTimeUtils dtUtils = new DateTimeUtils(getContext());
			chatTime.setText(dtUtils.formatDateTime(calendar));
			if (chatMessage.getUserId() == account.getUser_id()) {
				view.findViewById(R.id.kubiks).setVisibility(View.VISIBLE);
				TextView iksiuks = (TextView) view.findViewById(R.id.chat_message_delete);
				iksiuks.setClickable(true);
				iksiuks.setVisibility(View.VISIBLE);
				view.setTag(true);
				iksiuks.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						context.deleteMessage(chatMessage.getMessageId(), chatMessage);
					}
				});
			} else {
				view.findViewById(R.id.kubiks).setVisibility(View.GONE);
				TextView iksiuks = (TextView) view.findViewById(R.id.chat_message_delete);
				iksiuks.setClickable(false);
				iksiuks.setVisibility(View.GONE);
				view.setTag(false);
			}
		}

		return view;
	}

}
