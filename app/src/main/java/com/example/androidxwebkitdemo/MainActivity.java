package com.example.androidxwebkitdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    SwipeRefreshLayout mSwipeRefreshLayout;
    WebView mWebView ;
    TextView mNameTV;
    TextView mEmailTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNameTV = findViewById(R.id.nameTV);
        mEmailTV = findViewById(R.id.emailTV);

        // Firebase data
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (signInAccount != null){
            mNameTV.setText(signInAccount.getDisplayName());
            mEmailTV.setText(signInAccount.getEmail());
        }


        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mWebView = findViewById(R.id.webView);

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);

//        mWebView.loadUrl("http://gauravthakur.me/");
        String url = "<html> <body> <p>This is a paragraph.</p> <p>This is another paragraph.</p> <p><a href=\"https://www.google.com/\">Google</a></p> </body> </html>";
        String loadURL = android.util.Base64.encodeToString(url.getBytes(), android.util.Base64.NO_PADDING);
        mWebView.loadData(loadURL, "text/html", "base64");

        mWebView.setWebViewClient(new LocalBrowser());

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this, "on it",Toast.LENGTH_SHORT).show();
                mWebView.reload();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }
    public class LocalBrowser extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
            String url = request.getUrl().toString();
            view.loadUrl(url);
            return true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }
}