package com.android.volley.toolbox;

import android.os.Build;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.Request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

import static okhttp3.internal.http.StatusLine.HTTP_CONTINUE;

/**
 * Created by zhongqing on 26/3/18.
 */

public class OkStack extends BaseHttpStack {


    private static final String UTF8_CHARSET = "UTF-8";


    private String appVersion, connectionType, carrierName;
    private boolean sendTestHeader;
    private OkHttpClient client;
    final static String IMAGE_VIKI_REGEX = "\\d+\\.viki\\.io";
    final Pattern pattern = Pattern.compile(IMAGE_VIKI_REGEX);


    public OkStack(String appVersion, String connectionType, String carrierName, boolean sendTestHeader) {
        this.appVersion = appVersion;
        this.connectionType = connectionType;
        this.carrierName = carrierName;
        this.sendTestHeader = sendTestHeader;
        client = new OkHttpClient();
    }

    public void updateConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    private Map<String, String> getVikiHeaders(int retryCount) {
        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put("X-Viki-app-ver", appVersion);
        additionalHeaders.put("X-Viki-manufacturer", Build.MANUFACTURER);
        additionalHeaders.put("X-Viki-device-model", Build.MODEL);
        additionalHeaders.put("X-Viki-device-os-ver", Build.VERSION.RELEASE);
        additionalHeaders.put("X-Viki-connection-type", connectionType);
        additionalHeaders.put("X-Viki-carrier", carrierName);
        additionalHeaders.put("X-Viki-retries", Integer.toString(retryCount));
        if (sendTestHeader)
            additionalHeaders.put("X-Viki-test", String.valueOf(sendTestHeader));
        return additionalHeaders;
    }

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        String url = getUrlBaseOnMethod(request);
        HashMap<String, String> map = new HashMap<>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        map.putAll(getVikiHeaders(request.getRetryPolicy().getCurrentRetryCount()));

        URL parsedUrl = new URL(url);

        final Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            StringBuilder acceptHeaderStringBuilder = new StringBuilder();
            if (Build.VERSION.SDK_INT >= 18) {
                acceptHeaderStringBuilder.append("image/webp;");
            }

            acceptHeaderStringBuilder.append("image/jpg;");
            acceptHeaderStringBuilder.append("image/png");

            map.put("Accept", acceptHeaderStringBuilder.toString());
        }

        okhttp3.Request.Builder okRequestBuilder = new okhttp3.Request.Builder()
                .url(parsedUrl);
        //add headers
        for (String headerName : map.keySet()) {
            okRequestBuilder.addHeader(headerName, map.get(headerName));
        }

        setConnectionParametersForRequest(okRequestBuilder, request);


        Response response = client.newCall(okRequestBuilder.build()).execute();
        int responseCode = response.code();

        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }

        if (!hasResponseBody(request.getMethod(), responseCode)) {
            return new HttpResponse(responseCode, convertHeaders(response.headers().toMultimap()));
        }

        return new HttpResponse(responseCode, convertHeaders(response.headers().toMultimap()),
                (int)response.body().contentLength(), response.body().byteStream());
    }

    /**
     * Checks if a response message contains a body.
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3">RFC 7230 section 3.3</a>
     * @param requestMethod request method
     * @param responseCode response status code
     * @return whether the response has a body
     */
    private static boolean hasResponseBody(int requestMethod, int responseCode) {
        return requestMethod != Request.Method.HEAD
                && !(HTTP_CONTINUE <= responseCode && responseCode < HttpURLConnection.HTTP_OK)
                && responseCode != HttpURLConnection.HTTP_NO_CONTENT
                && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED;
    }

    /* package */
    static void setConnectionParametersForRequest(okhttp3.Request.Builder builder,
                                                  Request<?> request) throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    builder.post(addBodyIfExists(builder, request));
                }
                break;
            case Request.Method.GET:
                builder.get();
                break;
            case Request.Method.DELETE:
                builder.delete(addBodyIfExists(builder, request));
                break;
            case Request.Method.POST:
                builder.post(addBodyIfExists(builder, request));
                break;
            case Request.Method.PUT:
                builder.put(addBodyIfExists(builder, request));

                break;
            case Request.Method.HEAD:
                builder.head();
                break;
            case Request.Method.PATCH:
                builder.patch(addBodyIfExists(builder, request));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static RequestBody addBodyIfExists(okhttp3.Request.Builder builder, Request<?> request)
            throws IOException, AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            builder.addHeader(HttpHeaderParser.HEADER_CONTENT_TYPE, request.getBodyContentType());
            return RequestBody.create(MediaType.parse(request.getBodyContentType()), body);
        }
        return Util.EMPTY_REQUEST;
    }


    private String getUrlBaseOnMethod(Request<?> request) throws AuthFailureError, IOException {
        String url = request.getUrl();
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
            case Request.Method.GET:
            case Request.Method.DELETE:
            case Request.Method.PUT:
            case Request.Method.HEAD:
                StringBuilder paramBuilder = new StringBuilder();
                if (request.getParams() != null && !request.getParams().isEmpty()) {
                    int count = 0;
                    for (Map.Entry<String, String> parameter : request.getParams().entrySet()) {
                        String key = parameter.getKey();
                        String value = parameter.getValue();
                        if (value == null) {
                            value = "null";
                        }
                        paramBuilder.append(URLEncoder.encode(key, UTF8_CHARSET));
                        paramBuilder.append("=");
                        paramBuilder.append(URLEncoder.encode(value, UTF8_CHARSET));
                        if (count < request.getParams().size() - 1) {
                            paramBuilder.append("&");
                        }
                        count++;
                    }
                }
                if (!TextUtils.isEmpty(paramBuilder.toString())) {
                    if (!url.contains("?")) {
                        url = url + "?" + paramBuilder.toString();
                    } else {
                        url = url + "&" + paramBuilder.toString();
                    }
                }
        }
        return url;
    }


    // VisibleForTesting
    static List<Header> convertHeaders(Map<String, List<String>> responseHeaders) {
        List<Header> headerList = new ArrayList<>(responseHeaders.size());
        for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            // HttpUrlConnection includes the status line as a header with a null key; omit it here
            // since it's not really a header and the rest of Volley assumes non-null keys.
            if (entry.getKey() != null) {
                for (String value : entry.getValue()) {
                    headerList.add(new Header(entry.getKey(), value));
                }
            }
        }
        return headerList;
    }
}
