package com.groupagendas.groupagenda.chat;

import java.util.ArrayList;

import  android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.adapters.ChatThreadAdapter;
import com.groupagendas.groupagenda.data.EventManagement;

public class ChatThreadFragment extends Fragment {
	ChatThreadAdapter adapter;
	ViewGroup container;

	public static Fragment newInstance() {
		ChatThreadFragment f = new ChatThreadFragment();
		Bundle args = new Bundle();
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.chat, container, false);
		this.container = container;
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (container != null) {
			LinearLayout chatInputBlock = (LinearLayout) container.findViewById(R.id.chat_inputBlock);
			if (chatInputBlock != null) {
				chatInputBlock.setVisibility(View.INVISIBLE);
			}
			final ArrayList<ChatThreadObject> tmpArray = EventManagement.getExistingChatThreads(getActivity());
			adapter = new ChatThreadAdapter(getActivity(), tmpArray);
			ListView chat_message_list = (ListView) container.findViewById(R.id.chat_message_list);
			if (chat_message_list != null) {
				chat_message_list.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						Intent intent = new Intent(getActivity(), ChatMessageActivity.class);
						intent.putExtra("event_id", tmpArray.get(arg2).getEvent_id());
						startActivity(intent);
					}

				});
				chat_message_list.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		}
	}
}
