package com.lonelydev.myweatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 100;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Button fetchButton = findViewById(R.id.button);
        TextView textView = findViewById(R.id.TextView);
        TextView city = findViewById(R.id.city);
        TextView temp = findViewById(R.id.temperature);
        TextView condition = findViewById(R.id.condition);
        ImageView imageView = findViewById(R.id.imageView);
        ImageView bgimg = findViewById(R.id.backgroundImageView);
        city.setText("");
        temp.setText("");
        condition.setText("");

        fetchButton.setOnClickListener(v -> {
            textView.setText("Fetching data...");
            getLocationAndFetchWeather(textView, city, temp, condition, imageView).thenAccept(conditionString -> {
                bgSet(bgimg, conditionString);
            });
        });

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }
    }

    private CompletableFuture<String> getLocationAndFetchWeather(TextView textView, TextView city, TextView temp, TextView condition, ImageView imageView) {
        CompletableFuture<String> future = new CompletableFuture<>();
        getLocation().thenAccept(locationString -> {
            if (locationString != null) {
                Log.d(TAG, "Location: " + locationString);

                QueryAPI apiService = RetrofitClient.getClient().create(QueryAPI.class);
                String API_KEY = "8102d2aa273c482997d124051240707";
                Call<DataModel> call = apiService.getData(API_KEY, locationString, "no");

                call.enqueue(new Callback<DataModel>() {
                    @Override
                    public void onResponse(Call<DataModel> call, Response<DataModel> response) {
                        if (response.isSuccessful()) {
                            DataModel weatherResponse = response.body();
                            if (weatherResponse != null) {
                                Log.d(TAG, "Location: " + weatherResponse.getLocation().getName());
                                Log.d(TAG, "Temperature: " + weatherResponse.getCurrent().getTempC() + "°C");
                                Log.d(TAG, "Condition: " + weatherResponse.getCurrent().getCondition().getText());

                                runOnUiThread(() -> {
                                    city.setText(weatherResponse.getLocation().getName());
                                    temp.setText(weatherResponse.getCurrent().getTempC() + "°");
                                    condition.setText(weatherResponse.getCurrent().getCondition().getText());
                                    textView.setText("");

                                    Glide.with(MainActivity.this)
                                            .load("https:" + weatherResponse.getCurrent().getCondition().getIcon())
                                            .into(imageView);
                                });
                            }
                            future.complete(weatherResponse.getCurrent().getCondition().getText());
                        } else {
                            Log.d(TAG, "Request failed. Error: " + response.message());
                            future.complete("sunny");
                        }
                    }

                    @Override
                    public void onFailure(Call<DataModel> call, Throwable t) {
                        Log.e(TAG, "Failed to fetch data", t);
                        runOnUiThread(() -> textView.setText("Failed to fetch data"));
                        future.completeExceptionally(t);
                    }
                });
            } else {
                Log.e(TAG, "Failed to get location: locationString is null");
                future.complete("sunny");
            }
        }).exceptionally(throwable -> {
            Log.e(TAG, "Failed to get location: " + throwable.getMessage());
            future.complete("sunny");
            return null;
        });
        return future;
    }

    private CompletableFuture<String> getLocation() {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            if (location != null) {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                future.complete(latLng.latitude + "," + latLng.longitude);
                            } else {
                                Log.d(TAG, "Location not available");
                                future.complete("10.960078,78.076607");
                            }
                        } else {
                            future.completeExceptionally(task.getException());
                        }
                    });
        } else {
            future.complete("10.960078,78.076607");
        }

        return future;
    }


    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        Log.d(TAG, "Location permission not granted, requesting...");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchWeather(findViewById(R.id.TextView), findViewById(R.id.imageView), findViewById(R.id.city), findViewById(R.id.temperature), findViewById(R.id.condition));
            } else {
                Log.d(TAG, "Location permission denied");
            }
        }
    }

    public void bgSet(ImageView imageView, String conditionString) {
        PexelsService service = PexelsClient.getClient().create(PexelsService.class);
        Call<PexelsResponse> call = service.getRandomPhotos(conditionString+" weather", 16);
        call.enqueue(new Callback<PexelsResponse>() {
            @Override
            public void onResponse(Call<PexelsResponse> call, Response<PexelsResponse> response) {
                int min = 1;
                int max = 15;
                Random random = new Random();
                int randomInt = random.nextInt((max - min) + 1) + min;
                if (response.isSuccessful() && response.body() != null) {
                    PexelsResponse pexelsResponse = response.body();
                    if (pexelsResponse.getPhotos() != null && !pexelsResponse.getPhotos().isEmpty()) {
                        String imageUrl = pexelsResponse.getPhotos().get(randomInt).getSrc().getMedium(); // Select the first photo
                        Glide.with(MainActivity.this).load(imageUrl).into(imageView);
                    } else {
                        Toast.makeText(MainActivity.this, "No photos found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PexelsResponse> call, Throwable t) {
                Log.e("MainActivity", "Error fetching data", t);
                Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
