package com.icanvass.helpers;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.ContentHandler;

/**
 * Created by romek on 19.10.2014.
 */
public class FanOutHelper {
    static final String url = "";

    static void send(Context ctx, String keyword) {
        Intent intent = new Intent();
        intent.setAction("com.spotio.fanout");
        intent.putExtra("what",keyword);
        ctx.sendBroadcast(intent);
    }

    public static WebView getWebView(final Context ctx) {
        WebView webView = new WebView(ctx);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setTag("FanOut");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                String u=urlNewString.toLowerCase();
                if (urlNewString.startsWith("pin")) {
                    send(ctx,"pin");
                    return true;
                } else if (urlNewString.startsWith("settings_user_added")) {
                    send(ctx,"users");
                    return true;
                } else if (urlNewString.startsWith("settings_role_changed")) {
                    send(ctx,"users");
                    return true;
                } else if (urlNewString.startsWith("settings_statuses_changed")) {
                    send(ctx,"statuses");
                    return true;
                } else if (urlNewString.startsWith("settings_questions_changed")) {
                    send(ctx,"fields");
                    return true;
                } else if (urlNewString.startsWith("settings_permissions_changed")) {
                    send(ctx,"users");
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }
        });
        webView.setVisibility(View.GONE);
        String c=SPHelper.getInstance().getSharer().getString("COMPANY",null);
        webView.loadUrl("file:///android_asset/fanout.html?r=3f449354&c="+c);
        return webView;
    }
}
