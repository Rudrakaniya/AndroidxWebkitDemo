package com.example.androidxwebkitdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String LOGCAT_TAG = "Androidx Webkit Demo";

    SwipeRefreshLayout mSwipeRefreshLayout;
    WebView mWebView;
    TextView mNameTV;
    TextView mEmailTV;
    EditText mEditText;
    Button mButton;

    // Firebase
    private FirebaseAuth mAuth;

    // Image Slider Using https://github.com/smarteist/Android-Image-Slider
    SliderView sliderView;
    private SliderAdapterExample adapter;
    private int SLIDER_COUNT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        // Image Slider
//        sliderView = findViewById(R.id.imageSlider);
//
//
//        adapter = new SliderAdapterExample(this);
//        sliderView.setSliderAdapter(adapter);
//        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
//        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
//        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
//        sliderView.setIndicatorSelectedColor(Color.WHITE);
//        sliderView.setIndicatorUnselectedColor(Color.GRAY);
//        sliderView.setScrollTimeInSec(3);
//        sliderView.setAutoCycle(true);
//        sliderView.startAutoCycle();
//
//        sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
//            @Override
//            public void onIndicatorClicked(int position) {
//                Log.i("GGG", "onIndicatorClicked: " + sliderView.getCurrentPagePosition());
//            }
//        });
//
//        renewItems();


        mNameTV = findViewById(R.id.nameTV);
        mEmailTV = findViewById(R.id.emailTV);
        mEditText = findViewById(R.id.locationET);
        mButton = findViewById(R.id.submitBtn);

        // Firebase data
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (signInAccount != null) {
            mNameTV.setText(signInAccount.getDisplayName());
            mEmailTV.setText(signInAccount.getEmail());
            signInAccount.getPhotoUrl();
        }

        // Adding data to Firestore
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        Map<String, Object> map = new HashMap<>();
        map.put("timeOfOpening", new Timestamp(new Date()));

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("logData")
                .add(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(LOGCAT_TAG, "onSuccess: Data uploaded successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(LOGCAT_TAG, "onFailure: Data upload failed");
                    }
                });

        // On location change Data upload
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loc = mEditText.getText().toString();

                Map<String, Object> map = new HashMap<>();
                map.put("currentCity", loc);
                map.put("timeOfChangingCity", new Timestamp(new Date()));


                FirebaseUser user = mAuth.getCurrentUser();
                FirebaseFirestore.getInstance().collection("users").document(user.getUid()).collection("locationChange")
                        .add(map)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(LOGCAT_TAG, "onSuccess: Change City data uploaded successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(LOGCAT_TAG, "onSuccess: Change City data uploaded failed");
                            }
                        });

            }
        });

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
                Toast.makeText(MainActivity.this, "on it", Toast.LENGTH_SHORT).show();
                mWebView.reload();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    public class LocalBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
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


//    // Image Slider
//    public void renewItems() {
//        List<SliderItem> sliderItemList = new ArrayList<>();
//        //dummy data
//        for (int i = 0; i < SLIDER_COUNT; i++) {
//            SliderItem sliderItem = new SliderItem();
//            if (i % 2 == 0) {
//                sliderItem.setImageUrl("https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260");
//            } else {
//                sliderItem.setImageUrl("https://images.pexels.com/photos/747964/pexels-photo-747964.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260");
//            }
//            sliderItemList.add(sliderItem);
//        }
//        adapter.renewItems(sliderItemList);
//    }


}