package com.groupagendas.groupagenda.chat;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;

public class ChatThreadsAdapter extends ArrayAdapter<ChatThreadObject>{

	private ArrayList<ChatThreadObject> items;
	
	public ChatThreadsAdapter(Context context, int textViewResourceId, ArrayList<ChatThreadObject> items) {
		super(context, textViewResourceId);
		this.items = items;
	}

}
