package com.android.volley.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by saurabharora on 9/4/17.
 */

public class ConnectionUtils {

    public static String getConnectionType( Context context )
    {
        ConnectivityManager mConnectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Skip if no connection, or background data disabled
        if(mConnectivity == null)
            return "UNKNOWN";

        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info != null )
        {
            // Only update if WiFi or 3G is connected and not roaming
            int netType = info.getType();
            int netSubtype = info.getSubtype();
            if (netType == ConnectivityManager.TYPE_WIFI)
            {
                return "WIFI";
            }
            else if ( netType == ConnectivityManager.TYPE_MOBILE )
            {
                switch( netSubtype )
                {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        return "1xRTT"; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        return "CDMA"; // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return "EDGE"; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        return "EVDO_0"; // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        return "EVDO_A"; // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        return "GPRS"; // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        return "HSDPA"; // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        return "HSPA"; // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        return "HSUPA"; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        return "UMTS"; // ~ 400-7000 kbps
		            /*
		             * Above API level 7, make sure to set android:targetSdkVersion
		             * to appropriate level to use these
		             */
                    case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                        return "EHRPD"; // ~ 1-2 Mbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                        return "EVDO_B"; // ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                        return "HSPAP"; // ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                        return "IDEN"; // ~25 kbps
                    case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                        return "LTE"; // ~ 10+ Mbps
                    // Unknown
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        return "UNKNOWN";
                    default:
                        return netSubtype + "";
                }
            }
            else if( netType == ConnectivityManager.TYPE_ETHERNET ){
                return "ETHERNET";
            }
        }
        return "UNKNOWN";
    }

    public static boolean hasNetWorkConnection(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
            return false;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isInWifiMode(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager == null)
            return false;
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi != null && mWifi.isConnected();
    }

    public static boolean isInEthernetMode(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager == null)
            return false;
        NetworkInfo mEtherent = connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        return mEtherent != null && mEtherent.isConnected();
    }

    public static boolean isNetworkMetered(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager == null)
            return false;
        // For TV, its possible to use Ethernet. Therefore, one of judgements whether play HD should rely on user need to pay for connection.
        // That's why we use network metered to judge. If this method return false, it means data has no limitations.
        // A network is classified as metered when the user is sensitive to heavy data usage on that connection due to monetary costs, data limitations or battery/performance issues.
        return !connManager.isActiveNetworkMetered();
    }
}
