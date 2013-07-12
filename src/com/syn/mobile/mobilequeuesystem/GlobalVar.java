package com.syn.mobile.mobilequeuesystem;

import java.util.HashMap;

import android.content.Context;

public class GlobalVar {
	private final String service = "ws_mpos.asmx";
	
	private String serviceUrl; 
	private int shopId;
	private String serverIp;
	private String serviceName;
	private Long refreshTime;
	private String vdoPath;
	private String imgPath;
	
	public GlobalVar(Context context){
		Setting setting = new Setting(context);
		HashMap<String, String> set = setting.getSetting();
		
		shopId = Integer.parseInt(set.get("shopid") == null ? "0" : set.get("shopid"));
		serverIp = set.get("serverip");
		serviceName = set.get("servicename");
		refreshTime = Long.parseLong(set.get("updateinterval") == null ? "10000" : set.get("updateinterval"));
		vdoPath = set.get("vdopath");
		imgPath = set.get("imgpath");

		serviceUrl = "http://" + serverIp + "/" + serviceName + "/" + service;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public int getShopId() {
		return shopId;
	}

	public String getServerIp() {
		return serverIp;
	}

	public String getServiceName() {
		return serviceName;
	}

	public Long getRefreshTime() {
		return refreshTime;
	}

	public String getVdoPath() {
		return vdoPath;
	}

	public String getImgPath() {
		return imgPath;
	}
}
