package at.ac.tuwien.mmi.gyropowerball;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WifiUtil {

    //returns local ip address 
    public static String getIPAddress() {
	try {
	    List<NetworkInterface> interfaces = Collections.list(NetworkInterface
		    .getNetworkInterfaces());
	    for (NetworkInterface intf : interfaces) {
		List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
		for (InetAddress addr : addrs) {
		    if (!addr.isLoopbackAddress()) {
			String sAddr = addr.getHostAddress().toUpperCase();
			boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
			if (isIPv4)
			    return sAddr;
		    }
		}
	    }
	} catch (Exception ex) {
	} // for now eat exceptions
	return "";
    }

    //returns true if phone is connected to the Internet
    public static boolean isOnline(Context context) {
	ConnectivityManager cm = (ConnectivityManager) context
		.getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo netInfo = cm.getActiveNetworkInfo();
	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	    return true;
	}
	return false;
    }

}
