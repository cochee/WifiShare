package com.vallny.wifi;

import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

public class WifiAdmin {
	// 定义一个WifiManager对象
	private WifiManager mWifiManager;
	// 定义一个WifiInfo对象
	private WifiInfo mWifiInfo;
	// 扫描出的网络连接列表
	private List<ScanResult> mWifiList;
	// 网络连接列表
	private List<WifiConfiguration> mWifiConfigurations;
	WifiLock mWifiLock;

	private static WifiAdmin wifiAdmin;

	public static final int WIFI_AP_STATE_DISABLING = 10;
	public static final int WIFI_AP_STATE_DISABLED = 11;
	public static final int WIFI_AP_STATE_ENABLING = 12;
	public static final int WIFI_AP_STATE_ENABLED = 13;
	public static final int WIFI_AP_STATE_FAILED = 14;

	public static WifiAdmin getInstance(Context context) {
		if (wifiAdmin == null) {
			wifiAdmin = new WifiAdmin(context);
		}
		return wifiAdmin;
	}

	private WifiAdmin(Context context) {
		// 取得WifiManager对象
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		// 取得WifiInfo对象
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	// 打开wifi
	public void openWifi() {
		if (checkState() != WifiManager.WIFI_STATE_ENABLED && checkState() != WifiManager.WIFI_STATE_ENABLING) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	// 关闭wifi
	public void closeWifi() {
		if (checkState() != WifiManager.WIFI_STATE_DISABLED && checkState() != WifiManager.WIFI_STATE_DISABLING) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	// 检查当前wifi状态
	public int checkState() {
		return mWifiManager.getWifiState();
	}

	// 检查当前AP状态
	public int checkApState() {
		try {
			Class localClass = this.mWifiManager.getClass();
			Method method = localClass.getMethod("getWifiApState", null);
			return (Integer) method.invoke(this.mWifiManager, null);
		} catch (Exception e) {
			return 0;
		}
	}

	// 锁定wifiLock
	public void acquireWifiLock() {
		mWifiLock.acquire();
	}

	// 解锁wifiLock
	public void releaseWifiLock() {
		// 判断是否锁定
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	// 创建一个wifiLock
	public void createWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("test");
	}

	// 得到配置好的网络
	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfigurations;
	}

	// 指定配置好的网络进行连接
	public void connetionConfiguration(int index) {
		if (index > mWifiConfigurations.size()) {
			return;
		}
		// 连接配置好指定ID的网络
		mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId, true);
	}

	public void startScan() {
		mWifiManager.startScan();
		// 得到扫描结果
		mWifiList = mWifiManager.getScanResults();
		// 得到配置好的网络连接
		mWifiConfigurations = mWifiManager.getConfiguredNetworks();
	}

	// 得到网络列表
	public List<ScanResult> getWifiList() {
		return mWifiList;
	}

	// 查看扫描结果
	public StringBuffer lookUpScan() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mWifiList.size(); i++) {
			sb.append("Index_" + new Integer(i + 1).toString() + ":");
			// 将ScanResult信息转换成一个字符串包
			// 其中把包括：BSSID、SSID、capabilities、frequency、level
			sb.append((mWifiList.get(i)).toString()).append("\n");
		}
		return sb;
	}

	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	public String getSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	public int getIpAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// 得到连接的ID
	public int getNetWordId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// 得到wifiInfo的所有信息
	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	// 添加一个网络并连接
	public void addNetWork(WifiConfiguration configuration) {
		int wcgId = mWifiManager.addNetwork(configuration);
		boolean isSuccess = mWifiManager.enableNetwork(wcgId, true);
		Log.d("yzy", "isSuccess=" + isSuccess);

	}

	// 断开指定ID的网络
	public void disConnectionWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}

	/**
	 * 配置wifi
	 * 
	 * @param SSID
	 * @param Password
	 * @param Type
	 * @return
	 */
	public WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = SSID;

		// WifiConfiguration tempConfig = isExsits(SSID, mWifiManager);
		// if (tempConfig != null) {
		// mWifiManager.removeNetwork(tempConfig.networkId);
		// }

		if (Type == 1) // WIFICIPHER_NOPASS
		{
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 2) // WIFICIPHER_WEP
		{
			config.hiddenSSID = true;
			config.wepKeys[0] = Password;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 3) // WIFICIPHER_WPA
		{
			config.preSharedKey = Password;
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}

	/**
	 * 判断wifi是否存在
	 * 
	 * @param SSID
	 * @param wifiManager
	 * @return
	 */
	private static WifiConfiguration isExsits(String SSID, WifiManager wifiManager) {
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals(SSID)) {
				return existingConfig;
			}
		}
		return null;
	}

	public void createWiFiAP(WifiConfiguration paramWifiConfiguration, boolean paramBoolean) {
		try {
			closeWifi();
			if (!(checkApState() == WIFI_AP_STATE_ENABLED || checkApState() == WIFI_AP_STATE_ENABLING)) {
				Class localClass = this.mWifiManager.getClass();
				Class[] arrayOfClass = new Class[2];
				arrayOfClass[0] = WifiConfiguration.class;
				arrayOfClass[1] = Boolean.TYPE;
				Method localMethod = localClass.getMethod("setWifiApEnabled", arrayOfClass);
				WifiManager localWifiManager = this.mWifiManager;
				Object[] arrayOfObject = new Object[2];
				arrayOfObject[0] = paramWifiConfiguration;
				arrayOfObject[1] = Boolean.valueOf(paramBoolean);
				localMethod.invoke(localWifiManager, arrayOfObject);
				return;
			}
		} catch (Exception localException) {
		}
	}
}