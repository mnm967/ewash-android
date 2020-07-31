package com.mnm.ewash;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mnm.ewash.adapters.AdminCardViewAdapter;
import com.mnm.ewash.adapters.RecyclerClickListener;
import com.mnm.ewash.adapters.RecyclerDivider;
import com.mnm.ewash.models.PendingRequest;
import com.mnm.ewash.models.User;
import com.mnm.ewash.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminViewRequestsActivity extends AppCompatActivity{
    Toolbar toolbar;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    List<PendingRequest> userRequests = new ArrayList<>();
    AlertDialog processingDialog;

    String reqType = "PENDING";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_view);
        mAuth = FirebaseAuth.getInstance();
        NetworkUtils.isNetworkConnected(AdminViewRequestsActivity.this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(AdminViewRequestsActivity.this, R.style.DialogCustom))
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


        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
        toolbar.setTitle("Pending");

        FloatingActionButton fab = findViewById(R.id.refresh_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.history_recyclerview);
        recyclerView.addItemDecoration(new RecyclerDivider(8));
        LinearLayoutManager lm = new LinearLayoutManager(AdminViewRequestsActivity.this);
        //lm.setAutoMeasureEnabled(false);
        recyclerView.setLayoutManager(lm);

        toolbar.inflateMenu(R.menu.admin_toolbar_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.pending_requests:
                        if(!reqType.equals("PENDING")){
                            toolbar.setTitle("Pending");
                            reqType = "PENDING";
                            reloadRecyclerView();
                        }
                        return true;
                    case R.id.accepted_requests:
                        if(!reqType.equals("ACCEPTED")){
                            toolbar.setTitle("Accepted");
                            reqType = "ACCEPTED";
                            reloadRecyclerView();
                        }
                        return true;
                    case R.id.rejected_requests:
                        if(!reqType.equals("REJECTED")){
                            toolbar.setTitle("Rejected");
                            reqType = "REJECTED";
                            reloadRecyclerView();
                        }
                        return true;
                    case R.id.completed_requests:
                        if(!reqType.equals("COMPLETED")){
                            toolbar.setTitle("Completed");
                            reqType = "COMPLETED";
                            reloadRecyclerView();
                        }
                        return true;
                    case R.id.cancelled_requests:
                        if(!reqType.equals("CANCELLED")){
                            toolbar.setTitle("Cancelled");
                            reqType = "CANCELLED";
                            try {
                                reloadRecyclerView();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        return true;
                   /* case R.id.refresh_requests:
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                        return true;*/
                }
                return false;
            }
        });
        reloadRecyclerViewFirst();
    }

    public void reloadRecyclerView(){
        recyclerView.setAdapter(null);
        processingDialog.show();
        userRequests.clear();
        Query requests = FirebaseDatabase.getInstance().getReference("requests");
        requests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                processingDialog.dismiss();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PendingRequest request = snapshot.getValue(PendingRequest.class);
                    if(request == null){
                        Log.i("eWash Req", "null");
                    }
                    Log.i("eWash Snap", request.requestId);
                    Log.i("eWash Time", ""+request.time);
                    if (request.status.contains(reqType)) {
                        userRequests.add(request);
                    }
                }
                AdminCardViewAdapter adapter = new AdminCardViewAdapter(AdminViewRequestsActivity.this, userRequests, new RecyclerClickListener() {
                    @Override
                    public void onLongClick(int position, String type) {
                    }

                    User user;
                    @Override
                    public void onClick(int position, String type) {
                        final PendingRequest request = userRequests.get(position);

                        if (type.equals(AdminCardViewAdapter.ACCEPT_BUTTON)) {
                            View editView = View.inflate(AdminViewRequestsActivity.this, R.layout.edittext_dialog, null);
                            final TextInputEditText textInputEditText = editView.findViewById(R.id.edit_text);
                            textInputEditText.setHint("Washer Name(s)");
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(AdminViewRequestsActivity.this, R.style.DialogCustom))
                                    .setTitle("Washer Name(s):")
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
                                            String washer = textInputEditText.getText().toString();
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("ACCEPTED");
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("ACCEPTED");
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("washerName").setValue(washer);
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("washerName").setValue(washer);
                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);
                                        }
                                    });
                            AlertDialog diag = builder.create();
                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                            diag.show();
                            return;

                        } else if (type.equals(AdminCardViewAdapter.REJECT_BUTTON)) {
                            final String[] reasons = {"Time Unavailable", "eWash is not in your Area", "Too many requests from User"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(AdminViewRequestsActivity.this, R.style.DialogCustom))
                                    .setTitle("Reason for Rejection:")
                                    .setItems(reasons, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("REJECTED - "+reasons[i]);
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("REJECTED - "+reasons[i]);
                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);
                                        }
                                    })
                                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                            AlertDialog diag = builder.create();
                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                            diag.show();
                            return;
                        } else if (type.equals(AdminCardViewAdapter.USER_BUTTON)) {
                            String uid = request.uid;
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    user = dataSnapshot.getValue(User.class);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(AdminViewRequestsActivity.this, R.style.DialogCustom))
                                            .setTitle("User Details")
                                            .setMessage("Name: " + user.name + "\nEmail: " + user.email + "\nPhone Number: " + user.phoneNumber)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            });
                                    AlertDialog diag = builder.create();
                                    diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                                    diag.show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //Toast.makeText(getApplicationContext(), "Failed - " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        } else if (type.equals(AdminCardViewAdapter.SHARE_BUTTON)) {
                            String uid = request.uid;
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    user = dataSnapshot.getValue(User.class);
                                    String text = "";
                                    text += "Name: "+user.name+" ";
                                    text += "Location: "+request.address+" ";
                                    text += "Wash Type: "+request.wash+" ";
                                    text += "Vehicle: "+request.vehicle+" ";
                                    text += "Vehicle Amount: "+request.vehicleAmount+" ";
                                    text += "Date/Time: "+ DateUtils.formatDateTime(getApplicationContext(), Long.parseLong(request.time), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE)+"";

                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                                    startActivity(Intent.createChooser(shareIntent, "Share Using"));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //Toast.makeText(getApplicationContext(), "Failed - " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        } else if (type.equals(AdminCardViewAdapter.COMPLETED_BUTTON)) {
                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("COMPLETED");
                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("COMPLETED");
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                            return;
                        } else if (type.equals(AdminCardViewAdapter.CANCELLED_BUTTON)) {
                            final String[] reasons = {"Time Unavailable", "eWash is not in your Area", "Too many requests from User"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(AdminViewRequestsActivity.this, R.style.DialogCustom))
                                    .setTitle("User Details")
                                    .setItems(reasons, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("CANCELLED - "+reasons[i]);
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("CANCELLED - "+reasons[i]);
                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);
                                        }
                                    })
                                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                            AlertDialog diag = builder.create();
                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                            diag.show();
                        }

                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Collections.sort(userRequests, new Comparator<PendingRequest>() {
                        //@RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        public int compare(long x, long y) {
                            return (x < y) ? -1 : ((x == y) ? 0 : 1);
                        }
                        @Override
                        public int compare(PendingRequest pendingRequest, PendingRequest t1) {
                            return compare(Long.parseLong(t1.time), Long.parseLong(pendingRequest.time));
                        }
                    });
                }
                // if(reqType.equals("ACCEPTED")){
                //    Collections.reverse(userRequests);
                //}

                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                processingDialog.dismiss();
            }
        });
    }

    public void reloadRecyclerViewFirst(){
        recyclerView.setAdapter(null);
        processingDialog.show();
        userRequests.clear();
        Query requests = FirebaseDatabase.getInstance().getReference("requests");
        requests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                processingDialog.dismiss();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PendingRequest request = snapshot.getValue(PendingRequest.class);
                    if (request.status.contains(reqType)) {
                        userRequests.add(request);
                    }
                }
                AdminCardViewAdapter adapter = new AdminCardViewAdapter(AdminViewRequestsActivity.this, userRequests, new RecyclerClickListener() {
                    @Override
                    public void onLongClick(int position, String type) {
                    }

                    User user;
                    @Override
                    public void onClick(int position, String type) {
                        final PendingRequest request = userRequests.get(position);

                        if (type.equals(AdminCardViewAdapter.ACCEPT_BUTTON)) {
                            View editView = View.inflate(AdminViewRequestsActivity.this, R.layout.edittext_dialog, null);
                            final TextInputEditText textInputEditText = editView.findViewById(R.id.edit_text);
                            textInputEditText.setHint("Washer Name(s)");
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(AdminViewRequestsActivity.this, R.style.DialogCustom))
                                    .setTitle("Washer Name(s):")
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
                                            String washer = textInputEditText.getText().toString();
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("ACCEPTED");
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("ACCEPTED");
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("washerName").setValue(washer);
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("washerName").setValue(washer);
                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);
                                        }
                                    });
                            AlertDialog diag = builder.create();
                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                            diag.show();
                            return;

                        } else if (type.equals(AdminCardViewAdapter.REJECT_BUTTON)) {
                            final String[] reasons = {"Time Unavailable", "eWash is not in your Area", "Too many requests from User"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(AdminViewRequestsActivity.this, R.style.DialogCustom))
                                    .setTitle("Reason for Rejection:")
                                    .setItems(reasons, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("REJECTED - "+reasons[i]);
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("REJECTED - "+reasons[i]);
                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);
                                        }
                                    })
                                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                            AlertDialog diag = builder.create();
                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                            diag.show();
                            return;
                        } else if (type.equals(AdminCardViewAdapter.USER_BUTTON)) {
                            String uid = request.uid;
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    user = dataSnapshot.getValue(User.class);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(AdminViewRequestsActivity.this, R.style.DialogCustom))
                                            .setTitle("User Details")
                                            .setMessage("Name: " + user.name + "\nEmail: " + user.email + "\nPhone Number: " + user.phoneNumber)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            });
                                    AlertDialog diag = builder.create();
                                    diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                                    diag.show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //Toast.makeText(getApplicationContext(), "Failed - " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        } else if (type.equals(AdminCardViewAdapter.SHARE_BUTTON)) {
                            String uid = request.uid;
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    user = dataSnapshot.getValue(User.class);
                                    String text = "";
                                    text += "Name: "+user.name+" ";
                                    text += "Location: "+request.address+" ";
                                    text += "Wash Type: "+request.wash+" ";
                                    text += "Vehicle: "+request.vehicle+" ";
                                    text += "Vehicle Amount: "+request.vehicleAmount+" ";
                                    text += "Date/Time: "+ DateUtils.formatDateTime(getApplicationContext(), Long.parseLong(request.time), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE)+"";

                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                                    startActivity(Intent.createChooser(shareIntent, "Share Using"));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //Toast.makeText(getApplicationContext(), "Failed - " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        } else if (type.equals(AdminCardViewAdapter.COMPLETED_BUTTON)) {
                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("COMPLETED");
                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("COMPLETED");
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                            return;
                        } else if (type.equals(AdminCardViewAdapter.CANCELLED_BUTTON)) {
                            final String[] reasons = {"Time Unavailable", "eWash is not in your Area", "Too many requests from User"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(AdminViewRequestsActivity.this, R.style.DialogCustom))
                                    .setTitle("User Details")
                                    .setItems(reasons, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FirebaseDatabase.getInstance().getReference("requests").child(request.requestId).child("status").setValue("CANCELLED - "+reasons[i]);
                                            FirebaseDatabase.getInstance().getReference("users").child(request.uid).child("history").child(request.requestId).child("status").setValue("CANCELLED - "+reasons[i]);
                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);
                                        }
                                    })
                                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                            AlertDialog diag = builder.create();
                            diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                            diag.show();
                        }

                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Collections.sort(userRequests, new Comparator<PendingRequest>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public int compare(PendingRequest pendingRequest, PendingRequest t1) {
                            return Long.compare(Long.parseLong(t1.time), Long.parseLong(pendingRequest.time));
                        }
                    });
                }
                // if(reqType.equals("ACCEPTED")){
                //    Collections.reverse(userRequests);
                //}
                if(userRequests.isEmpty()){
                    findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
                }

                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                processingDialog.dismiss();
            }
        });
    }
}
