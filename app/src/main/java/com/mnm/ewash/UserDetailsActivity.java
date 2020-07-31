package com.mnm.ewash;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mnm.ewash.models.User;
import com.mnm.ewash.utils.NetworkUtils;

import mehdi.sakout.fancybuttons.FancyButton;

public class UserDetailsActivity extends AppCompatActivity{
    Toolbar toolbar;

    TextView name;
    TextView email;
    TextView phoneNumber;

    FancyButton changeName;
    FancyButton changePhoneNumber;
    FancyButton resetPassword;
    FancyButton signOut;

    FirebaseAuth mAuth;
    AlertDialog processingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_details);
        mAuth = FirebaseAuth.getInstance();
        boolean con = NetworkUtils.isNetworkConnected(UserDetailsActivity.this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        if(!con) return;
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        phoneNumber = findViewById(R.id.phone_number);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(UserDetailsActivity.this, R.style.DialogCustom))
                .setTitle("Processing...")
                .setView(R.layout.processing_layout)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .setCancelable(true);
        processingDialog = builder.create();
        processingDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
        changeName = findViewById(R.id.btn_change_name);
        changePhoneNumber = findViewById(R.id.btn_change_phone_number);
        resetPassword = findViewById(R.id.btn_change_password);
        signOut = findViewById(R.id.btn_sign_out);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
        processingDialog.show();
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                processingDialog.dismiss();
                User user = dataSnapshot.getValue(User.class);
                name.setText(user.name);
                email.setText(user.email);
                phoneNumber.setText(user.phoneNumber);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                processingDialog.dismiss();
                //Toast.makeText(getApplicationContext(), "Failed - "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View editView = View.inflate(UserDetailsActivity.this, R.layout.edittext_dialog, null);
                final TextInputEditText textInputEditText = editView.findViewById(R.id.edit_text);
                textInputEditText.setHint("New Name");
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(UserDetailsActivity.this, R.style.DialogCustom))
                        .setTitle("Change Name")
                        .setView(editView)
                        .setCancelable(false)
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                userRef.child("name").setValue(textInputEditText.getText().toString());
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        });
                AlertDialog diag = builder.create();
                diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                diag.show();
            }
        });
        changePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View editView = View.inflate(UserDetailsActivity.this, R.layout.edittext_dialog, null);
                final TextInputEditText textInputEditText = editView.findViewById(R.id.edit_text);
                textInputEditText.setHint("New Phone Number");
                textInputEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(UserDetailsActivity.this, R.style.DialogCustom))
                        .setTitle("Change Phone Number")
                        .setView(editView)
                        .setCancelable(false)
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                userRef.child("phoneNumber").setValue(textInputEditText.getText().toString());
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        });
                AlertDialog diag = builder.create();
                diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                diag.show();
            }
        });
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stopService(new Intent(UserDetailsActivity.this, RequestListenerService.class));
                FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                jobDispatcher.cancelAll();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public void onBackPressed() {
        if(processingDialog.isShowing()){
            processingDialog.dismiss();
            finish();
        }else{
            finish();
        }
    }
}
