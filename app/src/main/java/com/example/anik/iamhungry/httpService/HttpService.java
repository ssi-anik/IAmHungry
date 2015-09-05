package com.example.anik.iamhungry.httpService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.example.anik.iamhungry.helpers.CustomProgressDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anik on 03-Sep-15, 003.
 */
public class HttpService {

    public static final int HTTP_GET = 1;
    public static final int HTTP_POST = 2;
    public static final int HTTP_PUT = 3;

    private String url = "";
    private int method = 0;
    private List<String> form_data_keys = new ArrayList<String>();
    private List<String> form_data_values = new ArrayList<String>();
    private Class intentClass = null;
    private IHttpService registered_listener = null;
    private boolean showProgressDialog = true;
    private boolean isCancelableProgressDialog = false;
    private String json = "";
    private int status_code = 0;
    private boolean isFile = false;
    private Map<String, String> extraValues = new HashMap<>();

    private Activity activity;
    private Context context;
    private ProgressDialog progressDialog;
    private DefaultHttpClient httpClient;
    private HttpGet httpGet;
    private HttpPost httpPost;
    private HttpPut httpPut;


    public HttpService(Activity activity) {
        this.activity = activity;
        this.context = context;
    }

    public HttpService(Context context) {
        this.context = context;
    }

    public HttpService onUrl(String url) {
        this.url = url.replace("/?", "");
        return this;
    }

    public HttpService onUrl(String url, String append_on_replace) {
        this.url = url.replace("?", append_on_replace);
        return this;
    }

    public HttpService withMethod(int httpMethod) throws RuntimeException {
        if (httpMethod != HTTP_GET && httpMethod != HTTP_POST && httpMethod != HTTP_PUT) {
            throw new RuntimeException("Not a valid http verb");
        }
        this.method = httpMethod;
        return this;
    }

    public HttpService withData(List<String> keys, List<String> values) throws RuntimeException {
        if (keys.size() != values.size())
            throw new RuntimeException("Key value pair doesn't match");
        form_data_keys.addAll(keys);
        form_data_values.addAll(values);
        return this;
    }

    public HttpService nextIntent(Class next_intent_class) {
        this.intentClass = next_intent_class;
        return this;
    }

    public HttpService registerResponse(IHttpService register_listener) {
        this.registered_listener = register_listener;
        return this;
    }

    public HttpService putExtraForIntent(Map<String, String> extraValues) {
        this.extraValues.putAll(extraValues);
        return this;
    }

    public HttpService hideProgressDialog() {
        this.showProgressDialog = false;
        return this;
    }

    public HttpService cancelableProgressDialog() {
        this.isCancelableProgressDialog = true;
        return this;
    }

    public void execute() {
        httpClient = new DefaultHttpClient();
        if (method == this.HTTP_POST) {
            httpPost = new HttpPost(this.url);
            if (form_data_keys.size() > 0) {
                List<NameValuePair> formData = new ArrayList<>();
                for (int i = 0; i < form_data_keys.size(); ++i) {
                    formData.add(new BasicNameValuePair(form_data_keys.get(i), form_data_values.get(i)));
                }
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(formData));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            new RequestPost().execute();
        } else if (method == this.HTTP_PUT) {
            httpPut = new HttpPut(this.url);
            if (form_data_keys.size() > 0) {
                List<NameValuePair> formData = new ArrayList<NameValuePair>();
                for (int i = 0; i < form_data_keys.size(); ++i) {
                    formData.add(new BasicNameValuePair(form_data_keys.get(i), form_data_values.get(i)));
                }
                try {
                    httpPut.setEntity(new UrlEncodedFormEntity(formData));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            new RequestPut().execute();
        } else {
            String request_url = "";
            if (form_data_keys.size() > 0) {
                List<NameValuePair> formData = new ArrayList<NameValuePair>();
                for (int i = 0; i < form_data_keys.size(); ++i) {
                    formData.add(new BasicNameValuePair(form_data_keys.get(i), form_data_values.get(i)));
                }
                request_url = this.url + "?" + URLEncodedUtils.format(formData, "utf-8");
            } else {
                request_url = this.url;
            }
            httpGet = new HttpGet(request_url);
            new RequestGet().execute();
        }
    }

    class RequestGet extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (HttpService.this.showProgressDialog == false) {
                return;
            }

            progressDialog = CustomProgressDialog.builder(HttpService.this.activity);
            progressDialog.setCancelable(HttpService.this.isCancelableProgressDialog);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpResponse response = httpClient.execute(HttpService.this.httpGet);
                StatusLine statusLine = response.getStatusLine();
                HttpService.this.status_code = statusLine.getStatusCode();
                HttpEntity entity = response.getEntity();
                HttpService.this.json = EntityUtils.toString(entity);
                if (status_code < HttpStatus.SC_BAD_REQUEST) {
                    return true;
                } else {
                    return false;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (null != progressDialog && progressDialog.isShowing())
                progressDialog.dismiss();
            if (registered_listener != null) {
                HttpService.this.registered_listener.onResponseReceived(status_code, HttpService.this.json);
            } else if (intentClass != null) {
                Intent intent = new Intent(HttpService.this.activity, HttpService.this.intentClass);
                intent.putExtra("statusCode", status_code);
                intent.putExtra("response", HttpService.this.json);
                if (HttpService.this.extraValues.size() > 0) {
                    for (Map.Entry<String, String> values : extraValues.entrySet()) {
                        intent.putExtra(values.getKey(), values.getValue());
                    }
                }
                HttpService.this.activity.startActivity(intent);
            }
        }
    }

    class RequestPost extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (HttpService.this.showProgressDialog == false) {
                return;
            }

            progressDialog = CustomProgressDialog.builder(HttpService.this.activity);
            progressDialog.setCancelable(HttpService.this.isCancelableProgressDialog);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpResponse response = httpClient.execute(HttpService.this.httpPost);
                StatusLine statusLine = response.getStatusLine();
                HttpService.this.status_code = statusLine.getStatusCode();
                HttpEntity entity = response.getEntity();
                HttpService.this.json = EntityUtils.toString(entity);
                if (status_code < HttpStatus.SC_BAD_REQUEST) {
                    return true;
                } else {
                    return false;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (null != progressDialog && progressDialog.isShowing())
                progressDialog.dismiss();
            if (registered_listener != null) {
                HttpService.this.registered_listener.onResponseReceived(status_code, HttpService.this.json);
            } else if (intentClass != null) {
                Intent intent = new Intent(HttpService.this.activity, HttpService.this.intentClass);
                intent.putExtra("statusCode", status_code);
                intent.putExtra("response", HttpService.this.json);
                if (HttpService.this.extraValues.size() > 0) {
                    for (Map.Entry<String, String> values : extraValues.entrySet()) {
                        intent.putExtra(values.getKey(), values.getValue());
                    }
                }
                HttpService.this.activity.startActivity(intent);
            }
        }
    }

    class RequestPut extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (HttpService.this.showProgressDialog == false) {
                return;
            }

            progressDialog = CustomProgressDialog.builder(HttpService.this.activity);
            progressDialog.setCancelable(HttpService.this.isCancelableProgressDialog);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpResponse response = httpClient.execute(HttpService.this.httpPut);
                StatusLine statusLine = response.getStatusLine();
                HttpService.this.status_code = statusLine.getStatusCode();
                HttpEntity entity = response.getEntity();
                HttpService.this.json = EntityUtils.toString(entity);
                if (status_code < HttpStatus.SC_BAD_REQUEST) {
                    return true;
                } else {
                    return false;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (null != progressDialog && progressDialog.isShowing())
                progressDialog.dismiss();
            if (registered_listener != null) {
                HttpService.this.registered_listener.onResponseReceived(status_code, HttpService.this.json);
            } else if (intentClass != null) {
                Intent intent = new Intent(HttpService.this.activity, HttpService.this.intentClass);
                intent.putExtra("statusCode", status_code);
                intent.putExtra("response", HttpService.this.json);
                if (HttpService.this.extraValues.size() > 0) {
                    for (Map.Entry<String, String> values : extraValues.entrySet()) {
                        intent.putExtra(values.getKey(), values.getValue());
                    }
                }
                HttpService.this.activity.startActivity(intent);
            }
        }
    }

}
