package com.groupagendas.groupagenda.calendar.adapters;

import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatMessageActivity;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.utils.DateTimeUtils;

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
		if(chatMessage.getCreated() != null){
			calendar.setTimeInMillis(chatMessage.getCreated());
		}
		DateTimeUtils dtUtils = new DateTimeUtils(getContext());
		chatTime.setText(dtUtils.formatDateTime(calendar));
		TextView fromWho = (TextView) view.findViewById(R.id.chat_message_from_who);
		if (chatMessage.getUserId() == account.getUser_id()) {
			fromWho.setText(getContext().getResources().getString(R.string.you));
			view.findViewById(R.id.kubiks).setVisibility(View.VISIBLE);
			final ImageView iksiuks = (ImageView) view.findViewById(R.id.delete_button);
			iksiuks.setClickable(true);
			if (!chatMessage.isDeleted()) {
				iksiuks.setVisibility(View.VISIBLE);
				view.setTag(true);
				iksiuks.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						 new AlertDialog.Builder(context)
					        .setIcon(android.R.drawable.ic_dialog_alert)
					        .setTitle(context.getResources().getString(R.string.delete_message_title))
					        .setMessage(context.getResources().getString(R.string.delete_message_confirmation))
					        .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					        	iksiuks.setEnabled(false);
					        	context.deleteMessage(chatMessage.getMessageId(), chatMessage); 
					        }

					    })
					    .setNegativeButton(context.getResources().getString(R.string.no), null)
					    .show();
					}
				});
			} else {
				iksiuks.setVisibility(View.INVISIBLE);
			}
		} else {
			fromWho.setText(chatMessage.getFullname());
			view.findViewById(R.id.kubiks).setVisibility(View.GONE);
			ImageView iksiuks = (ImageView) view.findViewById(R.id.delete_button);
			iksiuks.setClickable(false);
			iksiuks.setVisibility(View.GONE);
			view.setTag(false);
		}
		EventManagement.resetEventsNewMessageCount(context, chatMessage.getEventId());
		return view;
	}

}
