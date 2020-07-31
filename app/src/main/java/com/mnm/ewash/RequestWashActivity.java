package com.mnm.ewash;


import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alium.nibo.models.NiboSelectedPlace;
import com.alium.nibo.placepicker.NiboPlacePickerActivity;
import com.alium.nibo.utils.NiboConstants;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mnm.ewash.models.PendingRequest;
import com.mnm.ewash.utils.OnMapAndViewReadyListener;
import com.pixplicity.easyprefs.library.Prefs;
import com.takisoft.datetimepicker.DatePickerDialog;
import com.takisoft.datetimepicker.widget.DatePicker;

import java.util.Calendar;

import mehdi.sakout.fancybuttons.FancyButton;

public class RequestWashActivity extends AppCompatActivity {
    public static final String VEHICLE_CAR = "Car";
    public static final String VEHICLE_TAXI = "Taxi";
    public static final String VEHICLE_MINIBUS = "Minibus";
    public static final String VEHICLE_BUS = "Bus";
    public static final String VEHICLE_TRUCK = "Truck";

    public static final String SERVICE_STANDARD = "Standard Wash";
    public static final String SERVICE_EXECUTIVE = "Executive Wash";
    public static final String SERVICE_VALET = "Valet Wash";

    public String selectedWash = SERVICE_STANDARD;
    public String selectedVehicle = VEHICLE_CAR;
    public int vehicleAmount = 1;

    Toolbar toolbar;
    SupportMapFragment locationPreview;
    GoogleMap previewMap;
    TextView locationAddress;
    FancyButton changeLocation;

    FancyButton standardWash;
    FancyButton executiveWash;
    FancyButton valetWash;

    TextView standardInfoText;
    ImageView standardInfoBtn;

    CardView serviceTypeCard;

    TextView executiveInfoText;
    ImageView executiveInfoBtn;

    TextView valetInfoText;
    ImageView valetInfoBtn;

    FancyButton carWash;
    FancyButton taxiWash;
    FancyButton minibusWash;
    FancyButton busWash;
    FancyButton truckWash;

    FancyButton vehicles1;
    FancyButton vehicles2;
    FancyButton vehicles3;
    FancyButton vehicles4;

    TextView totalAmount;
    FancyButton submitRequest;

    Calendar calendar = Calendar.getInstance();
    FancyButton changeDate;
    FancyButton changeTime;
    TextView dateText;
    TextView timeText;

    LatLng currentLatLng;
    long longTime;
    GoogleApiClient mGoogleApiClient;

    final int[] times = {7, 8, 9, 10, 11, 12, 13 ,14, 15, 16, 17};
    String[] stringTimes = {
            "07:00",
            "07:15",
            "07:30",
            "07:45",
            "08:00",
            "08:15",
            "08:30",
            "08:45",
            "09:00",
            "09:15",
            "09:30",
            "09:45",
            "10:00",
            "10:15",
            "10:30",
            "10:45",
            "11:00",
            "11:15",
            "11:30",
            "11:45",
            "12:00",
            "12:15",
            "12:30",
            "12:45",
            "13:00",
            "13:15",
            "13:30",
            "13:45",
            "14:00",
            "14:15",
            "14:30",
            "14:45",
            "15:00",
            "15:15",
            "15:30",
            "15:45",
            "16:00",
            "16:15",
            "16:30",
            "16:45",
            "17:00"};


    public JobTrigger periodicTrigger(int frequency, int tolerance){
        return Trigger.executionWindow(frequency - tolerance, frequency);
    }

    FirebaseAuth mAuth;
    AlertDialog processingDialog;
    //DatabaseReference databaseReference;
    int PLACE_PICKER_REQUEST = 4500;
    int NIBO_PICKER_REQUEST = 3500;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_wash);
        mAuth = FirebaseAuth.getInstance();
        calendar.set(Calendar.MINUTE, 0);
       /* NetworkUtils.isNetworkConnected(RequestWashActivity.this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });*/
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 390);
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestWashActivity.this, R.style.DialogCustom))
                .setTitle("Processing...")
                .setView(R.layout.processing_layout)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                    }
                })
                .setCancelable(true);
        processingDialog = builder.create();
        processingDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addApi(Places.GEO_DATA_API)
        .addApi(Places.PLACE_DETECTION_API)
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {

            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        })
        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            }
        }).build();*/
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        locationPreview = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_preview);
        new OnMapAndViewReadyListener(locationPreview, new OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                previewMap = googleMap;
            }
        });
        locationAddress = findViewById(R.id.location_address);
        changeLocation = findViewById(R.id.btn_change_location);

        standardWash = findViewById(R.id.btn_standard_wash);
        executiveWash = findViewById(R.id.btn_executive_wash);
        valetWash = findViewById(R.id.btn_valet_wash);

        carWash = findViewById(R.id.btn_car_wash);
        taxiWash = findViewById(R.id.btn_taxi_wash);
        minibusWash = findViewById(R.id.btn_minibus_wash);
        busWash = findViewById(R.id.btn_bus_wash);
        truckWash = findViewById(R.id.btn_truck_wash);

        vehicles1 = findViewById(R.id.btn_vehicles_1);
        vehicles2 = findViewById(R.id.btn_vehicles_2);
        vehicles3 = findViewById(R.id.btn_vehicles_3);
        vehicles4 = findViewById(R.id.btn_vehicles_4);

        totalAmount = findViewById(R.id.total_amount);
        submitRequest = findViewById(R.id.btn_submit_request);

        changeDate = findViewById(R.id.btn_change_date);
        changeTime = findViewById(R.id.btn_change_time);
        dateText = findViewById(R.id.date);
        timeText = findViewById(R.id.time);

        standardInfoText = findViewById(R.id.standard_wash_info_text);
        standardInfoBtn = findViewById(R.id.standard_wash_info_btn);

        executiveInfoText = findViewById(R.id.executive_wash_info_text);
        executiveInfoBtn = findViewById(R.id.executive_wash_info_btn);

        valetInfoText = findViewById(R.id.valet_wash_info_text);
        valetInfoBtn = findViewById(R.id.valet_wash_info_btn);

        ((ViewGroup)findViewById(R.id.service_type_layout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup)findViewById(R.id.service_type_card)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        ((ViewGroup)findViewById(R.id.main_layout)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        //((ViewGroup)findViewById(R.id.service_type_layout)).getLayoutTransition().enableTransitionType(LayoutTransition.APPEARING);
        //((ViewGroup)findViewById(R.id.service_type_layout)).getLayoutTransition().enableTransitionType(LayoutTransition.DISAPPEARING);

        standardInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TransitionManager.beginDelayedTransition(serviceTypeCard);
                executiveInfoText.setVisibility(View.GONE);
                valetInfoText.setVisibility(View.GONE);
                standardInfoText.setVisibility(View.VISIBLE);
            }
        });
        executiveInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TransitionManager.beginDelayedTransition(serviceTypeCard);
                standardInfoText.setVisibility(View.GONE);
                valetInfoText.setVisibility(View.GONE);
                executiveInfoText.setVisibility(View.VISIBLE);
            }
        });
        valetInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TransitionManager.beginDelayedTransition(serviceTypeCard);
                executiveInfoText.setVisibility(View.GONE);
                standardInfoText.setVisibility(View.GONE);
                valetInfoText.setVisibility(View.VISIBLE);
            }
        });

        timeText.setText(DateUtils.formatDateTime(getApplicationContext(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
        dateText.setText(DateUtils.formatDateTime(getApplicationContext(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE));

        currentLatLng = null;
        locationAddress.setText("Choose Location");

        changeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(RequestWashActivity.this);
                datePickerDialog.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar comp = Calendar.getInstance();
                        comp.set(Calendar.YEAR, year);
                        comp.set(Calendar.MONTH, month);
                        comp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        if(!comp.before(Calendar.getInstance())){
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            dateText.setText(DateUtils.formatDateTime(getApplicationContext(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE));
                        }else{
                            Toast.makeText(getApplicationContext(), "Date cannot be before Today", Toast.LENGTH_SHORT ).show();
                            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                            calendar.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
                            calendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                            dateText.setText(DateUtils.formatDateTime(getApplicationContext(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE));
                        }
                    }
                });
                datePickerDialog.show();
            }
        });

        int mtime = getNearestInt(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), times);
        calendar.set(Calendar.HOUR_OF_DAY, mtime);
        calendar.set(Calendar.MINUTE, 0);
        timeText.setText(DateUtils.formatDateTime(getApplicationContext(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
        changeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, stringTimes){
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        TextView textView = (TextView) super.getView(position, convertView, parent);
                        textView.setTextColor(Color.WHITE);
                        textView.setGravity(Gravity.CENTER);
                        //textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                        return textView;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestWashActivity.this, R.style.DialogCustom))
                        .setTitle("Choose Time").setCancelable(false)
                        .setSingleChoiceItems(arrayAdapter, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String time = stringTimes[i];
                                int hour = Integer.valueOf(time.split(":")[0]);
                                int minute = Integer.valueOf(time.split(":")[1]);
                                Log.i("eWash", "Time: "+hour+":"+minute);
                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);
                                timeText.setText(DateUtils.formatDateTime(getApplicationContext(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog diag = builder.create();
                diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                diag.show();
            }
        });

        standardWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectWash(standardWash);
            }
        });
        executiveWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectWash(executiveWash);
            }
        });
        valetWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectWash(valetWash);
            }
        });

        carWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectVehicle(carWash);
            }
        });
        taxiWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectVehicle(taxiWash);
            }
        });
        minibusWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectVehicle(minibusWash);
            }
        });
        busWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectVehicle(busWash);
            }
        });
        truckWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectVehicle(truckWash);
            }
        });

        vehicles1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVehicleAmount(1, vehicles1);
            }
        });
        vehicles2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVehicleAmount(2, vehicles2);
            }
        });
        vehicles3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVehicleAmount(3, vehicles3);
            }
        });
        vehicles4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVehicleAmount(4, vehicles4);
            }
        });

        changeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                boolean gps_enabled = false;

                try{
                    gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                }catch (Exception e){
                    gps_enabled = true;
                    e.printStackTrace();
                }

                if(!gps_enabled) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(RequestWashActivity.this, R.style.DialogCustom))
                            .setTitle("Current Location")
                            .setMessage("Enable Location Services for Current Location")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                                        startActivityForResult(builder.build(RequestWashActivity.this), PLACE_PICKER_REQUEST);
                                    } catch (GooglePlayServicesRepairableException e) {
                                        e.printStackTrace();
                                        Intent intent = new Intent(RequestWashActivity.this, NiboPlacePickerActivity.class);
                                        NiboPlacePickerActivity.NiboPlacePickerBuilder nbuilder = new NiboPlacePickerActivity.NiboPlacePickerBuilder()
                                                .setSearchBarTitle("Search For Address")
                                                .setConfirmButtonTitle("Choose This Location");
                                        NiboPlacePickerActivity.setBuilder(nbuilder);
                                        startActivityForResult(intent, NIBO_PICKER_REQUEST);
                                    } catch (GooglePlayServicesNotAvailableException e) {
                                        e.printStackTrace();
                                        Intent intent = new Intent(RequestWashActivity.this, NiboPlacePickerActivity.class);
                                        NiboPlacePickerActivity.NiboPlacePickerBuilder nbuilder = new NiboPlacePickerActivity.NiboPlacePickerBuilder()
                                                .setSearchBarTitle("Search For Address")
                                                .setConfirmButtonTitle("Choose This Location");
                                        NiboPlacePickerActivity.setBuilder(nbuilder);
                                        startActivityForResult(intent, NIBO_PICKER_REQUEST);
                                    }
                                }
                            });
                    dialog.show();
                }else{*/
                    try {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        startActivityForResult(builder.build(RequestWashActivity.this), PLACE_PICKER_REQUEST);
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                        Intent intent = new Intent(RequestWashActivity.this, NiboPlacePickerActivity.class);
                        NiboPlacePickerActivity.NiboPlacePickerBuilder nbuilder = new NiboPlacePickerActivity.NiboPlacePickerBuilder()
                                .setSearchBarTitle("Search For Address")
                                .setConfirmButtonTitle("Choose This Location");
                        NiboPlacePickerActivity.setBuilder(nbuilder);
                        startActivityForResult(intent, NIBO_PICKER_REQUEST);
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                        Intent intent = new Intent(RequestWashActivity.this, NiboPlacePickerActivity.class);
                        NiboPlacePickerActivity.NiboPlacePickerBuilder nbuilder = new NiboPlacePickerActivity.NiboPlacePickerBuilder()
                                .setSearchBarTitle("Search For Address")
                                .setConfirmButtonTitle("Choose This Location");
                        NiboPlacePickerActivity.setBuilder(nbuilder);
                        startActivityForResult(intent, NIBO_PICKER_REQUEST);
                    }
                //}
            }
        });
        submitRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLatLng == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestWashActivity.this, R.style.DialogCustom))
                            .setTitle("Location")
                            .setMessage("Please choose a Location")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                    AlertDialog diag = builder.create();
                    diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                    diag.show();
                    return;
                }
                processingDialog.setCancelable(false);
                processingDialog.show();
                longTime = calendar.getTimeInMillis();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("requests");
                final String requestId = databaseReference.push().getKey();
                final PendingRequest pendingRequest = new PendingRequest(requestId, "" + mAuth.getCurrentUser().getUid(), currentLatLng.latitude + "", currentLatLng.longitude + "", locationAddress.getText().toString(), longTime + "", selectedWash, selectedVehicle, totalAmount.getText().toString(), "PENDING", vehicleAmount+"", "");
                databaseReference.child(requestId).setValue(pendingRequest);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //processingDialog.dismiss();
                        DatabaseReference userHistory = FirebaseDatabase.getInstance().getReference("users");
                        userHistory.child(mAuth.getCurrentUser().getUid()).child("history").child(requestId).setValue(pendingRequest);

                        Prefs.putString("REQ_ID", requestId);
                        Prefs.putString("REQ_STATUS", pendingRequest.status);
                        try {
                            FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
                            jobDispatcher.cancelAll();
                            Job job = jobDispatcher.newJobBuilder()
                                    .setService(RequestJobService.class)
                                    .setTag(RequestJobService.TAG)
                                    .setRecurring(true)
                                    .setLifetime(Lifetime.FOREVER)
                                    .setTrigger(periodicTrigger(20, 1))
                                    .setReplaceCurrent(true)
                                    .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                                    .setConstraints(Constraint.ON_ANY_NETWORK)
                                    .build();
                            jobDispatcher.mustSchedule(job);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        processingDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestWashActivity.this, R.style.DialogCustom))
                                .setTitle("eWash Request")
                                .setMessage("Your car wash request has been submitted. Check back later to view the status of your request. We'll keep you updated with any changes.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                        finish();
                                        startActivity(intent);
                                    }
                                });
                        AlertDialog diag = builder.create();
                        diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                        diag.show();
                        /*final InterstitialAd interstitialAd = new InterstitialAd(RequestWashActivity.this);
                        interstitialAd.setAdUnitId("ca-app-pub-8736882148099200/7936865199");
                        interstitialAd.loadAd(new AdRequest.Builder().build());
                        interstitialAd.setAdListener(new AdListener(){
                            @Override
                            public void onAdLoaded() {
                                super.onAdLoaded();
                                processingDialog.dismiss();
                                processingDialog.setCancelable(true);
                                interstitialAd.show();
                            }

                            @Override
                            public void onAdFailedToLoad(int i) {
                                super.onAdFailedToLoad(i);
                                processingDialog.dismiss();
                                processingDialog.setCancelable(true);
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestWashActivity.this, R.style.DialogCustom))
                                        .setTitle("eWash Request")
                                        .setMessage("Your Car Wash request has been submitted. Check back later to view the status of your request.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                finish();

                                                startActivity(intent);
                                            }
                                        });
                                builder.show();
                            }

                            @Override
                            public void onAdClosed() {
                                super.onAdClosed();
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RequestWashActivity.this, R.style.DialogCustom))
                                        .setTitle("eWash Request")
                                        .setMessage("Your Car Wash request has been submitted. Check back later to view the status of your request.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                finish();
                                                startActivity(intent);
                                            }
                                        });
                                builder.show();
                            }
                        });*/
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        processingDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Failed - "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        databaseError.toException().printStackTrace();
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });
            }
        });
    }

    public int getNearestInt(int myNumber, int[] numbers){
        int distance = Math.abs(numbers[0] - myNumber);
        int idx = 0;
        for(int c = 1; c < numbers.length; c++){
            int cdistance = Math.abs(numbers[c] - myNumber);
            if(cdistance < distance){
                idx = c;
                distance = cdistance;
            }
        }
        return numbers[idx];
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == NIBO_PICKER_REQUEST){
            NiboSelectedPlace selectedPlace = data.getParcelableExtra(NiboConstants.SELECTED_PLACE_RESULT);
            currentLatLng = selectedPlace.getLatLng();
            locationAddress.setText(selectedPlace.getPlaceAddress());
            if(previewMap != null){
                previewMap.clear();
                previewMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 20f));
                previewMap.addMarker(new MarkerOptions().position(currentLatLng));
            }
            /*String url = "http://maps.google.com/maps/api/staticmap?center="+currentLatLng.latitude+","+currentLatLng.longitude+"&markers="+currentLatLng.latitude+","+currentLatLng.longitude+"&zoom=15&size=400x250&sensor=false";
            Glide.with(RequestWashActivity.this).load(url).into(locationPreview);*/
        }else if(resultCode == Activity.RESULT_OK && requestCode == PLACE_PICKER_REQUEST){
            Place place = PlacePicker.getPlace(this, data);
            if(place != null){
                currentLatLng = place.getLatLng();
                locationAddress.setText(place.getAddress());
                if(previewMap != null){
                    previewMap.clear();
                    previewMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f));
                    previewMap.addMarker(new MarkerOptions().position(currentLatLng));
                }
                /*String url = "http://maps.google.com/maps/api/staticmap?center="+currentLatLng.latitude+","+currentLatLng.longitude+"&markers="+currentLatLng.latitude+","+currentLatLng.longitude+"&zoom=15&size=400x250&sensor=false";
                Glide.with(RequestWashActivity.this).load(url).into(locationPreview);*/
            }else{
                Toast.makeText(getApplicationContext(), "Please choose a location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setVehicleAmount(int amount, FancyButton fancyButton){
        vehicleAmount = amount;
        vehicles1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        vehicles2.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        vehicles3.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        vehicles4.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        fancyButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        updateTotalAmount();
    }

    private void selectWash(FancyButton fancyButton) {
        standardWash.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        executiveWash.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        valetWash.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        if (fancyButton == standardWash) {
            selectedWash = SERVICE_STANDARD;
        } else if (fancyButton == executiveWash) {
            selectedWash = SERVICE_EXECUTIVE;
        } else if (fancyButton == valetWash) {
            selectedWash = SERVICE_VALET;
        }
        fancyButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        updateTotalAmount();
    }

    private void selectVehicle(FancyButton fancyButton) {
        carWash.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        taxiWash.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        minibusWash.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        busWash.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        truckWash.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight));
        if (fancyButton == carWash) {
            selectedVehicle = VEHICLE_CAR;
        } else if (fancyButton == taxiWash) {
            selectedVehicle = VEHICLE_TAXI;
        } else if (fancyButton == minibusWash) {
            selectedVehicle = VEHICLE_MINIBUS;
        } else if (fancyButton == busWash) {
            selectedVehicle = VEHICLE_BUS;
        } else if (fancyButton == truckWash) {
            selectedVehicle = VEHICLE_TRUCK;
        }
        fancyButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        updateTotalAmount();
    }

    private void updateTotalAmount() {
        if (selectedWash.contains(SERVICE_STANDARD)) {
            switch (selectedVehicle) {
                case VEHICLE_CAR:
                    totalAmount.setText("R"+70*vehicleAmount);
                    break;
                case VEHICLE_TAXI:
                    totalAmount.setText("R"+80*vehicleAmount);
                    break;
                case VEHICLE_MINIBUS:
                    totalAmount.setText("R"+90*vehicleAmount);
                    break;
                case VEHICLE_BUS:
                    totalAmount.setText("R"+100*vehicleAmount);
                    break;
                case VEHICLE_TRUCK:
                    totalAmount.setText("R"+130*vehicleAmount);
                    break;
            }
        } else if (selectedWash.contains(SERVICE_EXECUTIVE)) {
            switch (selectedVehicle) {
                case VEHICLE_CAR:
                    totalAmount.setText("R"+90*vehicleAmount);
                    break;
                case VEHICLE_TAXI:
                    totalAmount.setText("R"+100*vehicleAmount);
                    break;
                case VEHICLE_MINIBUS:
                    totalAmount.setText("R"+120*vehicleAmount);
                    break;
                case VEHICLE_BUS:
                    totalAmount.setText("R"+140*vehicleAmount);
                    break;
                case VEHICLE_TRUCK:
                    totalAmount.setText("R"+160*vehicleAmount);
                    break;
            }
        } else if (selectedWash.contains(SERVICE_VALET)) {
            switch (selectedVehicle) {
                case VEHICLE_CAR:
                    totalAmount.setText("R"+600*vehicleAmount);
                    break;
                case VEHICLE_TAXI:
                    totalAmount.setText("R"+700*vehicleAmount);
                    break;
                case VEHICLE_MINIBUS:
                    totalAmount.setText("R"+800*vehicleAmount);
                    break;
                case VEHICLE_BUS:
                    totalAmount.setText("R"+900*vehicleAmount);
                    break;
                case VEHICLE_TRUCK:
                    totalAmount.setText("R"+1000*vehicleAmount);
                    break;
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 390){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
               Intent intent = getIntent();
                finish();
               startActivity(intent);
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getBaseContext(), R.style.DialogCustom))
                        .setTitle("Permissions")
                        .setMessage("Location & Internet permissions are required to send a request.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                            }
                        });
                try {
                    AlertDialog diag = builder.create();
                    diag.getWindow().getAttributes().windowAnimations = R.style.DialogAnim;
                    diag.show();
                }catch (Exception e){
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        if(processingDialog.isShowing()){
            processingDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        }
    }
}