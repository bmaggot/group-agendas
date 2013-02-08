package com.groupagendas.groupagenda.timezone;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LatestEventStructure {
	private static final int MAX_ITEM_AMOUNT = 10;
	
	public class LatestEventMetaData {
		public static final String LATELY_CREATED_EVENTS = "lately_created_events";
		public static final String PRESENT_ITEM_AMOUNT_KEY = "item_amount";
	}
	
	SharedPreferences prefs;
	Editor editor;
	
	public LatestEventStructure(Context context) {
		prefs = context.getSharedPreferences(LatestEventMetaData.LATELY_CREATED_EVENTS, Context.MODE_PRIVATE);
		editor = prefs.edit();
	}
	
	public boolean itemInsert(int id) {
		int presentItemCount = getPresentItemCount();
		
		if (presentItemCount < MAX_ITEM_AMOUNT) {
			editor.putInt(""+presentItemCount, id);
			
			return setPresentItemCount(presentItemCount+1);
		} else {
			for (int i = 0; i < presentItemCount; i++) {
				editor.putInt(""+i, getItemId(i+1));
			}			
			editor.putInt(""+presentItemCount, id);
			
			return setPresentItemCount(presentItemCount);
		}
	}
	
	/**
	 * Get present items count.
	 * 
	 * @author meska.lt@gmail.com
	 * @since 2013-01-31
	 * @version 1
	 */
	public int getPresentItemCount() {
		return prefs.getInt(LatestEventMetaData.PRESENT_ITEM_AMOUNT_KEY, 0);
	}
	
	/**
	 * Sets present items count.
	 * 
	 * @author meska.lt@gmail.com
	 * @since 2013-01-31
	 * @version 1
	 * @return	true - if amount was overwritten successfully;
	 * 			false - if overwriting was unsuccessful.
	 */
	public boolean setPresentItemCount(int amount) {
		if ((amount <= MAX_ITEM_AMOUNT) && (amount > 0)) {
			editor.putInt(LatestEventMetaData.PRESENT_ITEM_AMOUNT_KEY, amount);
		}
		
		return editor.commit();
	}
	
	/**
	 * Get event id, located in position given by parameter.
	 * 
	 * @author meska.lt@gmail.com
	 * @since 2013-01-31
	 * @version 1
	 * @return	event_id if successful;
	 * 			-1 if id isn't present or position parameter is wrong.
	 */
	public int getItemId(int position) {
		return prefs.getInt(""+position, -1);
	}
	
	/**
	 * Get all event id's that were inserted lately.
	 * 
	 * Returns an array of event ids.
	 * 
	 * @author meska.lt@gmail.com
	 * @since 2013-01-31
	 * @version 1
	 * @return	integer array containing lately created event ids.
	 */
	public int[] getItemIds() {
		int itemAmount = getPresentItemCount();
		int[] ids = new int[itemAmount];
		
		for (int i = 0; i < itemAmount; i++) {
			ids[i] = getItemId(i);
		}
		
		return ids;
	}
	
	/**
	 * Overwrites all event id's that were created lately.
	 * 
	 * Sets event_id value to corresponding one, located in given parameter, and resets
	 * present items count.
	 * 
	 * @author meska.lt@gmail.com
	 * @since 2013-01-31
	 * @version 1
	 * @return	true - if all items were overwritten successfully;
	 * 			false - if overwriting was unsuccessful.
	 */
	public void batchItemInsert(int[] ids) {
		int itemsToInsert;
		
		if (ids.length > MAX_ITEM_AMOUNT) {
			itemsToInsert = MAX_ITEM_AMOUNT;
		} else {
			itemsToInsert = ids.length;
		}
		
		for (int i = 0; i < itemsToInsert; i++) {
			
		}
	}

	/**
	 * Clears all event id's created lately.
	 * 
	 * Sets event_id value to -1 for every possible entry located in SharedPreferences and
	 * resets present items count.
	 * 
	 * @author meska.lt@gmail.com
	 * @since 2013-01-31
	 * @version 1
	 * @return	true - if all items were cleared
	 * 			false - if clearing was unsuccessful
	 */
	public boolean clearItems() {
		int presentItemAmount = getPresentItemCount();
		for (int i = 0; i < presentItemAmount; i++) {
			editor.putInt(""+i, -1);
		}
		
		return setPresentItemCount(0);
	}
	
	@Override
	public String toString() {
		int itemsPresent = getPresentItemCount();
		String ids = "";
		
		if (getPresentItemCount() > 0) {
			for (int i = 0; i < itemsPresent; i++) {
				ids += "" + getItemId(i);
				if (i != (itemsPresent-1)) {
					ids += ", ";
				}
			}
			
		}
		
		return ids;
	}
}
