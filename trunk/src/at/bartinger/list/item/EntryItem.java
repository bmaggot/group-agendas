package at.bartinger.list.item;

import com.groupagendas.groupagenda.events.Event;


public class EntryItem implements Item{

	public final Event event;

	public EntryItem(Event event) {
		this.event = event;
	}
	
	@Override
	public boolean isSection() {
		return false;
	}

}
