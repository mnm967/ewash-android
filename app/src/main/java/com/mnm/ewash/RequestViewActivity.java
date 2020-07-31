package com.mnm.ewash;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mnm.ewash.models.PendingRequest;
import com.mnm.ewash.utils.NetworkUtils;
import com.pixplicity.easyprefs.library.Prefs;

import mehdi.sakout.fancybuttons.FancyButton;

public class RequestViewActivity extends AppCompatActivity{
    public static boolean activityIsRunning = false;

    FancyButton buttonLarge;
    FancyButton button1;
    FancyButton button2;
    FancyButton button3;
    TextView promptText;
    Toolbar toolbar;
    RelativeLayout mainLayout;

    FirebaseAuth mAuth;
    AlertDialog processingDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_view_activity);

        activityIsRunning = true;
        buttonLarge = findViewById(R.id.btn_large);
        button1 = findViewById(R.id.btn_1);
        button2 = findViewById(R.id.btn_2);
        button3 = findViewById(R.id.btn_3);
        promptText = findViewById(R.id.prompt_text);
        toolbar = findViewById(R.id.toolbar);
        mainLayout = findViewById(R.id.main_relative_layout);

        mAuth = FirebaseAuth.getInstance();
        boolean con = NetworkUtils.isNetworkConnected(RequestViewActivity.this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        if(!con) return;

        final FloatingActionButton fab = findViewById(R.id.refresh_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
       /* toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.refresh:
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                        return true;
                }
                return false;
            }
        });*/

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestViewActivity.this, R.style.DialogCustom))
                .setTitle("Processing...")
                .setView(R.layout.processing_layout)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .setCancelable(false);
        processingDialog = builder.create();
        processingDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
        String reqID = Prefs.getString("REQ_ID", "");
        if(reqID.isEmpty()){
            finish();
        }
        processingDialog.show();
        button3.setText("VIEW REQUEST");
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewRequestActivity.class);
                startActivity(intent);
            }
        });
        Query history = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("history").child(reqID);
        history.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final PendingRequest request = dataSnapshot.getValue(PendingRequest.class);
                if(request == null){
                    Prefs.putString("REQ_ID", "");
                    Prefs.putString("REQ_STATUS", "");
                    Intent intent = new Intent(getApplicationContext(), RequestWashActivity.class);
                    startActivity(intent);
                    return;
                }
                if(request.status == null){
                    Prefs.putString("REQ_ID", "");
                    Prefs.putString("REQ_STATUS", "");
                    Intent intent = new Intent(getApplicationContext(), RequestWashActivity.class);
                    startActivity(intent);
                    return;
                }
                if(request.status.contains("PENDING")) {
                    mainLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
                    fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.orange_500)));
                    toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_700));
                    }
                    button2.setVisibility(View.GONE);
                    button1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
                    button1.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
                    button2.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
                    button2.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
                    buttonLarge.setIconResource(R.drawable.request_pending);
                    buttonLarge.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
                    buttonLarge.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
                    button1.setText("CANCEL");
                    button1.setOnClickListener(new View.OnClickListener() {
                        AlertDialog confirmDialog = null;

                        @Override
                        public void onClick(View view) {
                            boolean con = NetworkUtils.isViewNetworkConnected(RequestViewActivity.this, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                }
                            });
                            if(!con) return;
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestViewActivity.this, R.style.DialogCustom))
                                    .setTitle("Request Cancellation")
                                    .setMessage("Your request will be cancelled.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                                            jobDispatcher.cancelAll();
                                            processingDialog.show();
                                            //stopService(new Intent(getApplicationContext(), RequestListenerService.class));
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("CANCELLED - By Customer");
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("CANCELLED - By Customer")
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Prefs.putString("REQ_STATUS", "CANCELLED");
                                                            processingDialog.dismiss();
                                                            Intent intent = getIntent();
                                                            finish();
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestViewActivity.this, R.style.DialogCustom))
                                                                    .setTitle("Connection Failed")
                                                                    .setMessage(e.getMessage())
                                                                    .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                        }
                                                                    })
                                                                    .setCancelable(true);
                                                            AlertDialog diag = builder.create();
                                                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                                                            diag.show();
                                                        }
                                                    });
                                                /*final InterstitialAd interstitialAd = new InterstitialAd(RequestViewActivity.this);
                                                interstitialAd.setAdUnitId("ca-app-pub-8736882148099200/7936865199");
                                                interstitialAd.loadAd(new AdRequest.Builder().build());
                                                interstitialAd.setAdListener(new AdListener(){
                                                    @Override
                                                    public void onAdLoaded() {
                                                        super.onAdLoaded();
                                                        processingDialog.dismiss();
                                                        interstitialAd.show();
                                                    }

                                                    @Override
                                                    public void onAdFailedToLoad(int i) {
                                                        super.onAdFailedToLoad(i);
                                                        processingDialog.dismiss();
                                                        Intent intent = getIntent();
                                                        finish();
                                                        startActivity(intent);
                                                    }

                                                    @Override
                                                    public void onAdClosed() {
                                                        super.onAdClosed();
                                                        Intent intent = getIntent();
                                                        finish();
                                                        startActivity(intent);
                                                    }
                                                });*/
                                        }
                                    })
                                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            confirmDialog.dismiss();
                                        }
                                    }).setCancelable(false);
                            confirmDialog = builder.create();
                            confirmDialog.show();
                        }
                    });
                    promptText.setText("Your request is being processed...");
                }
                else if(request.status.contains("ACCEPTED")) {
                    mainLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_blue_500));
                    fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.light_blue_500)));
                    toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_blue_500));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.light_blue_700));
                    }
                    button1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_blue_500));
                    button1.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_blue_500));
                    button2.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_blue_500));
                    button2.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_blue_500));
                    buttonLarge.setIconResource(R.drawable.request_accepted);
                    buttonLarge.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_blue_500));
                    buttonLarge.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_blue_500));
                    button1.setText("COMPLETE");
                    button1.setOnClickListener(new View.OnClickListener() {
                        AlertDialog confirmDialog = null;

                        @Override
                        public void onClick(View view) {
                            if(!Prefs.getString("REQ_STATUS", "").contains("ACCEPTED")){
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                                return;
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestViewActivity.this, R.style.DialogCustom))
                                    .setTitle("Request Completion")
                                    .setMessage("Has your wash been completed?")
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //stopService(new Intent(getApplicationContext(), RequestListenerService.class));
                                            Prefs.putString("REQ_STATUS", "COMPLETED");
                                            FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                                            jobDispatcher.cancelAll();
                                            processingDialog.show();
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("COMPLETED");
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("COMPLETED")
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Prefs.putString("REQ_STATUS", "COMPLETED");
                                                            processingDialog.dismiss();
                                                            Intent intent = getIntent();
                                                            finish();
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestViewActivity.this, R.style.DialogCustom))
                                                                    .setTitle("Connection Failed")
                                                                    .setMessage(e.getMessage())
                                                                    .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                        }
                                                                    })
                                                                    .setCancelable(true);
                                                            AlertDialog diag = builder.create();
                                                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                                                            diag.show();
                                                        }
                                                    });
                                            /*final InterstitialAd interstitialAd = new InterstitialAd(RequestViewActivity.this);
                                            interstitialAd.setAdUnitId("ca-app-pub-8736882148099200/7936865199");
                                            interstitialAd.loadAd(new AdRequest.Builder().build());
                                            interstitialAd.setAdListener(new AdListener(){
                                                @Override
                                                public void onAdLoaded() {
                                                    super.onAdLoaded();
                                                    processingDialog.dismiss();
                                                    interstitialAd.show();
                                                }

                                                @Override
                                                public void onAdFailedToLoad(int i) {
                                                    super.onAdFailedToLoad(i);
                                                    processingDialog.dismiss();
                                                    Intent intent = getIntent();
                                                    finish();
                                                    startActivity(intent);
                                                }

                                                @Override
                                                public void onAdClosed() {
                                                    super.onAdClosed();
                                                    Intent intent = getIntent();
                                                    finish();
                                                    startActivity(intent);
                                                }
                                            });*/
                                        }
                                    })
                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    }).setCancelable(false);
                            confirmDialog = builder.create();
                            confirmDialog.show();
                        }
                    });
                    button2.setText("CANCEL");
                    button2.setOnClickListener(new View.OnClickListener() {
                        AlertDialog confirmDialog = null;

                        @Override
                        public void onClick(View view) {
                            if(!Prefs.getString("REQ_STATUS", "").contains("ACCEPTED")){
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                                return;
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestViewActivity.this, R.style.DialogCustom))
                                    .setTitle("Request Cancellation")
                                    .setMessage("Your request will be cancelled.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Prefs.putString("REQ_STATUS", "CANCELLED");
                                            FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                                            jobDispatcher.cancelAll();
                                            processingDialog.dismiss();
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("CANCELLED - By Customer");
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("CANCELLED - By Customer")
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Prefs.putString("REQ_STATUS", "CANCELLED");
                                                            processingDialog.dismiss();
                                                            Intent intent = getIntent();
                                                            finish();
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestViewActivity.this, R.style.DialogCustom))
                                                                    .setTitle("Connection Failed")
                                                                    .setMessage(e.getMessage())
                                                                    .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                        }
                                                                    })
                                                                    .setCancelable(true);
                                                            AlertDialog diag = builder.create();
                                                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                                                            diag.show();
                                                        }
                                                    });
                                            /*final InterstitialAd interstitialAd = new InterstitialAd(RequestViewActivity.this);
                                            interstitialAd.setAdUnitId("ca-app-pub-8736882148099200/7936865199");
                                            interstitialAd.loadAd(new AdRequest.Builder().build());
                                            interstitialAd.setAdListener(new AdListener(){
                                                @Override
                                                public void onAdLoaded() {
                                                    super.onAdLoaded();
                                                    processingDialog.dismiss();
                                                    interstitialAd.show();
                                                }

                                                @Override
                                                public void onAdFailedToLoad(int i) {
                                                    super.onAdFailedToLoad(i);
                                                    processingDialog.dismiss();
                                                    Intent intent = getIntent();
                                                    finish();
                                                    startActivity(intent);
                                                }

                                                @Override
                                                public void onAdClosed() {
                                                    super.onAdClosed();
                                                    Intent intent = getIntent();
                                                    finish();
                                                    startActivity(intent);
                                                }
                                            });*/
                                        }
                                    })
                                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    }).setCancelable(false);
                            confirmDialog = builder.create();
                            confirmDialog.show();
                        }
                    });
                    promptText.setText("Your request has been scheduled");
                }
                else if(request.status.contains("REJECTED")) {
                    FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                    jobDispatcher.cancelAll();
                    mainLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.red_400)));
                    toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.red_700));
                    }
                    button1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    button1.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    button2.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    button2.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    buttonLarge.setIconResource(R.drawable.request_rejected);
                    buttonLarge.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    buttonLarge.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    button1.setText("CONTINUE");
                    button1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Prefs.putString("REQ_ID", "");
                            Prefs.putString("REQ_STATUS", "");

                            Intent intent = new Intent(getApplicationContext(), RequestWashActivity.class);
                            finish();
                            startActivity(intent);
                            /*final InterstitialAd interstitialAd = new InterstitialAd(RequestViewActivity.this);
                            interstitialAd.setAdUnitId("ca-app-pub-8736882148099200/7936865199");
                            interstitialAd.loadAd(new AdRequest.Builder().build());
                            interstitialAd.setAdListener(new AdListener(){
                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                    processingDialog.dismiss();
                                    interstitialAd.show();
                                }

                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                    processingDialog.dismiss();
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    finish();
                                    startActivity(intent);
                                }

                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    finish();
                                    startActivity(intent);
                                }
                            });*/
                        }
                    });
                    button2.setText("FEEDBACK");
                    button2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Prefs.putString("REQ_ID", "");
                            Prefs.putString("REQ_STATUS", "");

                            Intent intent = new Intent(getApplicationContext(), ContactUsActivity.class);
                            finish();
                            startActivity(intent);
                        }
                    });
                    promptText.setText("Your request has been "+request.status);
                }
                else if(request.status.contains("COMPLETED")) {
                    FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                    jobDispatcher.cancelAll();
                    mainLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_400));
                    fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.green_400)));
                    toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_400));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.green_600));
                    }
                    button1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_400));
                    button1.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_400));
                    button2.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_400));
                    button2.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_400));
                    buttonLarge.setIconResource(R.drawable.request_completed);
                    buttonLarge.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_400));
                    buttonLarge.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_400));
                    button1.setText("CONTINUE");
                    button1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Prefs.putString("REQ_ID", "");
                            Prefs.putString("REQ_STATUS", "");

                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            finish();
                            startActivity(intent);
                        }
                    });
                    button2.setText("FEEDBACK");
                    button2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Prefs.putString("REQ_ID", "");
                            Prefs.putString("REQ_STATUS", "");

                            Intent intent = new Intent(getApplicationContext(), ContactUsActivity.class);
                            finish();
                            startActivity(intent);
                        }
                    });
                    promptText.setText("Your request has been COMPLETED");
                }
                else if(request.status.contains("CANCELLED")) {
                    FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                    jobDispatcher.cancelAll();
                       /* try {
                            Intent service = new Intent(getApplicationContext(), RequestListenerService.class);
                            stopService(service);
                        }catch (Exception e){
                            e.printStackTrace();
                        }*/
                    mainLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.red_400)));
                    toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.red_700));
                    }
                    button1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    button1.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    button2.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    button2.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    buttonLarge.setIconResource(R.drawable.request_rejected);
                    buttonLarge.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    buttonLarge.setFocusBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_400));
                    button1.setText("CONTINUE");
                    button1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Prefs.putString("REQ_ID", "");
                            Prefs.putString("REQ_STATUS", "");

                            Intent intent = new Intent(getApplicationContext(), RequestWashActivity.class);
                            finish();
                            startActivity(intent);
                        }
                    });
                    button2.setText("FEEDBACK");
                    button2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Prefs.putString("REQ_ID", "");
                            Prefs.putString("REQ_STATUS", "");

                            Intent intent = new Intent(getApplicationContext(), ContactUsActivity.class);
                            finish();
                            startActivity(intent);
                        }
                    });
                    promptText.setText("Your request has been " + request.status);
                }
                processingDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                processingDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestViewActivity.this, R.style.DialogCustom))
                        .setTitle("Something Went Wrong")
                        .setMessage(databaseError.getMessage())
                        .setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        }).setCancelable(false);
                AlertDialog diag = builder.create();
                diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                diag.show();
                finish();
            }
        });
    }
}
