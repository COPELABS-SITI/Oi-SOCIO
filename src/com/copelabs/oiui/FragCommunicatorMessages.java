package com.copelabs.oiui;

import com.copelabs.oiaidllibrary.UserDevice;

public interface FragCommunicatorMessages {
	public void passDataToFragment(UserDevice mSource, UserDevice mDestination, String mMessage);
}
