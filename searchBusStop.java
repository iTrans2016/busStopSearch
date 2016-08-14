package com.example.ben.itrans;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by helen_000 on 6/27/2016.
 */
public class searchBusStop extends Activity  implements OnMapReadyCallback {

    GoogleMap nMap;
    LatLng primary = new LatLng(1.329007, 103.802621);
    LatLng secondara = new LatLng(1.29664825487647, 103.85253591654006);
    LatLng secondare = new LatLng(1.29684825487647, 103.85253591654007);
    int count;
    private List<Double> singleCoordinates = new ArrayList<>();
    private List<List<Double>> busCoordinates = new ArrayList<>();
    int counting;
    RequestQueue requestQueue;
    private List<Integer> finished = new ArrayList<>();

    boolean dontCall;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_busstop);
        MapFragment nmapfragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.busStopMap));
        nmapfragment.getMapAsync(this);
        requestQueue = VolleySingleton.getInstance().getRequestQueue();
        counting = 0;
    }

    public void onMapReady(GoogleMap map) {
        nMap = map;
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(primary,15));
        nMap.addMarker(new MarkerOptions()
                .position(primary)
                .title("01012")
                .draggable(true)
                .snippet("")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_24dp)));

        nMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                counting++;
                LatLng position = marker.getPosition();
                singleCoordinates = new ArrayList<Double>();
                singleCoordinates.add(position.latitude);
                singleCoordinates.add(position.longitude);
                busCoordinates.add(singleCoordinates);
                System.out.println(String.valueOf(singleCoordinates));
                if(counting==3) {
                    nMap.clear();
                    counting = 0;
                    call();
                }
            }
        });

        nMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent sendBusStop = new Intent(searchBusStop.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("busStopNo", marker.getTitle());
                b.putParcelable("busStopPt", marker.getPosition());
                sendBusStop.putExtras(b);
                startActivity(sendBusStop);
            }
        });
        count = -50;
    }

    public void call(){
        dontCall = false;
        count += 50;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "http://datamall2.mytransport.sg/ltaodataservice/BusStops?$skip="+String.valueOf(count), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("value");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                counting = 0;
                                JSONObject services = jsonArray.getJSONObject(i);
                                if(finished.size()==busCoordinates.size()){
                                    dontCall = true;
                                }else{
                                    Double Latitude = services.getDouble("Latitude");
                                    Double Longitude = services.getDouble("Longitude");
                                    for(List<Double> coordinates:busCoordinates) {
                                        if(!finished.contains(counting)) {
                                            if (Math.sqrt(Math.pow(coordinates.get(0) - Latitude, 2) + Math.pow(coordinates.get(1) - Longitude, 2)) <= 0.0002) {
                                                System.out.println(String.valueOf(Math.sqrt(Math.pow(coordinates.get(0) - Latitude, 2) - Math.pow(coordinates.get(1) - Longitude, 2))));
                                                String busCode = services.getString("BusStopCode");
                                                LatLng busPosition = new LatLng(Latitude, Longitude);
                                                Toast.makeText(getApplicationContext(), String.valueOf(busPosition), Toast.LENGTH_LONG).show();
                                                nMap.addMarker(new MarkerOptions()
                                                        .position(busPosition)
                                                        .title(String.valueOf(busCode))
                                                        .snippet("")
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_24dp)));
                                                finished.add(counting);
                                            }
                                        }
                                    counting++;
                                    }
                                }

                            }
                            if(!dontCall) {
                                call();
                            }
                                /*
                                    end = true;
                                }else if(end){
                                    callAgain = true;
                                }*/
                            //JSONObject services = jsonArray.getJSONObject(i);
                            //String busNo = services.getString("ServiceNo");
                            // JSONObject nextBus = services.getJSONObject("NextBus");
                            //String eta = nextBus.getString("EstimatedArrival");
                            // String wheelC = nextBus.getString("Feature");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "ERROR");
                        Toast.makeText(getApplicationContext(), "That did not work:(", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("AccountKey", "3SnRYzr/X0eKp2HvwTYtmg==");
                headers.put("UniqueUserID", "0bf7760d-15ec-4a1b-9c82-93562fcc9798");
                headers.put("accept", "application/json");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
}
