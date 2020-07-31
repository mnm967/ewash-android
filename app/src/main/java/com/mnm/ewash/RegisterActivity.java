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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mnm.ewash.models.User;
import com.mnm.ewash.utils.StringUtils;

import mehdi.sakout.fancybuttons.FancyButton;

public class RegisterActivity extends AppCompatActivity{
    public TextInputEditText firstName;
    public TextInputEditText lastName;
    public TextInputEditText email;
    public TextInputEditText phoneNumber;
    public TextInputEditText password;
    public TextInputEditText confirmPassword;

    public FancyButton register;
    public FancyButton login;
    
    FirebaseAuth mAuth;
    AlertDialog processingDialog;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        mAuth = FirebaseAuth.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RegisterActivity.this, R.style.DialogCustom))
                .setTitle("Processing...")
                .setView(R.layout.processing_layout)
                .setCancelable(false);
        processingDialog = builder.create();
        processingDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
        firstName = findViewById(R.id.first_name_edit_text);
        lastName = findViewById(R.id.last_name_edit_text);
        email = findViewById(R.id.email_edit_text);
        phoneNumber = findViewById(R.id.number_edit_text);
        password = findViewById(R.id.password_edit_text);
        confirmPassword = findViewById(R.id.confirm_password_edit_text);
        register = findViewById(R.id.btn_register);
        login = findViewById(R.id.btn_login);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean error = false;
                if(firstName.getText().toString().isEmpty()){
                    firstName.setError("*Required");
                    error =  true;
                }
                if(lastName.getText().toString().isEmpty()){
                    lastName.setError("*Required");
                    error =  true;
                }
                if(email.getText().toString().isEmpty()){
                    email.setError("*Required");
                    error =  true;
                }
                if(phoneNumber.getText().toString().isEmpty()){
                    phoneNumber.setError("*Required");
                    error =  true;
                }
                if(password.getText().toString().isEmpty()){
                    password.setError("*Required");
                    error =  true;
                }
                if(confirmPassword.getText().toString().isEmpty()){
                    confirmPassword.setError("*Required");
                    error =  true;
                }
                if(password.length() < 6){
                    password.setError("Password is too short");
                    error = true;
                }
                if(!StringUtils.isValidPhoneNumber(phoneNumber.getText().toString())){
                    phoneNumber.setError("Invalid Phone Number");
                    error =  true;
                }
                if(!StringUtils.isValidEmailAddress(email.getText().toString())){
                    email.setError("Invalid Email Address");
                    error =  true;
                }
                if(!error){
                    if(password.getText().toString().equals(confirmPassword.getText().toString())){
                        processingDialog.show();
                        final String mfirstName = firstName.getText().toString();
                        final String mlastName = lastName.getText().toString();
                        final String memail = email.getText().toString();
                        final String mphoneNumber = phoneNumber.getText().toString();
                        final String mpassword = password.getText().toString();

                        mAuth.createUserWithEmailAndPassword(memail, mpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                processingDialog.dismiss();
                                if(task.isSuccessful()&&mAuth.getCurrentUser() != null){
                                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
                                    User user = new User(mfirstName+" "+mlastName, memail, mphoneNumber);
                                    mDatabase.child(mAuth.getCurrentUser().getUid()).setValue(user);
                                    //TODO Show Dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RegisterActivity.this, R.style.DialogCustom))
                                            .setTitle("Registration Complete")
                                            .setMessage("You can now Log In with your Email & Password")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                    AlertDialog diag = builder.create();
                                    diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                                    diag.show();
                                }else{
                                    if(task.getException() instanceof FirebaseNetworkException){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RegisterActivity.this, R.style.DialogCustom))
                                                .setTitle("Registration Failed")
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
                                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RegisterActivity.this, R.style.DialogCustom))
                                                .setTitle("Registration Failed")
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
                    }else {
                        confirmPassword.setError("Passwords Do Not Match");
                    }
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }
}
