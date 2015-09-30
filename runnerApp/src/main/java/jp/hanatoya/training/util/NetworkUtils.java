package jp.hanatoya.training.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtils {
public static final String KEY_IP3 = "KEY_IP3";
public static final String KEY_FIRST_DIGITS = "KEY_FIRST_DIGITS";

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static boolean isLocalNetwork(String ip){
    	return ip.startsWith("192");
    }

    public static String getLastDigits(String ip){
    	String[] els = ip.split("[.]");   	
    	return els[els.length-1];
    }

	public static String getFirstDigits(String ip){
		return ip.substring(0, ip.lastIndexOf("."));
	}

    @SuppressLint("DefaultLocale")
	public static String getFullIp(String last3digits, Context context){
    	WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    	int i = wifiInfo.getIpAddress();
    	String ip =  String.format( 
    		    "%d.%d.%d.%d", 
    		    (i & 0xff), 
    		    (i >> 8 & 0xff),
    		    (i >> 16 & 0xff),
    		    (i >> 24 & 0xff));

    	String[] els = ip.split("[.]");   	
    	els[els.length-1] = last3digits;
    	
    	StringBuilder sb = new StringBuilder();
    	for (String el : els){
    		sb.append(el);
    		sb.append(".");
    	}
    	
    	String res = sb.toString();
    	res = res.substring(0, res.length() - 1);
    	
    	
    	return res;
    }
    
}
