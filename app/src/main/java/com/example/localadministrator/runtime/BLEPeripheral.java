package com.example.localadministrator.runtime;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Zhy on 2016/1/24.
 */
public class BLEPeripheral {
	private static final String TAG = BLEPeripheral.class.getCanonicalName();
	private BluetoothManager bluetoothManager ;
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothGattService runtime = null;
	private BluetoothGattCharacteristic gps,http,file = null;
	public BluetoothGattServer mGattServer ;
	private Context mContext;
	public BLEPeripheral(Context mContext){
		this.mContext = mContext;
	}

	public boolean initialize(){

		if(bluetoothManager == null){
			bluetoothManager = (BluetoothManager) mContext.getSystemService(mContext.BLUETOOTH_SERVICE);
			if(bluetoothManager==null){
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
			//if bluetoothManager is not null , get the GattServer
			mGattServer = bluetoothManager.openGattServer(mContext, new BluetoothGattServerCallback() {
				@Override
				public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
					super.onConnectionStateChange(device, status, newState);
				}

			});
		}

		bluetoothAdapter = bluetoothManager.getAdapter();
		if (bluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;

	}

	/**
	 * used to set the bluetoothGattService and bluetoothGattCharacteristic
	 * 1 the permission of the bluetoothGattCharacteristic is not sure ;
	 */

	public void registerSet() {
		runtime = new BluetoothGattService(UUID.fromString(ParameterManager.RUNTIME),BluetoothGattService.SERVICE_TYPE_PRIMARY);
		gps =new BluetoothGattCharacteristic(UUID.fromString(ParameterManager.GPS),
				BluetoothGattCharacteristic.PROPERTY_READ,BluetoothGattCharacteristic.PERMISSION_READ);
		http = new BluetoothGattCharacteristic(UUID.fromString(ParameterManager.HTTP),
				BluetoothGattCharacteristic.PROPERTY_READ,BluetoothGattCharacteristic.PERMISSION_READ);
		file = new BluetoothGattCharacteristic(UUID.fromString(ParameterManager.FILE),
				BluetoothGattCharacteristic.PROPERTY_READ,BluetoothGattCharacteristic.PERMISSION_READ);
		runtime.addCharacteristic(gps);
		runtime.addCharacteristic(http);
		runtime.addCharacteristic(file);

		try{
			boolean i = gps.setValue("wk".getBytes("UTF-8"));
			System.out.println("the value is :"+i);
		} catch (Exception e){
			e.printStackTrace();
		}
		String str11 = new String(gps.getValue());
		System.out.println("the value is wk:"+str11);

	}
	private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
		@Override
		public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
			super.onConnectionStateChange(device, status, newState);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothGatt.STATE_CONNECTED) {
					//mBluetoothDevices.add(device);
					//updateConnectedDevicesStatus();
					Log.v(TAG, "Connected to device: " + device.getAddress());
				} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
					//mBluetoothDevices.remove(device);
					//updateConnectedDevicesStatus();
					Log.v(TAG, "Disconnected from device");
				}
			} else {
				//mBluetoothDevices.remove(device);
				// updateConnectedDevicesStatus();
				// There are too many gatt errors (some of them not even in the documentation) so we just
				// show the error to the user.
				final String errorMessage = "wrong" + ": " + status;
				/*runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//Toast.makeText(DeviceScanActivity.this, errorMessage, Toast.LENGTH_LONG).show();
					}
				});*/
				Log.e(TAG, "Error when connecting: " + status);
			}
		}

		@Override
		public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
												BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
			Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
			Log.d(TAG, "Value: " + Arrays.toString(characteristic.getValue()));
			if (offset != 0) {
				mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
            /* value (optional) */ null);
				return;
			}
			mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
					offset, characteristic.getValue());
		}

		@Override
		public void onNotificationSent(BluetoothDevice device, int status) {
			super.onNotificationSent(device, status);
			Log.v(TAG, "Notification sent. Status: " + status);
		}

		@Override
		public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
												 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
												 int offset, byte[] value) {
			super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
					responseNeeded, offset, value);
			Log.v(TAG, "Characteristic Write request: " + Arrays.toString(value));
			// int status = mCurrentServiceFragment.writeCharacteristic(characteristic, offset, value);
			if (responseNeeded) {
				mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
            /* No need to respond with an offset */ 0,
            /* No need to respond with a value */ null);
			}
		}
	};
	/**
	 * THE advertiseCallback
	 */
	private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
		@Override
		public void onStartSuccess(AdvertiseSettings settingsInEffect) {
			super.onStartSuccess(settingsInEffect);
			Log.d(TAG, "Start Advertising бнбн");
		}

		/**
		 * onStartFailure()
		 *
		 * @param errorCode 1: ADVERTISE_FAILED_DATA_TOO_LARGE
		 *                  2: ADVERTISE_FAILED_TOO_MANY_ADVERTISERS
		 *                  3: ADVERTISE_FAILED_ALREADY_STARTED
		 *                  4: ADVERTISE_FAILED_INTERNAL_ERROR
		 *                  5: ADVERTISE_FAILED_FEATURE_UNSUPPORTED
		 */
		@Override
		public void onStartFailure(int errorCode) {
			super.onStartFailure(errorCode);
			Log.e(TAG, "onStartFailure errorCode" + errorCode);

			if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
				Log.e(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");

			} else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
				Log.e(TAG, "Failed to start advertising because no advertising instance is available.");

			} else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
				Log.e(TAG, "Failed to start advertising as the advertising is already started");

			} else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
				Log.e(TAG, "Operation failed due to an internal error");

			} else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
				Log.e(TAG, "This feature is not supported on this platform");

			}
		}
	};
	/** create AdvertiseSettings */
	public static AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
		AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
		mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
		mSettingsbuilder.setConnectable(connectable);
		mSettingsbuilder.setTimeout(timeoutMillis);
		mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
		AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();

		return mAdvertiseSettings;
	}
	/** create AdvertiseData */
	public static AdvertiseData createAdvertiseData(){
		AdvertiseData.Builder    mDataBuilder = new AdvertiseData.Builder();
		mDataBuilder.setIncludeDeviceName(true);
		// mDataBuilder.addServiceUuid(ParcelUuid.fromString(UUID_KEY_DATA));
		mDataBuilder.addServiceUuid(ParcelUuid.fromString(ParameterManager.RUNTIME));
		// mDataBuilder.addServiceUuid(ParcelUuid.fromString(BATTERY_LEVEL_UUID));
		//mDataBuilder.addServiceData()

		AdvertiseData mAdvertiseData = mDataBuilder.build();
		System.out.println(mAdvertiseData.toString());
		return mAdvertiseData;
	}

}
