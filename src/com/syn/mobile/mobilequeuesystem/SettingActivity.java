package com.syn.mobile.mobilequeuesystem;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends Activity {
	private EditText txtShopId;
	private EditText txtServerIp;
	private EditText txtServiceName;
	private EditText txtUpdateInterval;
	private EditText txtVDOPath;
	private EditText txtImgPath;
	
	private GlobalVar globalVar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		globalVar = new GlobalVar(SettingActivity.this);
		
		txtShopId = (EditText) findViewById(R.id.editTextShopID);
		txtServerIp = (EditText) findViewById(R.id.editTextServerIp);
		txtServiceName = (EditText) findViewById(R.id.editTextServiceName);
		txtUpdateInterval = (EditText) findViewById(R.id.editTextUpdateInterval);
		txtVDOPath = (EditText) findViewById(R.id.editTextVDOPath);
		txtImgPath = (EditText) findViewById(R.id.editTextImagePath);
		Button btnMinus = (Button) findViewById(R.id.buttonMinus);
		Button btnPlus = (Button) findViewById(R.id.buttonPlus);
		
		btnMinus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				int time = Integer.parseInt(txtUpdateInterval.getText().toString());
				if(--time > 9999)
					txtUpdateInterval.setText(String.valueOf(time));
			}
			
		});
		
		btnPlus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				int time = Integer.parseInt(txtUpdateInterval.getText().toString());
				txtUpdateInterval.setText(String.valueOf(++time));
			}
			
		});
		
		displaySetting();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.setting, menu);
		View v = menu.findItem(R.id.item_ctrl).getActionView();
		
		Button btnClose = (Button) v.findViewById(R.id.buttonClose);
		Button btnSave = (Button) v.findViewById(R.id.buttonSave);
		
		btnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				cancelClicked(v);
			}
			
		});
		
		btnSave.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				saveClicked(v);
			}
			
		});
		
		return true;
	}

	private void displaySetting(){
		txtShopId.setText(String.valueOf(globalVar.getShopId()));
		txtServerIp.setText(globalVar.getServerIp());
		txtServiceName.setText(globalVar.getServiceName());
		txtUpdateInterval.setText(String.valueOf(globalVar.getRefreshTime()));
		txtVDOPath.setText(globalVar.getVdoPath());
		txtImgPath.setText(globalVar.getImgPath());
	}
	
	public void saveClicked(final View v){
		int shopId = txtShopId.getText().toString().equals("") ? 0 : Integer.parseInt(txtShopId.getText().toString());
		String serverIp = txtServerIp.getText().toString();
		String serviceName = txtServiceName.getText().toString();
		long updateInterval = txtUpdateInterval.getText().toString().equals("") ? 10000 : 
				Long.parseLong(txtUpdateInterval.getText().toString()) >= 10000 ? 
						Long.parseLong(txtUpdateInterval.getText().toString()) : 10000;
		String vdoPath = txtVDOPath.getText().toString();
		String imgPath = txtImgPath.getText().toString();
				
		Setting setting = new Setting(SettingActivity.this);
		
		if(setting.insertSetting(shopId, serverIp, serviceName, updateInterval, vdoPath, imgPath) == 1){
			new AlertDialog.Builder(SettingActivity.this)
			.setTitle("Setting")
			.setMessage("Save setting succesfully")
			.setNeutralButton("Close", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					gotoMainActivity();
				}
				
			})
			.show();
		}
	}
	
	public void cancelClicked(final View v){
		gotoMainActivity();
	}
	
	private void gotoMainActivity(){
		Intent intent = new Intent(SettingActivity.this, QueueDisplayActivity.class);
		SettingActivity.this.startActivity(intent);

		SettingActivity.this.finish();
	}
}
