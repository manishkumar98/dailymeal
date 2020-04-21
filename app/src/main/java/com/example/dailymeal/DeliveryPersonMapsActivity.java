package com.example.dailymeal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
/*import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;*/
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.internal.ICameraUpdateFactoryDelegate;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class DeliveryPersonMapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private int status = 0;
    //private float rideDistance;
    private Button mLogout,mSettings,mDeliveryStatus,mHistory;
    private Switch mWorkingSwitch;
    private String mName;
    private String CustomerId=" ";
    private boolean isLoggingout=false;
    private SupportMapFragment mapFragment;
    private LinearLayout mCustomerInfo;
    private TextView mCustomerName, mCustomerPhone, mService;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private DatabaseReference mCustomerDatabase;
    Marker customerMarker;
    private LatLng customerLatLong;
    private Marker pickupMarker;
    private Boolean requestBol = false;
    private float rideDistance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_person_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);



        mCustomerName = (TextView) findViewById(R.id.customerName);
        mCustomerPhone = (TextView) findViewById(R.id.customerPhone);
        mService = (TextView) findViewById(R.id.service);
        mSettings=(Button)findViewById(R.id.settings);
        mWorkingSwitch=(Switch)findViewById(R.id.workingSwitch);
        mHistory = (Button) findViewById(R.id.history);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    connectDriver();
                }else{
                    disconnectdriver();
                }
            }
        });
        mLogout=(Button)findViewById(R.id.logout);
        mDeliveryStatus=(Button)findViewById(R.id.foodDeliveryStatusStatus);
        mDeliveryStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(status){
                    case 1:
                        status=2;
                        erasePolylines();

                        mDeliveryStatus.setText("Food Delivered");

                        break;
                    case 2:
                       recordDelivery();
                        endDelivery();
                        break;
                }
            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingout=true;
                disconnectdriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(DeliveryPersonMapsActivity.this,MainActivity.class);
                startActivity(intent);
                finish();

            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DeliveryPersonMapsActivity.this,DeliveryPersonSettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryPersonMapsActivity.this, HistoryActivity.class);
                intent.putExtra("customerordriver", "deliveryperson");
                startActivity(intent);
                return;
            }
        });


        getAssignedCustomer();

    }

    private void getAssignedCustomer(){
        String deliveryPersonId=FirebaseAuth.getInstance().getUid();
        DatabaseReference assignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("deliveryperson").child(deliveryPersonId).child("customerid");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override


            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    status = 1;
                    CustomerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerLocation();
                  //  getfoodtype();

                    getAssignedCustomerInfo();
                } else {
                  endDelivery();

                }
            }
           /*   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mCustomerName.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!=null){
                        mCustomerPhone.setText(map.get("phone").toString());
                    }


                }
                //else{
               //     mCustomerInfo.setVisibility(View.GONE);
                //}
                */


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        } );
    }

    private void getAssignedCustomerInfo(){
        mCustomerInfo.setVisibility(View.VISIBLE);
        mAuth=FirebaseAuth.getInstance();//taking firebase instance
        CustomerId=mAuth.getCurrentUser().getUid();
        rootRef=FirebaseDatabase.getInstance().getReference();
        mCustomerDatabase= rootRef.child("Users").child("customer").child(CustomerId);
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map=(Map<String, Object>)dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName=map.get("name").toString();
                        mCustomerName.setText(mName);

                    }
                    if(map.get("phone")!=null){
                       mCustomerPhone.setText(map.get("phone").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

   /* private void DeliveryComplete(){
        mRideStatus.setText("picked customer");
        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
        driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId);
        customerId="";
        rideDistance = 0;

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (assignedCustomerPickupLocationRefListener != null){
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        mCustomerInfo.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerPhone.setText("");
        mCustomerDestination.setText("Destination: --");
        mCustomerProfileImage.setImageResource(R.mipmap.ic_default_user);
    }
    */
   private void recordDelivery(){
       String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
       DatabaseReference deliveryPersonRef = FirebaseDatabase.getInstance().getReference().child("Users").child("deliveryperson").child(userId).child("history");
       DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("customer").child(CustomerId).child("history");
       DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
       String requestId = historyRef.push().getKey();
       deliveryPersonRef.child(requestId).setValue(true);
       customerRef.child(requestId).setValue(true);

       HashMap map = new HashMap();
       map.put("deliveryperson", userId);
       map.put("customer", CustomerId);

       map.put("timestamp", getCurrentTimestamp());
       
     //  map.put("destination", destination);
       map.put("location/from/lat", customerLatLong.latitude);
       map.put("location/from/lng", customerLatLong.longitude);
     //  map.put("location/to/lat", destinationLatLng.latitude);
       //map.put("location/to/lng", destinationLatLng.longitude);



       map.put("distance", rideDistance);


       historyRef.child(requestId).updateChildren(map);
   }

    private Long getCurrentTimestamp() {
       Long timestamp=System.currentTimeMillis()/1000;
       return timestamp;
    }

    private void getAssignedCustomerLocation() {
        DatabaseReference assignedCustomerLocationRef = FirebaseDatabase.getInstance().getReference().child("customerrequest").child("customerid").child("1");
        assignedCustomerLocationRef.addValueEventListener(new ValueEventListener() {
                                                              @Override
                                                              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                  if (dataSnapshot.exists()) {
                                                                      List<Object> map = (List<Object>) dataSnapshot.getValue();
                                                                      double locationLat = 0;
                                                                      double locationLong = 0;

                                                                      if (map.get(0) != null) {
                                                                          locationLat = Double.parseDouble(map.get(0).toString());

                                                                      }
                                                                      if (map.get(1) != null) {
                                                                          locationLong = Double.parseDouble(map.get(1).toString());

                                                                      }
                                                                       customerLatLong = new LatLng(locationLat, locationLong);

                                                                     customerMarker= mMap.addMarker(new MarkerOptions().position(customerLatLong).title("Delivery Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.home_address)));
                                                                      getRouteToMarker(customerLatLong);
                                                                  }
                                                              }
                                       //                   }
       // );


        @Override
        public void onCancelled (@NonNull DatabaseError databaseError){

        }
    });
        }

    private void getRouteToMarker(LatLng customerLatLong) {
       if(customerLatLong!=null&&mLastLocation!=null) {
           Routing routing = new Routing.Builder().travelMode(AbstractRouting.TravelMode.DRIVING).withListener(this).alternativeRoutes(false).waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), customerLatLong).build();
           routing.execute();
       }


    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }
    }



   /* LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    if(!CustomerId.equals("")){
                        rideDistance+=mLastLocation.distanceTo(location)/1000;
                    }
                    mLastLocation=location;
                    LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(0b1011));


                    String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refAvailable= FirebaseDatabase.getInstance().getReference("deliverypersonavailable");
                    DatabaseReference refWorking= FirebaseDatabase.getInstance().getReference("deliverypersonworking");
                    GeoFire geoFireAvailable=new GeoFire(refAvailable);
                    GeoFire geoFireWorking=new GeoFire(refWorking);


                    switch (CustomerId){
                        case "":
                            geoFireWorking.removeLocation(userId);
                            geoFireAvailable.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                            break;
                        default:
                            geoFireAvailable.removeLocation(userId);
                            geoFireWorking.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                            break;

                    }



                }



                String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("deliverypersonavailable");
                GeoFire geoFire=new GeoFire(ref);
                geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
            }
        }
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };*/
   LocationCallback mLocationCallback = new LocationCallback(){
       @Override
       public void onLocationResult(LocationResult locationResult) {
           for(Location location : locationResult.getLocations()){
               if(getApplicationContext()!=null){

                   if(!CustomerId.equals("") && mLastLocation!=null && location != null){
                       rideDistance += mLastLocation.distanceTo(location)/1000;
                   }
                   mLastLocation = location;


                   LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                   mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                   mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                   String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                   DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("deliverypersonavailable");
                   DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("deliverypersonworking");
                   GeoFire geoFireAvailable = new GeoFire(refAvailable);
                   GeoFire geoFireWorking = new GeoFire(refWorking);

                   switch (CustomerId){
                       case "":
                           geoFireWorking.removeLocation(userId);
                           geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                           break;

                       default:
                           geoFireAvailable.removeLocation(userId);
                           geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                           break;
                   }
               }
           }
       }
   };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(DeliveryPersonMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(DeliveryPersonMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }





    
    private void connectDriver(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, (com.google.android.gms.location.LocationCallback) mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }


     private void disconnectdriver(){
         if(mFusedLocationClient != null){
             mFusedLocationClient.removeLocationUpdates((com.google.android.gms.location.LocationCallback) mLocationCallback);
         }
         String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
         DatabaseReference ref = FirebaseDatabase.getInstance().getReference("deliverypersonavailable");

         GeoFire geoFire = new GeoFire(ref);
         geoFire.removeLocation(userId);

     }

    @Override


    public void onStop(){
        super.onStop();
        if(!isLoggingout) {
        disconnectdriver();
        }
    }
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }
    private void endDelivery(){
        mDeliveryStatus.setText("Delivery Complete");
        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference deliverypersonref = FirebaseDatabase.getInstance().getReference().child("Users").child("deliveryperson").child(userId).child("customerrequest");
        deliverypersonref.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerrequest");
        GeoFire geoFire = new GeoFire(ref);
           geoFire.removeLocation(CustomerId);
        CustomerId="";
        rideDistance=0;


        if(pickupMarker != null){
            pickupMarker.remove();
        }




        mCustomerInfo.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerPhone.setText("");
        mService.setText("");
        
    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

}
