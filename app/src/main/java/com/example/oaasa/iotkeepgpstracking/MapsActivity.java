package com.example.oaasa.iotkeepgpstracking;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ProgressDialog progressDialog;
    /*private static final String latitude="latitude";
    private static final String longitude="longitude";*/
    ArrayList<HashMap<String,Double>>Coordinate;
    HashMap<String,Double>resultcoordinate;
    List<Address> addresses;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Coordinate=new ArrayList<>();
        resultcoordinate=new HashMap<>();
        new BackgroundTask().execute();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        new BackgroundTask().execute();
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
    }

    public class BackgroundTask extends AsyncTask<Void,Void,String> {
        String Gps_Url = "http://iotkeep.com/IOTKEEP_API/json?api_key=SXRMYIQCUU";


        @Override
        protected void onPreExecute() {
           /* progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setMessage("Map Still Loading....");
            progressDialog.setTitle("Please Wait....");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();*/
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(Gps_Url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                String String_json = stringBuilder.toString().trim();
                Log.d("Json String", String_json);
                int count = 0;
                JSONObject jsonObject = new JSONObject(String_json);
                JSONArray jsonArray = jsonObject.getJSONArray("feeds");
                while (count < jsonArray.length()) {
                    JSONObject JO = jsonArray.getJSONObject(count);
                    count++;
                    Double Latitude = JO.getDouble("latitude");
                    Double Longitude = JO.getDouble("longitude");
                    Double Temparature=JO.getDouble("temp");
                    Double Humidity=JO.getDouble("humidity");
                    Log.d("Latitude", String.valueOf(Latitude));
                    Log.d("Longitude", String.valueOf(Longitude));
                    resultcoordinate = new HashMap<>();
                    resultcoordinate.put("latitude", Latitude);
                    resultcoordinate.put("longitude", Longitude);
                    resultcoordinate.put("Temparature",Temparature);
                    resultcoordinate.put("Humidity",Humidity);
                    Coordinate.add(resultcoordinate);


                }

            } catch (MalformedURLException e) {

            } catch (IOException e) {

            } catch (JSONException e) {

            }

            return "Map is Loaded Successfully";
        }

        @Override
        protected void onProgressUpdate(Void... values) {


        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("Map is Loaded Successfully")){
                Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
            }

            for (int i=0;i<Coordinate.size();i++){
                resultcoordinate=Coordinate.get(i);
                Double Lat=resultcoordinate.get("latitude");
                Double Lng=resultcoordinate.get("longitude");
                final Double temp=resultcoordinate.get("Temparature");
                final Double humidity=resultcoordinate.get("Humidity");
                // Add a marker in Sydney and move the camera
                LatLng oaasa = new LatLng(Lat, Lng);

                mMap.addMarker(new MarkerOptions().position(oaasa).title("position"));
                CameraPosition cameraPosition=new CameraPosition.Builder()
                        .target(oaasa).zoom(20).bearing(90).tilt(30).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        try {
                            geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
                            addresses=geocoder.getFromLocation(marker.getPosition().latitude,marker.getPosition().longitude,1);
                            Double latitude=addresses.get(0).getLatitude();
                            Double longitude=addresses.get(0).getLongitude();
                            String address=addresses.get(0).getAddressLine(0);
                            String city=addresses.get(0).getLocality();
                            String state=addresses.get(0).getAdminArea();
                            String postalcode=addresses.get(0).getPostalCode();
                            mMap.addPolyline(new PolylineOptions().add(new LatLng(latitude,longitude)).width(5).color(Color.BLUE).geodesic(true));

                            String line="Address :"+address+"\n"+"City :"+city+"\n"+"State :"+state+"\n"+"Postal Code :"
                                    +postalcode+"\n"+"Latitude :"+latitude+"\n"+"Longitude :"+longitude+"\n"+"Tempareture :"+temp+"\n"+"Humidity :"+humidity;

                            Toast.makeText(getApplicationContext(),line,Toast.LENGTH_LONG).show();
                        }catch (IOException e){
                            e.printStackTrace();
                        }

                        return true;
                    }
                });
                }

            }

        }
    }
