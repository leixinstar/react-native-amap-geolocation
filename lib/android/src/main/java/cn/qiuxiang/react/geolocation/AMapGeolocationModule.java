package cn.qiuxiang.react.geolocation;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;

public class AMapGeolocationModule extends ReactContextBaseJavaModule implements AMapLocationListener {
    private final ReactApplicationContext reactContext;
    private DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;
    private static AMapLocationClient locationClient;

    AMapGeolocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "AMapGeolocation";
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location != null) {
            if (location.getErrorCode() == 0) {
                eventEmitter.emit("AMapGeolocation", toReadableMap(location));
            }
            // TODO: 返回定位错误信息
        }
    }

    @ReactMethod
    public void init(String key, Promise promise) {
        if (locationClient != null) {
            locationClient.onDestroy();
        }

        AMapLocationClient.setApiKey(key);
        locationClient = new AMapLocationClient(reactContext);
        locationClient.setLocationListener(this);
        eventEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        promise.resolve(null);
    }

    @ReactMethod
    public void setOptions(ReadableMap options) {
        AMapLocationClientOption option = new AMapLocationClientOption();
        if (options.hasKey("interval")) {
            option.setInterval(options.getInt("interval"));
        }
        if (options.hasKey("reGeocode")) {
            option.setNeedAddress(options.getBoolean("reGeocode"));
        }
        if(options.hasKey("locationMode")) {
            option.setLocationMode(AMapLocationMode.valueOf(options.getString("locationMode")));//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        }
        if(options.hasKey("gpsFirst")) {
            option.setGpsFirst(options.getBoolean("gpsFirst"));//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        }
        if(options.hasKey("httpTimeout")) {
            option.setHttpTimeOut(options.getInt("httpTimeout"));//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        }
        if(options.hasKey("interval")) {
            option.setInterval(options.getInt("interval"));//可选，设置连续定位间隔。
        }
        if(options.hasKey("needAddress")) {
            option.setNeedAddress(options.getBoolean("needAddress"));//可选，设置是否返回逆地理地址信息。默认是true
        }
        if(options.hasKey("onceLocation")) {
            option.setOnceLocation(options.getBoolean("onceLocation"));//可选，设置是否单次定位。默认是false
        }
        if(options.hasKey("locationCacheEnable")) {
            option.setLocationCacheEnable(options.getBoolean("locationCacheEnable"));//可选，设置是否开启缓存，默认为true.
        }
        if(options.hasKey("onceLocationLatest")) {
            option.setOnceLocationLatest(options.getBoolean("onceLocationLatest"));//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        }
        if(options.hasKey("locationProtocol")) {
            option.setLocationProtocol(AMapLocationProtocol.valueOf(options.getString("locationProtocol")));//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        }
        if(options.hasKey("sensorEnable")) {
            option.setSensorEnable(options.getBoolean("sensorEnable"));//可选，设置是否使用传感器。默认是false
        }
        locationClient.setLocationOption(option);
    }

    @ReactMethod
    public void start() {
        locationClient.startLocation();
    }

    @ReactMethod
    public void stop() {
        locationClient.stopLocation();
    }

    @ReactMethod
    public void getOnceLocation() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setOnceLocation(true);
        option.setNeedAddress(true);    //可选，设置是否返回逆地理地址信息。默认是true
        // 设置定位参数
        locationClient.setLocationOption(option);
        // 启动定位
        locationClient.startLocation();
    }

    @ReactMethod
    public void getLastLocation(Promise promise) {
        promise.resolve(toReadableMap(locationClient.getLastKnownLocation()));
    }

    private ReadableMap toReadableMap(AMapLocation location) {
        WritableMap map = Arguments.createMap();
        map.putDouble("timestamp", location.getTime());
        map.putDouble("accuracy", location.getAccuracy());
        map.putDouble("latitude", location.getLatitude());
        map.putDouble("longitude", location.getLongitude());
        map.putDouble("altitude", location.getAltitude());
        map.putDouble("speed", location.getSpeed());
        if (!location.getAddress().isEmpty()) {
            map.putString("address", location.getAddress());
            map.putString("description", location.getDescription());
            map.putString("poiName", location.getPoiName());
            map.putString("country", location.getCountry());
            map.putString("province", location.getProvince());
            map.putString("city", location.getCity());
            map.putString("cityCode", location.getCityCode());
            map.putString("district", location.getDistrict());
            map.putString("street", location.getStreet());
            map.putString("streetNumber", location.getStreetNum());
            map.putString("adCode", location.getAdCode());
        }
        return map;
    }
}
