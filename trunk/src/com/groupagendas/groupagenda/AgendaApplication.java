package com.groupagendas.groupagenda;

import java.util.HashMap;

import android.app.Application;

import com.groupagendas.groupagenda.utils.FileCache;
import com.groupagendas.groupagenda.utils.ImageLoader;
import com.groupagendas.groupagenda.utils.MemoryCache;

public class AgendaApplication extends Application {
	
	private MemoryCache memoryCache = new MemoryCache();
	private HashMap<String, FileCache> fileCaches = new HashMap<String, FileCache>();
	private ImageLoader imageLoader;

	@Override
	public void onCreate() {
		imageLoader = new ImageLoader(this);
	}
	
	public ImageLoader getImageLoader(){
		return imageLoader;
	}
	
	public MemoryCache getMemoryCache(){
		return memoryCache;
	}
	
	public FileCache getFileCache(String folder) {
		FileCache fc = fileCaches.get(folder);
		if(fc != null){
			return fc;
		}else{
			fc = new FileCache(this, folder);
			fileCaches.put(folder, fc);
			return fc;
		}
	}
}