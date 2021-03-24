package com.example.petdb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String name, tel;
    double lat, lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ActionBar bar=getSupportActionBar();
        bar.setTitle("동물병원 위치 보기");
        bar.setDisplayHomeAsUpEnabled(true);

        Intent intent=getIntent();
        name=intent.getStringExtra("Name");
        tel=intent.getStringExtra("Tel");
        lat=intent.getDoubleExtra("Lat",0.0);// 숫자는 항상 디폴트 값이 있어야함
        lng=intent.getDoubleExtra("Lng", 0.0);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapMove(lat, lat, name, tel);
    }

    //지도 메서드
    void mapMove(double lat, double lng, String name, String tel){
        LatLng latlng=new LatLng(lat, lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
        MarkerOptions options=new MarkerOptions();
        options.position(latlng);
        options.title(name);
        options.snippet(tel);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.map));
        mMap.addMarker(options).showInfoWindow();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Uri uri= Uri.parse("tel:" + tel);
                Intent intent=new Intent(Intent.ACTION_DIAL, uri);
                startActivity(intent);
                return false;
            }
        });
    }
}