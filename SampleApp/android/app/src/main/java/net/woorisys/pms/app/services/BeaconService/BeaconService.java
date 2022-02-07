package net.woorisys.pms.app.services.BeaconService;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.RequiresApi;

import net.woorisys.pms.app.dataManager.SaveArrayListValue;
import net.woorisys.pms.app.dataManager.UserDataSingleton;
import net.woorisys.pms.app.services.BeaconService.Function.BeaconFunction;
import net.woorisys.pms.app.services.BeaconService.Function.Item;
import net.woorisys.pms.app.services.NotificationService.NotificationService;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.scanner.ScanFilterUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import static net.woorisys.pms.app.services.NotificationService.NotificationService.FOREGROUND_NOTIFICATION_ID;

public class BeaconService extends Service implements BeaconConsumer {
    private final static String TAG = "KTW_BeaconService";

    private final static String BEACON_PARSER = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";

    List<ScanFilter> mFilters;
    BluetoothLeScanner LeScanner_W;
    BeaconFunction mBeaconFunction;
    BeaconManager BeaconManager_W;
    BluetoothAdapter BluetoothAdapter_W;
    SaveArrayListValue mSaveArrayListValue;
    private boolean isStartScanning = false;
    private ScanSettings mScanSettings;
    private Vector<Item> items;
    private DecimalFormat mDecimalFormat = new DecimalFormat("#.##");

    private BeaconServiceUsage mBeaconServiceUsage = BeaconServiceUsage.PARKING;

    public BeaconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mBeaconFunction = new BeaconFunction(getApplicationContext());
        mSaveArrayListValue = new SaveArrayListValue();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            BeaconParser beaconParser = new BeaconParser();
            beaconParser.setBeaconLayout(BEACON_PARSER);
            List<BeaconParser> beaconParsers = new ArrayList<>();
            beaconParsers.add(beaconParser);
            // RequiresApi 가 필요 - Oreo 버전에서만 사용할 예정이기 때문에 Oreo 만 잡아준다
            mScanSettings = (new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)).build();
            ScanFilterUtils scanFilterUtils = new ScanFilterUtils();
            mFilters = scanFilterUtils.createScanFiltersForBeaconParsers(beaconParsers);
            BluetoothAdapter_W = BluetoothAdapter.getDefaultAdapter();
            LeScanner_W = BluetoothAdapter_W.getBluetoothLeScanner();

            StartBluetoothScanning();
        } else {
            BeaconManager_W = BeaconManager.getInstanceForApplication(this);
            BluetoothAdapter_W = BluetoothAdapter.getDefaultAdapter();
            BeaconManager_W.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_PARSER));

            if (!BeaconManager_W.isBound(this))
                BeaconManager_W.bind(this);
        }

        Log.i(TAG, "onCreate: Created Beacon Service....");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Destroyed Beacon Service ...");
        StopBluetoothScanning();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Beacon Service 사용처
        if (intent != null && intent.hasExtra("purpose")) {
            switch (Objects.requireNonNull(intent.getStringExtra("purpose"))) {
                case "parking":
                    this.mBeaconServiceUsage = BeaconServiceUsage.PARKING;
                    break;
                case "both":
                    this.mBeaconServiceUsage = BeaconServiceUsage.BOTH;
                    break;
                case "onepass":
                default:
                    this.mBeaconServiceUsage = BeaconServiceUsage.ONEPASS;
            }
        } else {
            UserDataSingleton.getInstance().loadUserData(getApplicationContext());
            switch (Objects.requireNonNull(UserDataSingleton.getInstance().getPurpose())) {
                case "parking":
                    this.mBeaconServiceUsage = BeaconServiceUsage.PARKING;
                    break;
                case "both":
                    this.mBeaconServiceUsage = BeaconServiceUsage.BOTH;
                    break;
                case "onepass":
                default:
                    this.mBeaconServiceUsage = BeaconServiceUsage.ONEPASS;
            }
        }
        mBeaconFunction.setBeaconServiceUsage(this.mBeaconServiceUsage);

        if (NotificationService.getInstance() == null) {
            new NotificationService(getApplicationContext());
        }
        Notification notification = NotificationService.getInstance().createForegroundNotification(this, UserDataSingleton.getInstance().getPurpose());
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);

        Log.i(TAG, "onStartCommand: Start Beacon Service....");
//      return START_NOT_STICKY;
        return START_STICKY;
    }

    // 안드로이드 버전이 마시멜로우 이하이면 실행
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBeaconServiceConnect() {
        BeaconManager_W.addRangeNotifier((beacons, region) -> {
            Log.d(TAG, "onBeaconServiceConnect - M");

            if (beacons.size() > 0) {
                Iterator<Beacon> iterator = beacons.iterator();
                String mainUUID = UserDataSingleton.getInstance().getLobbyBeaconUUID();
                String parkingUUID = UserDataSingleton.getInstance().getParkingBeaconUUID();

                items = new Vector<>();

                while (iterator.hasNext()) {
                    Beacon beacon = iterator.next();
                    Identifier id1 = beacon.getId1();
                    double rssi = beacon.getRssi();
                    int txPower = beacon.getTxPower();
                    double distance = Double.parseDouble(mDecimalFormat.format(beacon.getDistance()));
                    int major = beacon.getId2().toInt();
                    int minor = beacon.getId3().toInt();
                    String uuid = id1.toHexString().substring(2);

                    items.add(new Item(uuid, rssi, txPower, distance, major, minor));
                }

                for (Item item : items) {
                    String uuid = item.getAddress();
                    final double rssi = item.getRssi();
                    final int major = item.getMajor();
                    int minor = item.getMinor();

                    // Address 가 일치하는 Beacon 만 가져온다 &&  RSSI 가 -90 이상
                    if ((uuid.equals(mainUUID) || uuid.equals(parkingUUID)) && rssi >= -90) {
                        switch (major) {
                            // 로비 비컨
                            case 1:
                                Log.v(TAG, "BEACON 1...............");
                                if (mBeaconServiceUsage == BeaconServiceUsage.ONEPASS) {
                                    mBeaconFunction.OnlyOpenLobby(uuid, major, minor, rssi);
                                } else {
                                    mBeaconFunction.LOBBY_BEACON(uuid, major, minor, rssi);
                                }
                                break;
                            // 주차 출입 - 시작 비컨 1
                            case 2:
                                Log.v(TAG, "BEACON 2...............");
                                if (mBeaconServiceUsage != BeaconServiceUsage.ONEPASS) {
                                    mBeaconFunction.ENTRANCE_BEACON(major, minor, rssi);
                                }
                                break;
                            // 로비 2 - 엘리베이터
                            case 3:
                                if (mBeaconServiceUsage != BeaconServiceUsage.ONEPASS) {
                                    Log.v(TAG, "BEACON 3........");
                                    mBeaconFunction.ELEVATOR_BEACON(major, minor, rssi);
                                }
                                break;
                            // 주차면 상태 평시
                            case 4:
                                if (mBeaconServiceUsage != BeaconServiceUsage.ONEPASS) {
                                    Log.v(TAG, "BEACON 4........");
                                    mBeaconFunction.StayBeacon(major, minor, rssi, mSaveArrayListValue);
                                }
                                break;
                            // 주차면 상태 - 변화가 있을 때
                            case 5:
                                if (mBeaconServiceUsage != BeaconServiceUsage.ONEPASS) {
                                    Log.v(TAG, "BEACON 5........");
                                    mBeaconFunction.ChangeBeacon(major, minor, rssi, mSaveArrayListValue);
                                }
                                break;
                            // 시작 비컨 2
                            case 6:
                                if (mBeaconServiceUsage != BeaconServiceUsage.ONEPASS) {
                                    Log.d(TAG, "BEACON 6.....");
                                    mBeaconFunction.PARKING_BEACON(major, minor, rssi);
                                }
                                break;
                        }
                    }
                }
            }
        });

        //region 있어야 작동되는 것
        try {
            BeaconManager_W.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, "ERROR : " + e.getMessage());
        }

        BeaconManager_W.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
            }

            @Override
            public void didExitRegion(Region region) {
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
            }
        });

        try {
            BeaconManager_W.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, "ERROR : " + e.getMessage());
        }
        //endregion
    }

    // Bluetooth Low Energy Scanning Start
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void StartBluetoothScanning() {
        if (!isStartScanning) {
            if (LeScanner_W != null) {
                LeScanner_W.startScan(mFilters, mScanSettings, scanCallback);
                isStartScanning = true;
            }
        }
    }

    // Bluetooth Low Energy Scanning Stop
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void StopBluetoothScanning() {
        if (isStartScanning) {
            if (LeScanner_W != null) {
                LeScanner_W.stopScan(scanCallback);
                isStartScanning = false;
            }
        }
    }

    // 안드로이드 버전이 오레오 이상이면 실행
    @RequiresApi(api = Build.VERSION_CODES.O)
    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onBeaconServiceConnect - O");
            super.onScanResult(callbackType, result);

            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord == null) return;
            SparseArray<byte[]> sparseArray = scanRecord.getManufacturerSpecificData();
            byte[] byteValue = sparseArray.valueAt(0);

            if (byteValue != null) {
                if (byteValue.length >= 23) {
                    String uuid = String.format("%02x", byteValue[2] & 0xff) + String.format("%02x", byteValue[3] & 0xff)
                            + String.format("%02x", byteValue[4] & 0xff) + String.format("%02x", byteValue[5] & 0xff)
                            + String.format("%02x", byteValue[6] & 0xff) + String.format("%02x", byteValue[7] & 0xff)
                            + String.format("%02x", byteValue[8] & 0xff) + String.format("%02x", byteValue[9] & 0xff)
                            + String.format("%02x", byteValue[10] & 0xff) + String.format("%02x", byteValue[11] & 0xff)
                            + String.format("%02x", byteValue[12] & 0xff) + String.format("%02x", byteValue[13] & 0xff)
                            + String.format("%02x", byteValue[14] & 0xff) + String.format("%02x", byteValue[15] & 0xff)
                            + String.format("%02x", byteValue[16] & 0xff) + String.format("%02x", byteValue[17] & 0xff);

                    Log.d(TAG, "uuid : " + uuid);

                    if (uuid.equals(UserDataSingleton.getInstance().getLobbyBeaconUUID()) ||
                            uuid.equals(UserDataSingleton.getInstance().getParkingBeaconUUID())) {
                        final double rssi = result.getRssi();
                        String MajorValue = String.format("%02X", byteValue[18]) + String.format("%02X", byteValue[19]);
                        String MinorValue = String.format("%02X", byteValue[20]) + String.format("%02X", byteValue[21]);

                        final int major = Integer.valueOf(MajorValue, 16);
                        final int minor = Integer.valueOf(MinorValue, 16);

                        Log.d(TAG, "uuid : " + uuid);
                        Log.d(TAG, "major : " + major);
                        Log.d(TAG, "minor : " + minor);
                        Log.d(TAG, "rssi : " + rssi);

                        if (rssi >= -90) {
                            switch (major) {
                                // 로비 비컨
                                case 1:
                                    Log.v(TAG, "DETECT LOBBY BEACON rssi = " + rssi);
                                    if (mBeaconServiceUsage == BeaconServiceUsage.ONEPASS) {
                                        mBeaconFunction.OnlyOpenLobby(uuid, major, minor, rssi);
                                    } else {
                                        mBeaconFunction.LOBBY_BEACON(uuid, major, minor, rssi);
                                    }
                                    break;
                                // 주차 출입 - 시작 비컨 1
                                case 2:
                                    Log.v(TAG, "START BEACON");
                                    if (mBeaconServiceUsage != BeaconServiceUsage.ONEPASS) {
                                        mBeaconFunction.ENTRANCE_BEACON(major, minor, rssi);
                                    }
                                    break;
                                // 엘리베이터 3 - 엘리베이터
                                case 3:
                                    Log.v(TAG, "ELEVATOR");
                                    if (mBeaconServiceUsage != BeaconServiceUsage.ONEPASS) {
                                        mBeaconFunction.ELEVATOR_BEACON(major, minor, rssi);
                                    }
                                    break;
                                //주차면 상태 평시
                                case 4:
                                    if (mBeaconServiceUsage != BeaconServiceUsage.ONEPASS) {
                                        Log.v(TAG, "PARKING START");
                                        mBeaconFunction.StayBeacon(major, minor, rssi, mSaveArrayListValue);
                                    }
                                    break;
                                // 주차면 상태 - 변화가 있을 때
                                case 5:
                                    Log.v(TAG, "CHANGE PARKING");
                                    if (mBeaconServiceUsage != BeaconServiceUsage.ONEPASS) {
                                        mBeaconFunction.ChangeBeacon(major, minor, rssi, mSaveArrayListValue);
                                    }
                                    break;
                                // 시작 비컨 2
                                case 6:
                                    Log.v(TAG, "START BEACON 2");
                                    if (mBeaconServiceUsage != BeaconServiceUsage.ONEPASS) {
                                        mBeaconFunction.PARKING_BEACON(major, minor, rssi);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            LeScanner_W.stopScan(scanCallback);
            LeScanner_W.startScan(scanCallback);
        }
    };
}
