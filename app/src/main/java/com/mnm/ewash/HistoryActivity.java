package com.mnm.ewash;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mnm.ewash.adapters.HistoryCardViewAdapter;
import com.mnm.ewash.adapters.RecyclerDivider;
import com.mnm.ewash.models.PendingRequest;
import com.mnm.ewash.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity{
    Toolbar toolbar;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    List<PendingRequest> userRequests = new ArrayList<>();
    AlertDialog processingDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_view);
        mAuth = FirebaseAuth.getInstance();
        boolean con = NetworkUtils.isNetworkConnected(HistoryActivity.this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        if(!con) return;

        FloatingActionButton fab = findViewById(R.id.refresh_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HistoryActivity.this, R.style.DialogCustom))
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
        processingDialog.show();
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
        /*toolbar.inflateMenu(R.menu.toolbar_menu);
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

        recyclerView = findViewById(R.id.history_recyclerview);
        recyclerView.addItemDecoration(new RecyclerDivider(8));
        LinearLayoutManager lm = new LinearLayoutManager(HistoryActivity.this);
        //lm.setAutoMeasureEnabled(false);
        recyclerView.setLayoutManager(lm);
        Query history = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("history");
        history.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                processingDialog.dismiss();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PendingRequest request = snapshot.getValue(PendingRequest.class);
                    userRequests.add(request);
                }
                Collections.reverse(userRequests);
                if(userRequests.isEmpty()){
                    findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
                }
                HistoryCardViewAdapter adapter = new HistoryCardViewAdapter(HistoryActivity.this, userRequests);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                processingDialog.dismiss();
                finish();
            }
        });
    }
}
