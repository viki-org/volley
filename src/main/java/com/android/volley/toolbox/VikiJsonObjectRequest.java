package com.android.volley.toolbox;

import com.android.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by saurabharora on 29/3/17.
 */

public class VikiJsonObjectRequest extends JsonObjectRequest {

    private Map<String, String> mHeaders = new HashMap<>();
    private Map<String, String> mParams = new HashMap<>();

    private VikiJsonObjectRequest(int method, String url,
                                 Map<String, String> headers, Map<String, String> params, JSONObject jsonRequest,
                                 Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);

        mParams = params;
        this.mHeaders = headers;
    }

    private VikiJsonObjectRequest(Builder builder) {
        this(builder.method, builder.url, builder.headers, builder.params, builder.jsonRequest, builder.listener, builder.errorListener);
    }

    @Override
    public Map<String, String> getParams(){
        return mParams;
    }

    @Override
    public Map<String, String> getHeaders(){
        return mHeaders;
    }

    public static final class Builder {
        private int method;
        private String url;
        JSONObject jsonRequest;
        private Response.Listener<JSONObject> listener;
        private Response.ErrorListener errorListener;
        private Map<String, String> params, headers;

        public Builder(int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
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

        public Builder withRequestBody(JSONObject val) {
            jsonRequest = val;
            return this;
        }

        public Builder withParams(Map<String, String> val) {
            params = val;
            return this;
        }

        public VikiJsonObjectRequest build() {
            return new VikiJsonObjectRequest(this);
        }
    }
}