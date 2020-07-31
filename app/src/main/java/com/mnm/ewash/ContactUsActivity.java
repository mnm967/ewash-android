package com.mnm.ewash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import mehdi.sakout.fancybuttons.FancyButton;

public class ContactUsActivity extends AppCompatActivity{
    Toolbar toolbar;
    FancyButton sendEmail;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_us_view);
        toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        sendEmail = findViewById(R.id.btn_send_email);
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, "youcallwewash@gmail.com");
                startActivity(Intent.createChooser(intent, "Send Email"));
                finish();
            }
        });
    }
}
