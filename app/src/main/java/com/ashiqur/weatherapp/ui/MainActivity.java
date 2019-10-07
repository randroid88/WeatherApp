package com.ashiqur.weatherapp.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ashiqur.weatherapp.ApiDataRepository;
import com.ashiqur.weatherapp.R;
import com.ashiqur.weatherapp.rest_api.models.WeatherDataModel;
import com.ashiqur.weatherapp.utils.ViewModelUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    private static final String TAG = "MainActivity";
    LocationManager locationManager;

    private MainActivityViewModel mainActivityViewModel;
    private TextView tvTemperature, tvDescription, tvCloud, tvWindSpeed;
    private Button btnAnyWeather;
    private ForecastsDataAdapter adapter;
    private Button btnDeviceLocationWeather;
    private EditText etCityName, etCountryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initPermissions();
        findDeviceLocation();

        Toast.makeText(getApplicationContext(), "Location:" + ApiDataRepository.latitude + "," + ApiDataRepository.longitude, Toast.LENGTH_LONG).show();
        Log.wtf(TAG, ApiDataRepository.latitude + "," + ApiDataRepository.longitude);

        mainActivityViewModel = (MainActivityViewModel) ViewModelUtils.GetViewModel(MainActivity.this, MainActivityViewModel.class);

        mainActivityViewModel.getCurrentWeatherData().observe(MainActivity.this, new Observer<WeatherDataModel>() {
            @Override
            public void onChanged(WeatherDataModel weatherDataModel) {
                tvTemperature.setText(weatherDataModel.getTemperature());
                tvDescription.setText(weatherDataModel.getDescription());
                tvCloud.setText("Cloud:" + weatherDataModel.getClouds() + "%");
                tvWindSpeed.setText("Wind Speed:" + weatherDataModel.getWindSpeed());
            }
        });
        mainActivityViewModel.getForecastsData().observe(this, new Observer<List<WeatherDataModel>>() {
            @Override
            public void onChanged(List<WeatherDataModel> weatherDataModels) {
                adapter.setNotes(weatherDataModels);
            }
        });


    }

    private void initPermissions() {
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

    }

    private void findDeviceLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Check gps is enable or not

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Write Function To enable gps

            OnGPS();
        } else {
            //GPS is already On then

            getLocation();
        }
    }

    private void initViews() {
        etCityName = findViewById(R.id.et_city_name);
        etCountryId = findViewById(R.id.et_country_id);

        tvTemperature = findViewById(R.id.tv_temperature);
        tvCloud = findViewById(R.id.tv_cloud);
        tvDescription = findViewById(R.id.tv_desc);
        tvWindSpeed = findViewById(R.id.tv_wind_speed);
        btnAnyWeather = findViewById(R.id.btn_find_weather);

        btnAnyWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainActivityViewModel.fetchCurrentWeatherDataFromCityName(etCityName.getText().toString().trim(), etCountryId.getText().toString().trim());
            }
        });
        btnDeviceLocationWeather = findViewById(R.id.btn_find_device_location_weather);
        btnDeviceLocationWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ApiDataRepository.longitude != null && ApiDataRepository.latitude != null) {
                    findDeviceLocation();
                    mainActivityViewModel.fetchCurrentWeatherData(ApiDataRepository.longitude, ApiDataRepository.latitude);
                } else
                    Toast.makeText(getApplicationContext(), "Error Finding Current Location Weather.", Toast.LENGTH_LONG).show();
            }
        });
        initRecyclerView();
    }

    void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new ForecastsDataAdapter();
        recyclerView.setAdapter(adapter);

//        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
//                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                noteViewModel.delete(adapter.getNoteAt(viewHolder.getAdapterPosition()));
//                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
//            }
//        }).attachToRecyclerView(recyclerView);

    }

    private void getLocation() {

        //Check Permissions again

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,

                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location LocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps != null) {
                double lat = LocationGps.getLatitude();
                double longi = LocationGps.getLongitude();

                ApiDataRepository.latitude = String.valueOf(lat);
                ApiDataRepository.longitude = String.valueOf(longi);

                // showLocationTxt.setText("Your Location:"+"\n"+"Latitude= "+ApiDataRepository.latitude+"\n"+"Longitude= "+ApiDataRepository.longitude);
            } else if (LocationNetwork != null) {
                double lat = LocationNetwork.getLatitude();
                double longi = LocationNetwork.getLongitude();

                ApiDataRepository.latitude = String.valueOf(lat);
                ApiDataRepository.longitude = String.valueOf(longi);

                //showLocationTxt.setText("Your Location:"+"\n"+"Latitude= "+ApiDataRepository.latitude+"\n"+"Longitude= "+ApiDataRepository.longitude);
            } else if (LocationPassive != null) {
                double lat = LocationPassive.getLatitude();
                double longi = LocationPassive.getLongitude();

                ApiDataRepository.latitude = String.valueOf(lat);
                ApiDataRepository.longitude = String.valueOf(longi);

                //showLocationTxt.setText("Your Location:"+"\n"+"Latitude= "+ApiDataRepository.latitude+"\n"+"Longitude= "+ApiDataRepository.longitude);
            } else {
                Toast.makeText(this, "Can't Get Your Location", Toast.LENGTH_SHORT).show();
            }

            //Thats All Run Your App
        }

    }

    private void OnGPS() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
