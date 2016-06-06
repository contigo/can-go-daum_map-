
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import net.daum.mf.map.api.CameraPosition;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.CancelableCallback;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapReverseGeoCoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import com.example.songmyungjin.daum_map.search.Item;

import net.daum.mf.map.api.MapPoint.GeoCoordinate;

import com.example.songmyungjin.daum_map.search.OnFinishSearchListener;
import com.example.songmyungjin.daum_map.search.Searcher;
import java.util.List;

import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener, MapView.MapViewEventListener,MapView.POIItemEventListener,TextToSpeech.OnInitListener, CompoundButton.OnCheckedChangeListener {

    private static final String LOG_TAG = "LocationDemoActivity";
  private static final int RESULT_SPEECH = 1;
    private Intent i;
    private  TextView tv;
    private TextView GeoExplain;
    private ImageButton bt;
    private Button NowLocation;
    private Button hidy;

    private double mylatitude;
    private double mylongitude;



    private HashMap<Integer, Item> mTagItemMap = new HashMap<Integer, Item>();

    private  MapView mapView;

    boolean isPageOpen = false;

    Animation translateUP;
    Animation tranalateDown;

    LinearLayout slidepage;
    private Button GeoButton;
    private MapReverseGeoCoder mReverseGeoCoder = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonSearch = (Button)findViewById(R.id.buttonSearch);
        Button search_real =(Button)findViewById(R.id.search);


        tv= (TextView)findViewById(R.id.editTextQuery);

        mapView = new MapView(this);
        mapView.setDaumMapApiKey("14edc60c0e7b279c16e4d0c7950a8c22");
        mapView.setCurrentLocationEventListener(this);


        hidy = (Button)findViewById(R.id.hide);
        hidy.setOnClickListener(new View.OnClickListener(){
            private boolean state = false;
            @Override
            public void onClick(View view) {
                if(state){
                    state = false;
                    //showResult();
                }else{
                    state = true;
                    hideResult();
                }
            }
        });
        GeoExplain=(TextView)findViewById(R.id.GeoExplain);


        GeoButton = (Button)findViewById(R.id.nowlocation);
        GeoButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                mReverseGeoCoder = new MapReverseGeoCoder("14edc60c0e7b279c16e4d0c7950a8c22", mapView.getMapCenterPoint(), MainActivity.this, MainActivity.this);
                mReverseGeoCoder.startFindingAddress();
            }
        });

        //boolean isMove;

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        NowLocation = (Button)findViewById(R.id.location);
        NowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "현 위치 검색 중입니다.", Toast.LENGTH_SHORT).show();
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                mapView.setShowCurrentLocationMarker(false);
                MapPoint mylocation = MapPoint.mapPointWithGeoCoord(mylatitude, mylongitude);
                CameraPosition myCamera = new CameraPosition(mylocation,2);
                mapView.animateCamera(CameraUpdateFactory.newCameraPosition(myCamera), 1000, new CancelableCallback() {
                    @Override
                    public void onFinish() {
                        Toast.makeText(getBaseContext(), "현재위치를 찾습니다.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getBaseContext(), "현재위치를 찾을수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.buttonSearch){
                    i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
                    i.putExtra(RecognizerIntent.EXTRA_PROMPT,"말해주세요");
                    Toast.makeText(MainActivity.this,"start speak",Toast.LENGTH_SHORT).show();
                    try{
                        startActivityForResult(i,RESULT_SPEECH);
                    }catch (ActivityNotFoundException e){
                        Toast.makeText(getApplicationContext(),"Speech To Text를 지원하지 않습니다.",Toast.LENGTH_SHORT).show();
                        e.getStackTrace();
                    }
                }
            }
        });


        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633), true);
// 줌 레벨 변경
        mapView.setZoomLevel(7, true);
// 중심점 변경 + 줌 레벨 변경
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(33.41, 126.52), 9, true);
// 줌 인
        mapView.zoomIn(true);
// 줌 아웃
        mapView.zoomOut(true);

        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("명진이 장소!!");
        marker.setTag(0);
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633));
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);


        // 상하 방향 뷰 슬라이드
        slidepage = (LinearLayout)findViewById(R.id.slideOpenPage);

        translateUP = AnimationUtils.loadAnimation(this,R.anim.translate_up);
        tranalateDown = AnimationUtils.loadAnimation(this,R.anim.translate_down);

        SlidingPageAnimationListener animListener = new SlidingPageAnimationListener();
        translateUP.setAnimationListener(animListener);
        tranalateDown.setAnimationListener(animListener);

        search_real.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = tv.getText().toString();
                if(query == null || query.length() == 0){
                    showToast("검색어를 입력하세요");
                    return;
                }
                hideSoftKeyboard();





                GeoCoordinate geoCoordinate = mapView.getMapCenterPoint().getMapPointGeoCoord();
                double latitude = geoCoordinate.latitude;
                double longitude = geoCoordinate.longitude;


                int radius = 10000; // 반경거리
                int page = 1;//한페이지 당 15개

                for(int a = page; a<3;a++) {
                    Searcher searcher = new Searcher();
                    searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, a, "14edc60c0e7b279c16e4d0c7950a8c22", new OnFinishSearchListener() {
                        @Override
                        public void onSuccess(List<Item> itemList) {
                            //mapView.removeAllPOIItems();//기존 검색 결과 삭제
                            showResult(itemList);


                        }

                        @Override
                        public void onFail() {
                            showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
                        }
                    });
                }
                mapView.removeAllPOIItems();//기존 검색 결과 삭제

                }





        });



    }

    private void showResult(List<Item> itemList) {
        MapPointBounds mapPointBounds = new MapPointBounds();

        for (int i = 0; i < itemList.size(); i++) {
            Item item = itemList.get(i);

            MapPOIItem poiItem = new MapPOIItem();
            poiItem.setItemName(item.title);
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude);
            poiItem.setMapPoint(mapPoint);
            mapPointBounds.add(mapPoint);
            poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomImageResourceId(R.drawable.map_pin_blue);
            poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomSelectedImageResourceId(R.drawable.map_pin_red);
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);

            mapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);
        }

        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds));

        MapPOIItem[] poiItems = mapView.getPOIItems();
        if (poiItems.length > 0) {
            mapView.selectPOIItem(poiItems[0], false);
        }
    }


    public void onButtonClicked(View v){
        if(isPageOpen){
            slidepage.startAnimation(translateUP);
            slidepage.setVisibility(View.INVISIBLE);
            isPageOpen = false;
        }else{
            slidepage.startAnimation(tranalateDown);
            slidepage.setVisibility(View.VISIBLE);
            isPageOpen = true;
        }
    }

    public boolean onTouchEvent(MotionEvent event, MapView mapView)

    {

        if (event.getAction() == event.ACTION_UP) {

            // 터치된 화면의 좌표를 지리좌표로 변환한다



            return true;

        }

        else return false;

    }


    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {

        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            if (poiItem == null) return null;
            Item item = mTagItemMap.get(poiItem.getTag());
            if (item == null) return null;
            ImageView imageViewBadge = (ImageView) mCalloutBalloon.findViewById(R.id.badge);
            TextView textViewTitle = (TextView) mCalloutBalloon.findViewById(R.id.title);
            textViewTitle.setText(item.title);
            TextView textViewDesc = (TextView) mCalloutBalloon.findViewById(R.id.desc);
            textViewDesc.setText(item.address);
            imageViewBadge.setImageDrawable(createDrawableFromUrl(item.imageUrl));
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

    }
    private Drawable createDrawableFromUrl(String url) {
        try {
            InputStream is = (InputStream) this.fetch(url);
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object fetch(String address) throws MalformedURLException,IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }


    private class SlidingPageAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
        /*
            if(isPageOpen){
                slidepage.setVisibility(View.INVISIBLE);
                slideBtn.setText("Open");
                isPageOpen = false;
            }else{
                slideBtn.setText("Close");
                isPageOpen = true;
            }
        */
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }


    void hideResult(){
        findViewById(R.id.editTextQuery).setVisibility(View.GONE);
        findViewById(R.id.buttonSearch).setVisibility(View.GONE);
        findViewById(R.id.location).setVisibility(View.GONE);
        findViewById(R.id.status).setVisibility(View.GONE);
        findViewById(R.id.status2).setVisibility(View.GONE);
        findViewById(R.id.status3).setVisibility(View.GONE);
    }
    /*
    void showResult(List<Item> itemList){
        findViewById(R.id.editTextQuery).setVisibility(View.VISIBLE);
        findViewById(R.id.buttonSearch).setVisibility(View.VISIBLE);
        findViewById(R.id.location).setVisibility(View.VISIBLE);
        findViewById(R.id.status).setVisibility(View.VISIBLE);
        findViewById(R.id.status2).setVisibility(View.VISIBLE);
        findViewById(R.id.status3).setVisibility(View.VISIBLE);
    }
*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mapView.setShowCurrentLocationMarker(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && (requestCode == RESULT_SPEECH )){
            ArrayList<String> Result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String Result_set = Result.get(0);

            tv.setText(""+Result_set);
            //Toast.makeText(MainActivity.this,Result_set,Toast.LENGTH_SHORT).show();
        }
    }


    @Override // 처음 맵을 그릴때 그리는 부분
    public void onMapViewInitialized(MapView mapView) {


        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.537229,127.005515), 2, true);

        Searcher searcher = new Searcher();
        String query = tv.getText().toString();
        double latitude = 37.537229;
        double longitude = 127.005515;
        int radius = 10000; // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 사용. meter 단위 (0 ~ 10000)
        int page = 1;
        String apikey = "14edc60c0e7b279c16e4d0c7950a8c22";

        searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, page, apikey, new OnFinishSearchListener() {
            @Override
            public void onSuccess(final List<Item> itemList) {
                showResult(itemList);
            }

            @Override
            public void onFail() {
                showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
            }
        });

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

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

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    @Override
    public void onInit(int i) {

    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
        //현위치를 받음
        Toast.makeText(MainActivity.this,mapPointGeo.latitude+"+"+mapPointGeo.longitude,Toast.LENGTH_LONG).show();
        mylatitude = mapPointGeo.latitude;
        mylongitude = mapPointGeo.longitude;
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mapReverseGeoCoder.toString();
        onFinishReverseGeoCoding(s);
    }

    private void onFinishReverseGeoCoding(String s) {
      //  Toast.makeText(MainActivity.this, "Reverse Geo-coding : " + s, Toast.LENGTH_SHORT).show();
        GeoExplain.setText(""+s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        onFinishReverseGeoCoding("Fail");
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }


}
