package pl.qprogramming.shopper.watch.util;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.Properties;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class HttpUtil {


    public static <T> void post(Context context, String url, T body, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        post(context, url, body, listener, errorListener, null);
    }

    public static <T> void post(Context context, String url, T body, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener, @Nullable Integer timeout) {
        request(Request.Method.POST, context, url, body, listener, errorListener, timeout);
    }

    public static <T> void postArray(Context context, String url, T body, Response.Listener<JSONArray> listener, @Nullable Response.ErrorListener errorListener, @Nullable Integer timeout) {
        requestArray(Request.Method.POST, context, url, body, listener, errorListener, timeout);
    }

    public static <T> void postArray(Context context, String url, T body, Response.Listener<JSONArray> listener, @Nullable Response.ErrorListener errorListener) {
        requestArray(Request.Method.POST, context, url, body, listener, errorListener, null);
    }

    public static void get(Context context, String url, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener, @Nullable Integer timeout) {
        request(Request.Method.GET, context, url, null, listener, errorListener, timeout);
    }

    public static void get(Context context, String url, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        request(Request.Method.GET, context, url, null, listener, errorListener, null);
    }

    public static void getArray(Context context, String url, Response.Listener<JSONArray> listener, @Nullable Response.ErrorListener errorListener) {
        requestArray(Request.Method.GET, context, url, null, listener, errorListener, null);
    }

    public static void getArray(Context context, String url, Response.Listener<JSONArray> listener, @Nullable Response.ErrorListener errorListener, @Nullable Integer timeout) {
        requestArray(Request.Method.GET, context, url, null, listener, errorListener, timeout);
    }

    @SneakyThrows
    public static <T> void request(int method, Context context, String url, T body, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener, @Nullable Integer timeout) {
        val queue = Volley.newRequestQueue(context);
        val targetUrl = context.getString(R.string.base_url) + url;
        String strBody;
        if (body != null) {
            strBody = new Gson().toJson(body);
        } else {
            strBody = "{}";
        }
        val jsonRequest = new JSONObject(strBody);
        val postRequest = new JsonObjectRequest(method, targetUrl, jsonRequest, listener, errorListener) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                return addAuthToParams(context, super.getParams());
            }
        };
        if (timeout != null) {
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    timeout,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }
        queue.add(postRequest);
    }


    @SneakyThrows
    public static <T> void requestArray(int method, Context context, String url, T body, Response.Listener<JSONArray> listener, @Nullable Response.ErrorListener errorListener, @Nullable Integer timeout) {
        val queue = Volley.newRequestQueue(context);
        val targetUrl = context.getString(R.string.base_url) + url;
        String strBody;
        if (body != null) {
            strBody = new Gson().toJson(body);
        } else {
            strBody = "[]";
        }
        val jsonRequest = new JSONArray(strBody);
        val postRequest = new JsonArrayRequest(method, targetUrl, jsonRequest, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return addAuthToParams(context, super.getParams());
            }
        };
        if (timeout != null) {
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    timeout,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }
        queue.add(postRequest);
    }

    private static Map<String, String> addAuthToParams(Context context, Map<String, String> params) {
        val sp = getDefaultSharedPreferences(context);
        val email = sp.getString(Properties.EMAIL, null);
        val token = sp.getString(Properties.TOKEN, null);
        if (params == null) {
            params = new HashMap<>();
        }
        if (email != null && token != null) {
            String credentials = email + ":" + token;
            String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            params.put("Authorization", auth);
        }
        return params;
    }

}
