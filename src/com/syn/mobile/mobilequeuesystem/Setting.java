package com.syn.mobile.mobilequeuesystem;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class Setting {
	private DatabaseHelper dbHelper;
	private final String TABLE = "settings";
	
	public Setting(Context c){
		dbHelper = new DatabaseHelper(c);
	}
	
	public int insertSetting(int shopId, String serverIp, String serviceName,
			long updateInterval, String vdoPath, String imgPath){
		int status = 0;
		
		openDatabase();
		dbHelper.myDataBase.execSQL("DELETE FROM " + TABLE);
		
		try {
			ContentValues cv = new ContentValues();
			cv.put("shopid", shopId);
			cv.put("serverip", serverIp);
			cv.put("servicename", serviceName);
			if(updateInterval != 0)
				cv.put("updateinterval", updateInterval);
			if(!vdoPath.equals(""))
				cv.put("vdopath", vdoPath);
			if(!imgPath.equals(""))
				cv.put("imgpath", imgPath);
			dbHelper.myDataBase.insert(TABLE, null, cv);
			status = 1;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeDatabase();
		return status;
	}
	
	public HashMap<String, String> getSetting(){
		String strSql = "SELECT * FROM " + TABLE;

		HashMap<String, String> setting = 
				new HashMap<String, String>();
		
		openDatabase();
		Cursor cursor = dbHelper.myDataBase.rawQuery(strSql, null);
		if(cursor.moveToFirst()){
			setting.put("shopid", cursor.getString(cursor.getColumnIndex("shopid")));
			setting.put("serverip", cursor.getString(cursor.getColumnIndex("serverip")));
			setting.put("servicename", cursor.getString(cursor.getColumnIndex("servicename")));
			setting.put("updateinterval", cursor.getString(cursor.getColumnIndex("updateinterval")));
			setting.put("vdopath", cursor.getString(cursor.getColumnIndex("vdopath")));
			setting.put("imgpath", cursor.getString(cursor.getColumnIndex("imgpath")));
		}
		cursor.close();
		closeDatabase();
		return setting;
	}
	
	private void openDatabase(){
		dbHelper.openDataBase();
	}
	
	private void closeDatabase(){
		dbHelper.closeDataBase();
	}
}
