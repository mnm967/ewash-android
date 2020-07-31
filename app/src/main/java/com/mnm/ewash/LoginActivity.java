package com.mnm.ewash;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.mnm.ewash.utils.StringUtils;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginActivity extends AppCompatActivity{
    public TextInputEditText username;
    public TextInputEditText password;
    public FancyButton login;
    public FancyButton register;
    public FancyButton forgotPassword;

    FirebaseAuth mAuth;

    AlertDialog processingDialog;

    /*public JobTrigger periodicTrigger(int frequency, int tolerance){
        return Trigger.executionWindow(frequency - tolerance, frequency);
    }*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        /*FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
        jobDispatcher.cancelAll();
        Job job = jobDispatcher.newJobBuilder()
                .setService(TestJobService.class)
                .setTag("test_job")
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(periodicTrigger(30, 10))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        jobDispatcher.mustSchedule(job);*/
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        }
        username = findViewById(R.id.username_edit_text);
        password = findViewById(R.id.password_edit_text);
        login = findViewById(R.id.btn_login);
        register = findViewById(R.id.btn_register);
        forgotPassword = findViewById(R.id.btn_forgot_password);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.DialogCustom))
                .setTitle("Processing...")
                .setView(R.layout.processing_layout)
                .setCancelable(false);
        processingDialog = builder.create();
        processingDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean error = false;
                if(username.getText().toString().isEmpty()){
                    username.setError("*Required");
                    error =  true;
                }
                if(password.getText().toString().isEmpty()){
                    password.setError("*Required");
                    error =  true;
                }
                if(!StringUtils.isValidEmailAddress(username.getText().toString())){
                    username.setError("Invalid Email Address");
                    error =  true;
                }
                if(!error){
                    processingDialog.show();
                    mAuth.signInWithEmailAndPassword(username.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    processingDialog.dismiss();
                                    if(task.isSuccessful()&&mAuth.getCurrentUser() != null){
                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.DialogCustom))
                                                    .setTitle("Sign In Failed")
                                                    .setMessage("Invalid Username or Password")
                                                    .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                        }
                                                    })
                                                    .setCancelable(true);
                                            AlertDialog diag = builder.create();
                                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                                            diag.show();
                                        }else if(task.getException() instanceof FirebaseNetworkException){
                                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.DialogCustom))
                                                    .setTitle("Sign In Failed")
                                                    .setMessage("Please check your internet connection.")
                                                    .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                        }
                                                    })
                                                    .setCancelable(true);
                                            AlertDialog diag = builder.create();
                                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                                            diag.show();
                                        }else{
                                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.DialogCustom))
                                                    .setTitle("Sign In Failed")
                                                    .setMessage(task.getException().getMessage())
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
                                        task.getException().printStackTrace();
                                    }
                                }
                            });
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
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
