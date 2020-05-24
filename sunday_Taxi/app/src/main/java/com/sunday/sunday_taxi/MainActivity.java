package com.sunday.sunday_taxi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.util.StringUtils;
import com.sunday.sunday_taxi.models.AddressModel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.CameraUpdate;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_LOCATION = 10001;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude = 37.5571992;
    private double longitude = 126.970536;
    private MyLocationListener myLocationListener;
    private RelativeLayout mapViewContainer;
    private MyMapViewEventListener myMapViewEventListener;
    private MapReverseGeoCoder mapReverseGeoCoder;
    private MyMapReverseGeoCoder myMapReverseGeoCoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 안드로이드에서 권한 확인이 의무화 되어서 작성된 코드! 개념만 이해
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                return;
            }
        }

        Location loc = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (loc != null) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        }

        MapView mapView = new MapView(this);
        mapViewContainer = findViewById(R.id.map_view);
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);
        mapView.setMapCenterPoint(mapPoint, true);
        mapViewContainer.addView(mapView);

        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Current Location");
        marker.setTag(0);
        marker.setMapPoint(mapPoint);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        mapView.addPOIItem(marker);
        this.myLocationListener = new MyLocationListener(mapView);

        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.01f, myLocationListener);

        this.myMapReverseGeoCoder = new MyMapReverseGeoCoder();
        mapReverseGeoCoder = new MapReverseGeoCoder("3782bd3774b50c20516c5165f5539af3", mapPoint, myMapReverseGeoCoder, this);
        mapReverseGeoCoder.startFindingAddress();

        this.myMapViewEventListener = new MyMapViewEventListener(myMapReverseGeoCoder, this);
        mapView.setMapViewEventListener(myMapViewEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 승인이 된 경우 다시 그리기
                    recreate();
                } else {
                    // 권한 승인이 안 된 경우 종료
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(myLocationListener);
    }

    private class MyLocationListener implements LocationListener {
        private MapView mapView;

        public MyLocationListener(MapView parentMapView) {
            this.mapView = parentMapView;
        }

        @Override
        public void onLocationChanged(Location location) {
            // 위치가 변경되었을 때마다 현재 위치를 갱신하고 새로운 Marker 생성
            // 현재 위치 갱신시 새로운 Marker를 생성하기 전에 mapView에 존재하는 기존 marker 제거하기
            mapView.removeAllPOIItems();

            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(location.getLatitude(), location.getLongitude());
            MapPOIItem marker = new MapPOIItem();
            marker.setItemName("Current Location");
            marker.setTag(0);
            marker.setMapPoint(mapPoint);
            marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

            mapView.addPOIItem(marker);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    private class MyMapViewEventListener implements MapView.MapViewEventListener {
        private MyMapReverseGeoCoder myMapReverseGeoCoder;
        private MapReverseGeoCoder mapReverseGeoCoder;
        private Activity activity;

        public MyMapViewEventListener(MyMapReverseGeoCoder myMapReverseGeoCoder, Activity activity) {
            this.myMapReverseGeoCoder = myMapReverseGeoCoder;
            this.activity = activity;
        }

        @Override
        public void onMapViewInitialized(MapView mapView) {

        }

        @Override
        public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
            mapReverseGeoCoder = new MapReverseGeoCoder("3782bd3774b50c20516c5165f5539af3", mapPoint, myMapReverseGeoCoder, activity);
            mapReverseGeoCoder.startFindingAddress();
            mapView.removeAllPOIItems();
            MapPOIItem marker = new MapPOIItem();
            marker.setItemName("Current Location");
            marker.setTag(0);
            marker.setMapPoint(mapPoint);
            marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

            mapView.addPOIItem(marker);
        }

        @Override
        public void onMapViewZoomLevelChanged(MapView mapView, int i) {

        }

        @Override
        public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

        }

        @Override
        public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
            Thread request = new Thread(new AddressRequester(mapPoint.getMapPointGeoCoord().latitude, mapPoint.getMapPointGeoCoord().longitude, makeHandler()));
            request.start();
        }

        private Handler makeHandler() {
            return new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    AddressModel addressModel = (AddressModel) msg.obj;
                    StringBuilder stringBuilder = new StringBuilder();

                    TextView addressName = findViewById(R.id.address);

                    if (addressModel.documents.get(0).roadAddress.buildingName != null) {
                        stringBuilder.append(addressModel.documents.get(0).roadAddress.addressName);
                        stringBuilder.append("\n");
                        stringBuilder.append(addressModel.documents.get(0).roadAddress.buildingName);
                        addressName.setText(stringBuilder.toString());
                    } else if (addressModel.documents.get(0).roadAddress.buildingName == null && addressModel.documents.get(0).roadAddress.addressName != null) {
                        stringBuilder.append(addressModel.documents.get(0).roadAddress.addressName);
                        addressName.setText(stringBuilder.toString());
                    } else if (addressModel.documents.get(0).roadAddress.addressName == null) {
                        stringBuilder.append(addressModel.documents.get(0).address.addressName);
                        addressName.setText(stringBuilder.toString());
                    }
                }
            };
        }
    }

    private class MyMapReverseGeoCoder implements MapReverseGeoCoder.ReverseGeoCodingResultListener {
        @Override
        public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
            //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {

        }
    }
}
