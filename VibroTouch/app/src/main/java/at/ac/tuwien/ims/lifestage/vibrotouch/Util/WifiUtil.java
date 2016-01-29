package at.ac.tuwien.ims.lifestage.vibrotouch.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Utility for Wifi connectivity.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class WifiUtil {
    public static String localIPAdress="";

    //returns local ip address
    public static String getIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressValidator.getInstance().isValidInet4Address(sAddr);
                        if (isIPv4) {
                            localIPAdress=sAddr;
                            return sAddr;
                        }
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
