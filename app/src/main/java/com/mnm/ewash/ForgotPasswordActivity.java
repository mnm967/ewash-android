package com.mnm.ewash;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;

import mehdi.sakout.fancybuttons.FancyButton;

public class ForgotPasswordActivity extends AppCompatActivity{
    Toolbar toolbar;
    TextView forgotText;
    TextInputEditText email;
    FancyButton resetButton;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_view);
        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.toolbar);
        email = findViewById(R.id.username_edit_text);
        resetButton = findViewById(R.id.btn_reset_password);
        forgotText = findViewById(R.id.forgot_text);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText().toString().isEmpty()){
                    email.setError("*Required");
                }else if(!email.getText().toString().contains("@") || !email.getText().toString().contains(".")){
                    email.setError("Enter valid email address");
                }else{
                    mAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ForgotPasswordActivity.this, R.style.DialogCustom))
                                        .setTitle("Reset Email")
                                        .setMessage("We have sent a reset link to your email.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                finish();
                                            }
                                        });
                                AlertDialog diag = builder.create();
                                diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                                diag.show();
                            }else{
                                if(task.getException() instanceof FirebaseNetworkException){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ForgotPasswordActivity.this, R.style.DialogCustom))
                                            .setTitle("Password Reset Failed")
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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ForgotPasswordActivity.this, R.style.DialogCustom))
                                            .setTitle("Password Reset Failed")
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
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        //startActivity(intent);
    }
}
