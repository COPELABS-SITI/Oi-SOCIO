package com.copelabs.oiui;

import com.copelabs.oiaidllibrary.UserDevice;

public interface FragCommunicatorDevices {
	public void newDeviceInfo(UserDevice mDevice);
	public void lostDevice(UserDevice mDevice);
	public void showError(int mError);
}
