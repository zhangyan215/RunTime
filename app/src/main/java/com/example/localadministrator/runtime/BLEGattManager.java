package com.example.localadministrator.runtime;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Zhy on 2016/1/24.
 */
public class BLEGattManager {
	private static final String TAG = BLEGattManager.class.getCanonicalName();
	private BluetoothManager bluetoothManager = null;
	private BluetoothAdapter bluetoothAdapter=null;
	private BluetoothLeAdvertiser bluetoothLeAdvertiser = null;
	private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<>();
	private Context mContext;
	private BLEPeripheral blePeripheral ;
	private BluetoothLeClass mBLE;
	private Handler mHandler1;
	private Handler mHandler2;
	private boolean mScanning ;

	public BLEGattManager(Context mContext){
		this.mContext = mContext;
	}
	public void bleInitialize() {
		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Log.e(TAG,"the device not support BLE ");
			return ;
		}
		// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
		// BluetoothAdapter through BluetoothManager.
		bluetoothManager = (BluetoothManager)mContext.getSystemService(mContext.BLUETOOTH_SERVICE);
		bluetoothAdapter = bluetoothManager.getAdapter();
		// Checks if Bluetooth is supported on the device.
		if (bluetoothAdapter == null) {
			Log.e(TAG, "the bluetooth is not supported");
			return;
		}
		if (!bluetoothAdapter.enable()) {
			bluetoothAdapter.enable();
		}
		bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
		if(bluetoothLeAdvertiser == null){
			Log.e(TAG, " the device not support peripheral mode");
			ParameterManager.isPeripheral = false;
			ParameterManager.isRequest = true;
		}else {
			Log.e(TAG, "the device support peripheral mode, maybe as request device");
			ParameterManager.isPeripheral = true;
			blePeripheral = new BLEPeripheral(mContext);
			if (blePeripheral.initialize()) {
				Log.i(TAG,"this device can  as assist device! and there need a function to determine the role of this device");
				// determine whether this device as request or assist device
				//ParameterManager.isRequest = false;
			} else {
				Log.i(TAG,"this device can only as request device!");
				ParameterManager.isRequest = true;
			}
		}
		mBLE = new BluetoothLeClass(mContext);
		if (!mBLE.initialize()) {
			Log.e(TAG, "Unable to initialize Bluetooth");
			return;
		}
		//callback when find the services on the ble devices;
		mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
		//events when the data is communicated;
		mBLE.setOnDataAvailableListener(mOnDataAvailable);
	}
	public void bleScan(){
		Timer timer = new Timer(false);
		timer.schedule(new BLEScan(), ParameterManager.delay, ParameterManager.period);
	}
	/**
	 * make the ble scan save energy
	 */
	class BLEScan extends TimerTask {
		@Override
		public void run() {
			scanLeDevice(true);
			//bluetoothAdapter.startLeScan(mLeScanCallback);
		}
	}

	/**
	 *
	 * @param enable
	 */
	private void scanLeDevice(final boolean enable) {
		/*Looper.prepare();
		mHandler1 = new Handler();
		Looper.loop();*/
		if (enable) {
			Log.d(TAG,"start ble scan to find nearby devices");
			// Stops scanning after a pre-defined scan period.
			innerDeviceService.mHandler1.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					bluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, ParameterManager.SCAN_PERIOD);

			mScanning = true;
			bluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			bluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}
	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			mLeDevices.add(device);
			if(device.getAddress()!=null){
				if(device.getName().contains("GPS")) {
					System.out.println("the address is :"+device.getAddress());

					bluetoothAdapter.stopLeScan(mLeScanCallback);
					mBLE.connect(device.getAddress());
					//0000180F-0000-1000-8000-00805f9b34fb
					//ScanRecord mScanRecord = (ScanRecord) scanRecord;
					String str1 = new String(scanRecord);
					System.out.println("the scanRecord is :" + scanRecord + "the length is:" + scanRecord.length + "the str is:" + str1 + "the data is:" + scanRecord.toString());

					//Utils.bytesToHexString(scanRecord);// 数组反转
					// 将Byte数组的数据以十六进制表示并拼接成字符串
					StringBuffer str = new StringBuffer();
					int i = 0;
					for (byte b : scanRecord) {
						i = (b & 0xff);
						str.append(Integer.toHexString(i));
					}
					String discoveryServceID = str.toString();
					Log.d(TAG, device.getName() + " scanRecord:\n" + discoveryServceID);

					// 查询是否含有指定的Service UUID信息
					if (discoveryServceID.indexOf("2161aff4c0215e621e1f8c36c495a93f"
							.replace("-", "")) != -1) {
						Log.d(TAG, device.getName() + " has available service UUID");
					}
					//Log.d(TAG,"the scanRecord is :"+parse+"\n"+parse.length());
					bluetoothAdapter.stopLeScan(mLeScanCallback);
					Log.d(TAG, "attempt to connect the device!");
					Log.d(TAG, "find the device :" + device.getName()+" "+device.getAddress());
				}

			}
		}


	};

	/**
	 * find the services on the ble devices;
	 */
	private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {

		@Override
		public void onServiceDiscover(BluetoothGatt gatt) {
			displayGattServices(mBLE.getSupportedGattServices());
		}

	};
	/**
	 * find the data communication
	 */
	private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new BluetoothLeClass.OnDataAvailableListener() {

		/**
		 * read the data on the ble devices
		 */

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status) {
			System.out.println("will read the value of the char");

			if (status == BluetoothGatt.GATT_SUCCESS)

				//String num1 = new String(characteristic.getValue());
				Log.e(TAG, "onCharRead " + gatt.getDevice().getName()
						+ " read "
						+ characteristic.getUuid().toString()
						+ " -> "
						+ Utils.bytesToHexString(characteristic.getValue()));
		}

		/**
		 * write date to the ble devices
		 */

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic) {
			Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
					+ " write "
					+ characteristic.getUuid().toString()
					+ " -> "
					+ new String(characteristic.getValue()));
		}
	};

	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null) return;

		for (BluetoothGattService gattService : gattServices) {
			//-----Service information-----//
			int type = gattService.getType();
			Log.e(TAG, "-->service type:" + Utils.getServiceType(type));
			Log.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
			Log.e(TAG, "-->service uuid:" + gattService.getUuid());

			//-----Characteristics information-----//
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());

				int permission = gattCharacteristic.getPermissions();
				Log.e(TAG, "---->char permission:" + Utils.getCharPermission(permission));

				int property = gattCharacteristic.getProperties();
				Log.e(TAG, "---->char property:" + Utils.getCharPropertie(property));
				// get the value of the characteristic
				byte[] data = gattCharacteristic.getValue();
				if (data != null && data.length > 0) {
					Log.e(TAG, "---->char value:" + new String(data));
				}

				//UUID_KEY_DATA is the uuid which can interact with ble Characteristic
				if (gattCharacteristic.getUuid().toString().equals(ParameterManager.GPS)) {
					//read the data of Characteristic will strike mOnDataAvailable.onCharacteristicRead()
					Looper.prepare();
					mHandler2 = new Handler();
					Looper.loop();
					innerDeviceService.mHandler2.postDelayed(new Runnable() {
						@Override
						public void run() {
							mBLE.readCharacteristic(gattCharacteristic);
						}
					}, 500);

					//receive the notice of Characteristic will be written,On receiving the data from bt will strike mOnDataAvailable.onCharacteristicWrite()
					mBLE.setCharacteristicNotification(gattCharacteristic, true);
					//set the value of the characteristic
					gattCharacteristic.setValue("send data->");
					//write data into the bT model
					mBLE.writeCharacteristic(gattCharacteristic);
				}

				//-----Descriptors information-----//
				List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
				for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
					Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
					int descPermission = gattDescriptor.getPermissions();
					Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));

					byte[] desData = gattDescriptor.getValue();
					if (desData != null && desData.length > 0) {
						Log.e(TAG, "-------->desc value:" + new String(desData));
					}
				}
			}
		}

	}



}
