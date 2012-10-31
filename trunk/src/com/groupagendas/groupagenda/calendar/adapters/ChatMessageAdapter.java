package com.groupagendas.groupagenda.calendar.adapters;

import java.util.Calendar;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatMessageActivity;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
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
		Account account = new Account(context);
		TextView messageBody = (TextView) view.findViewById(R.id.chat_message_body);
		if (!chatMessage.isDeleted()) {
			messageBody.setText(chatMessage.getMessage());
			messageBody.setTextAppearance(context, R.style.chat_message_body);
		} else {
			messageBody.setText(context.getResources().getString(R.string.chat_deleted_message));
			messageBody.setTextAppearance(context, R.style.chat_deleted_message_style);
		}
		TextView chatTime = (TextView) view.findViewById(R.id.chat_message_time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Utils.unixTimestampToMilis(chatMessage.getCreated()));
		DateTimeUtils dtUtils = new DateTimeUtils(getContext());
		chatTime.setText(dtUtils.formatDateTime(calendar));
		TextView fromWho = (TextView) view.findViewById(R.id.chat_message_from_who);
		fromWho.setText(chatMessage.getFullname());
		if (chatMessage.getUserId() == account.getUser_id()) {
			view.findViewById(R.id.kubiks).setVisibility(View.VISIBLE);
			ImageView iksiuks = (ImageView) view.findViewById(R.id.delete_button);
			iksiuks.setClickable(true);
			if (!chatMessage.isDeleted()) {
				iksiuks.setVisibility(View.VISIBLE);
				view.setTag(true);
				iksiuks.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						context.deleteMessage(chatMessage.getMessageId(), chatMessage);
					}
				});
			}
		} else {
			view.findViewById(R.id.kubiks).setVisibility(View.GONE);
			ImageView iksiuks = (ImageView) view.findViewById(R.id.delete_button);
			iksiuks.setClickable(false);
			iksiuks.setVisibility(View.GONE);
			view.setTag(false);
		}

		return view;
	}

}
