package com.android.volley.toolbox;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class XWWWFormStringRequest extends Request<String> {
    /** Charset for request. */
    private static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = String.format("application/x-www-form-urlencoded");

    private final Listener<String> mListener;
    private Map<String, String> mHeaders = new HashMap<>();
    private Map<String, String> mParams = new HashMap<>();
    private String mRequestBody;

    /**
     * Creates a new request with the given method.
     *
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public XWWWFormStringRequest(int method, String url, Listener<String> listener,
                                 ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    /**
     * Creates a new request with the given method.
     *
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param headers Headers of the request
     * @param params Parameters of the request
     * @param errorListener Error listener, or null to ignore errors
     */
    public XWWWFormStringRequest(int method, String url, Map<String, String> headers, Map<String, String> params, String requestBody, Listener<String> listener, ErrorListener errorListener) {
        this(method, url, listener, errorListener);
        mHeaders = headers;
        mParams = params;
        mRequestBody = (requestBody == null) ? null : requestBody;
    }

    @Override
    public Map<String, String> getParams(){
        return mParams;
    }

    @Override
    public Map<String, String> getHeaders(){
        return mHeaders;
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    /**
     * @deprecated Use {@link #getBodyContentType()}.
     */
    @Override
    public String getPostBodyContentType() {
        return getBodyContentType();
    }

    /**
     * @deprecated Use {@link #getBody()}.
     */
    @Override
    public byte[] getPostBody() {
        return getBody();
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public String getCacheKey() {
        String cacheKey = getUrl();
        for (Map.Entry<String, String> entry: mParams.entrySet()){
            cacheKey += entry.getKey() + entry.getValue();
        }
        return cacheKey;
    }
}
