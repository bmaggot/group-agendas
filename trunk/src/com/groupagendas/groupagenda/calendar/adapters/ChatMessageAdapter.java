package com.groupagendas.groupagenda.calendar.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.chat.ChatMessageObject;
import com.groupagendas.groupagenda.data.DataManagement;

public class ChatMessageAdapter extends AbstractAdapter<ChatMessageObject> {

	public ChatMessageAdapter(Context context, List<ChatMessageObject> list) {
		super(context, list);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = mInflater.inflate(R.layout.chat_message, null);
		}

		final ChatMessageObject chatMessage = (ChatMessageObject) this.getItem(i);
//		if (!chatMessage.deleted) {
//			Account account = new Account();
//			TextView messageBody = (TextView) view.findViewById(R.id.chat_message_body);
//			messageBody.setText(chatMessage.message);
//			TextView chatTime = (TextView) view.findViewById(R.id.chat_message_time);
//			chatTime.setText(chatMessage.dateTime);
//			if (chatMessage.userId == account.getUser_id()) {
//				view.findViewById(R.id.kubiks).setVisibility(View.VISIBLE);
//				
//				TextView iksiuks = (TextView) view.findViewById(R.id.chat_message_delete);
//				iksiuks.setVisibility(View.VISIBLE);
//				iksiuks.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						Toast.makeText(getContext(), R.string.delete_chatm_progress, Toast.LENGTH_SHORT).show();
//						DataManagement.getInstance(getContext()).removeChatMessage(chatMessage.messageId, chatMessage.eventId);
//					}
//				});
//			}
//		}
		
		return view;
	}

}
