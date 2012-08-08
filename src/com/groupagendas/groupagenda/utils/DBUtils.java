package com.groupagendas.groupagenda.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.groupagendas.groupagenda.error.report.Reporter;

import android.content.Context;
import android.os.Environment;

public class DBUtils {
	
	public final static String DBPath = Environment.getDataDirectory() + "/data/com.groupagendas.groupagenda/databases/";
	
	private String mDBName;
	
	public DBUtils(String DBName){
		mDBName = DBName;
	}
	
	public boolean checkDataBase() {
		File dbFile = new File(DBPath + mDBName);
		return dbFile.exists();
	}
	
	public void copyDataBase(Context cont) {
		InputStream myInput;
		try {
			myInput = cont.getAssets().open(mDBName);
			String outFileName = DBPath + mDBName;

			File f = new File(DBPath);
			if (!f.exists())
				f.mkdir();

			OutputStream myOutput = new FileOutputStream(outFileName);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			myOutput.flush();
			myOutput.close();
			myInput.close();
		} catch (IOException e) {
			Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}
	}
}
