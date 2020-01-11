package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class HomeActivity extends AppCompatActivity implements LocationListener {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    String uri="";
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    void resetpass()
    {
        Button send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = editText.getText().toString().trim();
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, pass);
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                final EditText editText1 = findViewById(R.id.editresetnew);
                                String newPass = editText1.getText().toString().trim();
                                final String TAG = "yo";
                                if (task.isSuccessful()) {
                                    user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Password updated");
                                            } else {
                                                Log.d(TAG, "Error password not updated");
                                            }
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "Error auth failed");
                                }
                            }
                        });
            }
        });

    }
    private EditText editText, editTextnew;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menuLogout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.resetpass:
                editText.setVisibility(View.VISIBLE);
                editTextnew.setVisibility(View.VISIBLE);
                resetpass();
                break;

        }
        return true;
    }

    private void SendMessage(final User user)
    {
        final SmsManager smsManager = SmsManager.getDefault();
        final StringBuffer smsBody = new StringBuffer();
//        smsBody.append(Uri.parse(uri));
//        smsBody.append("\n\n\n HELP!");
        final FirebaseUser userr = FirebaseAuth.getInstance().getCurrentUser();
        String userrr = userr.getUid();

        final String sharelinktext  = "https://womensafetyapp.page.link/?"+
                "link=https://www.example.com/example.php?user="+userrr+
                "&apn="+ getPackageName()+
                "&st="+"My Refer Link"+
                "&sd="+"Reward Coins 20"+
                "&si="+"https://www.blueappsoftware.com/logo-1.png";

        final Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                //.setLongLink(dynamicLink.getUri())
                .setLongLink(Uri.parse(sharelinktext))  // manually
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                            @Override
                            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                if (task.isSuccessful()) {
                                    // Short link created
                                    Uri shortLink = task.getResult().getShortLink();
                                    Uri flowchartLink = task.getResult().getPreviewLink();
                                    Log.e("main ", "short link " + shortLink + "\n text"+sharelinktext);
                                    smsManager.sendTextMessage(user.contact1,null,shortLink.toString(),null,null);
                                    // share app dialog

                                } else {
                                    // Error
                                    // ...
                                    Log.e("main", " error " + task.getException());
                                }
                            }
                        });


//        smsManager.sendTextMessage(user.contact2,null,smsBody.toString(),null,null);
//        smsManager.sendTextMessage(user.contact3,null,smsBody.toString(),null,null);
//        Log.e("SendMessage",uri+"000");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case 0:
            case 1:
            case 2:
                if(grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){}
                else
                    request();
                return;
        }
    }


    public void request()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},0);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},2);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        editText = findViewById(R.id.editreset);
        editTextnew = findViewById(R.id.editresetnew);
        editText.setVisibility(View.INVISIBLE);
        editTextnew.setVisibility(View.INVISIBLE);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},0);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},2);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        final Button send =(Button) findViewById(R.id.send);
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference mReference = mDatabase.getReference("Users");
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        SendMessage(user);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //FirebaseDatabaseHelper use krna ho agar
//                new FirebaseDatabaseHelper().extract(new FirebaseDatabaseHelper.DataStatus() {
//                    @Override
//                    public void DataIsLoaded(User user) {
//                        send.setText(""+user.contact1);
//                    }
//
//                    @Override
//                    public void DataIsInserted() {
//
//                    }
//
//                    @Override
//                    public void DataIsUpdated() {
//
//                    }
//
//                    @Override
//                    public void DataIsDeleted() {
//
//                    }
//                });
            }
        });
    }

    Double lat,lon;

    @Override
    public void onLocationChanged(Location currentLocation) {
        uri = "http://maps.google.com/maps?daddr=" + currentLocation.getLatitude()+","+currentLocation.getLongitude();
        lat = currentLocation.getLatitude();
        lon = currentLocation.getLongitude();
        final FirebaseUser userr = FirebaseAuth.getInstance().getCurrentUser();
        String userrr = userr.getUid();
        myRef.child(userrr).child("lat").setValue(""+lat);
        myRef.child(userrr).child("lon").setValue(""+lon);
//        Log.e("onLocationChanged",uri);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
