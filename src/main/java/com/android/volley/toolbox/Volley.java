/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.utils.ConnectionUtils;

import java.io.File;

import okhttp3.OkHttpClient;

public class Volley {

    /** Default on-disk cache directory. */
    private static final String DEFAULT_CACHE_DIR = "volley";

    private static BaseHttpStack httpStack;

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @param network An {@link Network} to use for the network, or null for default.
     * @return A started {@link RequestQueue} instance.
     */
    private static RequestQueue newRequestQueue(Context context, Network network) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        return queue;
    }

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, new OkHttpClient());
    }

    public static RequestQueue newRequestQueue(Context context, OkHttpClient okHttpClient) {
        if (httpStack == null) {
            httpStack = newHttpStack(context, okHttpClient);
        }
        Network network = new BasicNetwork(httpStack);
        return newRequestQueue(context, network);
    }

    public static void updateConnectionType(String connectionType) {
        if (httpStack != null) {
            httpStack.updateConnectionType(connectionType);
        }
    }

    private static BaseHttpStack newHttpStack(Context context, OkHttpClient okHttpClient) {
        String versionName = "";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (NameNotFoundException e) {
        }

        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String carrierName = telephonyManager != null
                ? telephonyManager.getNetworkOperatorName()
                : null;

        String connectionType = ConnectionUtils.getConnectionType(context);

        return new OkStack(versionName, connectionType, carrierName, okHttpClient);
    }

    public static void clearCache(RequestQueue requestQueue) {
        requestQueue.getCache().clear();
    }
}
