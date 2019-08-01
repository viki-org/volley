package com.android.volley.toolbox;

import com.android.volley.Response;
import com.android.volley.VolleyLog;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by saurabharora on 29/3/17.
 */

public class VikiStringRequest extends StringRequest {

    /** Charset for request. */
    private static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private Map<String, String> mHeaders = new HashMap<>();
    private Map<String, String> mParams = new HashMap<>();
    private String mRequestBody;

    private VikiStringRequest(int method, String url,
                              Map<String, String> headers, Map<String, String> params, String requestBody,
                              Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);

        mHeaders = headers;
        mParams = params;
        mRequestBody = (requestBody == null) ? null : requestBody;
    }

    private VikiStringRequest(Builder builder) {
        this(builder.method, builder.url, builder.headers, builder.params, builder.requestBody, builder.listener, builder.errorListener);
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
    public String getPostBodyContentType() {
        return getBodyContentType();
    }

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
    public String getCacheKey() {
        StringBuilder cacheKeyBuilder = new StringBuilder(getUrl());
        for (Map.Entry<String, String> entry: mParams.entrySet()){
            cacheKeyBuilder.append(entry.getKey());
            cacheKeyBuilder.append(entry.getValue());
        }
        return cacheKeyBuilder.toString();
    }

    public static final class Builder {
        private int method;
        private String url, requestBody;
        private Response.Listener<String> listener;
        private Response.ErrorListener errorListener;
        private Map<String, String> params, headers;

        public Builder(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            this.method = method;
            this.url = url;
            this.listener = listener;
            this.errorListener = errorListener;
            this.params = new HashMap<>();
            this.headers = new HashMap<>();
        }

        public Builder withAdditionalHeaders(Map<String, String> additionalHeaders) {
            if(additionalHeaders!=null) {
                for (String key : additionalHeaders.keySet()) {
                    headers.put(key, additionalHeaders.get(key));
                }
            }
            return this;
        }

        public Builder withRequestBody(String val) {
            requestBody = val;
            return this;
        }

        public Builder withParams(Map<String, String> val) {
            params = val;
            return this;
        }

        public VikiStringRequest build() {
            return new VikiStringRequest(this);
        }
    }
}