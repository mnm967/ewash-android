package com.mnm.ewash;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
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

public class HomeActivity extends AppCompatActivity{

    Toolbar toolbar;
    FancyButton washButton;
    FancyButton historyButton;
    TextView adminText;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        mAuth = FirebaseAuth.getInstance();
        Prefs.putString("REQ_ID", "");
        Prefs.putString("REQ_STATUS", "");
        /*AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, R.style.Theme_AppCompat_Dialog_Alert))
                .setTitle("Registration Complete")
                .setMessage("You can now Log In with your Email & Password")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog diag = builder.create();
                diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                diag.show();*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        }
        //startService(new Intent(this, RequestListenerService.class));
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        washButton = findViewById(R.id.btn_car_wash);
        historyButton = findViewById(R.id.btn_history);
        adminText = findViewById(R.id.admin_text);

        adminText.setText("Welcome User. Click the button to Wash Your Car!");
        adminText.setVisibility(View.VISIBLE);
        washButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWashActivity();
            }
        });
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHistoryActivity();
            }
        });
        try {
            if (mAuth.getCurrentUser().getUid().equals("PnNAT9YjbIMn9Wq73Lczyv0Qifu1")) {
                adminText.setVisibility(View.VISIBLE);
                adminText.setText("Welcome Admin. Click Button to View User Requests.");
                historyButton.setVisibility(View.GONE);
                washButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startAdminActivity();
                    }
                });
                try{
                    FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                    jobDispatcher.cancelAll();
                    Job job = jobDispatcher.newJobBuilder()
                            .setService(AdminJobService.class)
                            .setTag(AdminJobService.TAG)
                            .setRecurring(true)
                            .setLifetime(Lifetime.FOREVER)
                            .setTrigger(periodicTrigger(20, 1))
                            .setReplaceCurrent(true)
                            .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                            .setConstraints(Constraint.ON_ANY_NETWORK)
                            .build();
                    jobDispatcher.mustSchedule(job);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            signUserOut();
        }
    }
    public JobTrigger periodicTrigger(int frequency, int tolerance){
        return Trigger.executionWindow(frequency - tolerance, frequency);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void startAdminActivity(){
        Intent intent = new Intent(getApplicationContext(), AdminViewRequestsActivity.class);
        startActivity(intent);
    }

    AlertDialog processingDialog;
    private void startWashActivity(){
        try {
            boolean con = NetworkUtils.isHomeNetworkConnected(HomeActivity.this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            if (!con) return;

            final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, R.style.DialogCustom))
                    .setTitle("Connecting...")
                    .setView(R.layout.processing_layout)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    })
                    .setCancelable(true);
            processingDialog = builder.create();
            processingDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
            processingDialog.show();
            final Query version = FirebaseDatabase.getInstance().getReference("version");
            version.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int version = Integer.valueOf(dataSnapshot.getValue().toString());
                    processingDialog.dismiss();
                    if (version > BuildConfig.VERSION_CODE) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, R.style.DialogCustom))
                                .setTitle("Outdated Version")
                                .setMessage("You are using an outdated version of eWash. Please update the application to use our services.")
                                .setCancelable(false)
                                .setNegativeButton("LATER", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setPositiveButton("UPDATE NOW", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try{
                                          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mnm.ewash")));
                                        }catch (Exception e){
                                            try{
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.mnm.ewash")));
                                            }catch (Exception ex){}
                                        }
                                    }
                                });
                        AlertDialog diag = builder.create();
                        diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                        diag.show();
                    } else {
                        if (Prefs.getString("REQ_ID", "").isEmpty() && Prefs.getString("REQ_STATUS", "").isEmpty()) {
                            //processingDialog.show();
                            final Query history = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("history");
                            history.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        PendingRequest request = snapshot.getValue(PendingRequest.class);
                                        if (request.status.equals("PENDING") || request.status.equals("ACCEPTED")) {
                                            Prefs.putString("REQ_ID", request.requestId + "");
                                            Prefs.putString("REQ_STATUS", request.status);
                                            FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                                            jobDispatcher.cancelAll();
                                            Job job = jobDispatcher.newJobBuilder()
                                                    .setService(RequestJobService.class)
                                                    .setTag(RequestJobService.TAG)
                                                    .setRecurring(true)
                                                    .setLifetime(Lifetime.FOREVER)
                                                    .setTrigger(Trigger.executionWindow(0, 30))
                                                    .setReplaceCurrent(true)
                                                    .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                                                    .setConstraints(Constraint.ON_ANY_NETWORK)
                                                    .build();
                                            jobDispatcher.mustSchedule(job);
                                            Intent intent = new Intent(getApplicationContext(), RequestViewActivity.class);
                                            startActivity(intent);
                                            processingDialog.dismiss();
                                            return;
                                        } else {
                                            FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                                            jobDispatcher.cancelAll();
                                        }
                                    }
                                    Intent intent = new Intent(getApplicationContext(), RequestWashActivity.class);
                                    startActivity(intent);
                                    processingDialog.dismiss();
                                    return;
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    processingDialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, R.style.DialogCustom))
                                            .setTitle("Connection Error")
                                            .setMessage("Unable to Connect. Try Again Later: " + databaseError.getMessage())
                                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                @Override
                                                public void onCancel(DialogInterface dialogInterface) {
                                                    //Intent intent = getIntent();
                                                    //finish();
                                                    //startActivity(intent);
                                                }
                                            })
                                            .setCancelable(false)
                                            .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                    AlertDialog diag = builder.create();
                                    diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                                    diag.show();
                                    //finish();
                                }
                            });
                        } else {
                            Intent intent = new Intent(getApplicationContext(), RequestViewActivity.class);
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    processingDialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, R.style.DialogCustom))
                            .setTitle("Connection Error")
                            .setMessage("Unable to Connect. Try Again Later: " + databaseError.getMessage())
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    //Intent intent = getIntent();
                                    //finish();
                                    //startActivity(intent);
                                }
                            })
                            .setCancelable(false)
                            .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                    AlertDialog diag = builder.create();
                    diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                    diag.show();
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void startHistoryActivity(){
        Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
        startActivity(intent);
    }
    private void startContactActivity(){
        Intent intent = new Intent(getApplicationContext(), ContactUsActivity.class);
        startActivity(intent);
    }
    private void startUserDetailsActivity(){
        Intent intent = new Intent(getApplicationContext(), UserDetailsActivity.class);
        startActivity(intent);
    }
    private void signUserOut(){
        Prefs.putString("REQ_ID", "");
        //stopService(new Intent(this, RequestListenerService.class));
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
        jobDispatcher.cancelAll();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /*AlertDialog processingDialog;
    private boolean checkConnection(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, R.style.DialogCustom))
                .setTitle("Connecting...")
                .setView(R.layout.processing_layout)
                .setCancelable(false);
        processingDialog = builder.create();
        try{

        }catch(Exception e){

        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ewash_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.tool_contact) {
            startContactActivity();
            return true;
        }else if (id == R.id.tool_sign_out) {
            signUserOut();
            return true;
        }else if (id == R.id.tool_account_details) {
            startUserDetailsActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
