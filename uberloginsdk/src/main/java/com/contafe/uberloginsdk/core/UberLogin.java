package com.contafe.uberloginsdk.core;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.contafe.uberloginsdk.R;
import com.contafe.uberloginsdk.utilities.ConnectionDetector;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Natig on 2/6/17.
 */

public abstract class UberLogin {
    private Dialog authDialog;
    private WebView authWebView;
    private String clientId, clientSecret, serverToken;
    private Context context;
    private ConnectionDetector connectionDetector;
    private ProgressBar progressBar;
    private RequestParams requestParams;
    private AsyncHttpClient asyncHttpClient;

    private String AUTH_URL;
    private String TOKEN_URL = "https://login.uber.com/oauth/v2/token";

    public UberLogin(Context context) {
        // setting context
        this.context = context;
        // initializing connectionDetector
        connectionDetector = new ConnectionDetector(context);
        // initializing request params
        requestParams = new RequestParams();
        // initializing asyncHttpClient
        asyncHttpClient = new AsyncHttpClient();
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
        this.AUTH_URL = "https://login.uber.com/oauth/v2/authorize?client_id=" + clientId + "&response_type=code";
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setServerToken(String serverToken) {
        this.serverToken = serverToken;
    }

    public void start() {
        //checking for internet connection
        if (connectionDetector.isConnectiongToInternet()) {
            //initializing dialog object
            authDialog = new Dialog(context);
            // initializing resource file of dialog
            authDialog.setContentView(R.layout.dialog_auth);
            // initializing webview of dialog
            authWebView = (WebView) authDialog.findViewById(R.id.webv);
            // initializing progressbar
            progressBar = (ProgressBar) authDialog.findViewById(R.id.authProgressBar);
            // showing progressbar
            progressBar.setVisibility(View.VISIBLE);
            // hiding webview
            authWebView.setVisibility(View.GONE);
            //enabling javascript
            authWebView.getSettings().setJavaScriptEnabled(true);
            //loading url
            authWebView.loadUrl(AUTH_URL);
            //customizing webview actions
            authWebView.setWebViewClient(new WebViewClient() {
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    Log.d("Uber Login", url);
                    //variable for storing auth code
                    String authCode;
                    //checking if redirect url contains the word '?code', it'll get authorization code
                    if (url.contains("?code=") || url.contains("&code=")) {
                        // now we're parsing our url string to Uri
                        Uri uri = Uri.parse(url);
                        // getting auth code from url
                        authCode = uri.getQueryParameter("code");
                        // posting oath code to get auth data
                        getToken(authCode);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    Log.d("Uber Login", url);
                    if (url.contains("?code=") || url.contains("&code=")) {
                        // hiding webview
                        authWebView.setVisibility(View.GONE);
                        // showing progressbar
                        progressBar.setVisibility(View.VISIBLE);
                    } else if (url.contains("?error=")) {
                        // now we're parsing our url string to Uri
                        Uri uri = Uri.parse(url);
                        // passing error
                        onGetError(uri.getQueryParameter("error"));
                        // hiding webview
                        authWebView.setVisibility(View.GONE);
                        // showing progressbar
                        progressBar.setVisibility(View.VISIBLE);
                        // dismissing dialog
                        authDialog.dismiss();
                    } else {
                        // scrolling to top of the page
                        view.scrollTo(0, 0);
                        // hiding progressbar
                        progressBar.setVisibility(View.GONE);
                        // showing webview
                        authWebView.setVisibility(View.VISIBLE);
                    }
                }
            });
            // showing auth dialog
            authDialog.show();
            // getting window of auth dialog
            Window window = authDialog.getWindow();
            // resizing authDialog's size
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            onGetError("No internet connection. That's the moment for using NIC (www.nicapp.me). ");
        }
    }

    protected abstract void onGetToken(String response);

    protected abstract void onGetError(String response);

    private void getToken(String code) {
        /*
         *   curl -F 'client_secret=YOUR_CLIENT_SECRET' \
         *   -F 'client_id=YOUR_CLIENT_ID' \
         *   -F 'grant_type=authorization_code' \
         *   -F 'redirect_uri=YOUR_REDIRECT_URI' \
         *   -F 'code=AUTHORIZATION_CODE_FROM_STEP_2' \
         *   https://login.uber.com/oauth/v2/token
         */
        // adding parameters to post
        requestParams.put("client_id", clientId);
        requestParams.put("client_secret", clientSecret);
        requestParams.put("grant_type", "authorization_code");
        requestParams.put("redirect_uri", "http://localhost");
        requestParams.put("code", code);
        // posting request params to Uber's server
        asyncHttpClient.post(TOKEN_URL, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // creating string from byte
                String response = new String(responseBody);
                // sending JSON string
                onGetToken(response);
                // hiding progressbar
                progressBar.setVisibility(View.GONE);
                // showing webview
                authWebView.setVisibility(View.VISIBLE);
                // dismissing auth dialog
                authDialog.dismiss();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // creating string from byte
                String response = new String(responseBody);
                // sending JSON string
                onGetError(response);
                // hiding progressbar
                progressBar.setVisibility(View.GONE);
                // showing webview
                authWebView.setVisibility(View.VISIBLE);
                // dismissing auth dialog
                authDialog.dismiss();
            }
        });
    }
}

