package com.example.localadministrator.runtime;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

/**
 * Created by Zhy on 2015/11/20.
 */
public class BLEManager {
	// BLEDemo主要代码
	private BluetoothAdapter mBtAdapter = null;
	private BluetoothGatt mBtGatt = null;
	private int mState = 0;
	private Context mContext;
	private BluetoothGattCharacteristic mWriteCharacteristic = null;
	private BluetoothGattCharacteristic mReadCharacteristric = null;

	private final String TAG = "BLE_Demo";

	// 设备连接状态
	private final int CONNECTED = 0x01;
	private final int DISCONNECTED = 0x02;
	private final int CONNECTTING = 0x03;

	// 读写相关的Service、Characteristic的UUID
	public static final UUID TRANSFER_SERVICE_READ = UUID.fromString("34567817-2432-5678-1235-3c1d5ab44e17");
	public static final UUID TRANSFER_SERVICE_WRITE = UUID.fromString("34567817-2432-5678-1235-3c1d5ab44e18");
	public static final UUID TRANSFER_CHARACTERISTIC_READ = UUID.fromString("23487654-5678-1235-2432-3c1d5ab44e94");
	public static final UUID TRANSFER_CHARACTERISTIC_WRITE = UUID.fromString("23487654-5678-1235-2432-3c1d5ab44e93");

	// BLE设备连接通信过程中回调
	private BluetoothGattCallback mBtGattCallback = new BluetoothGattCallback() {

		// 连接状态发生改变时的回调
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
											int newState) {

			if (status == BluetoothGatt.GATT_SUCCESS) {
				mState = CONNECTED;
				Log.d(TAG, "connected OK");
				mBtGatt.discoverServices();
			} else if (newState == BluetoothGatt.GATT_FAILURE) {
				mState = DISCONNECTED;
				Log.d(TAG, "connect failed");
			}
		}

		// 远端设备中的服务可用时的回调
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {

			if (status == BluetoothGatt.GATT_SUCCESS) {
				BluetoothGattService btGattWriteService = mBtGatt
						.getService(TRANSFER_SERVICE_WRITE);
				BluetoothGattService btGattReadService = mBtGatt
						.getService(TRANSFER_SERVICE_READ);
				if (btGattWriteService != null) {
					mWriteCharacteristic = btGattWriteService
							.getCharacteristic(TRANSFER_CHARACTERISTIC_WRITE);
				}
				if (btGattReadService != null) {
					mReadCharacteristric = btGattReadService
							.getCharacteristic(TRANSFER_CHARACTERISTIC_READ);
					if (mReadCharacteristric != null) {
						mBtGatt.readCharacteristic(mReadCharacteristric);
					}
				}
			}
		}

		// 某Characteristic的状态为可读时的回调
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status) {

			if (status == BluetoothGatt.GATT_SUCCESS) {
				readCharacterisricValue(characteristic);

				// 订阅远端设备的characteristic，
				// 当此characteristic发生改变时当回调mBtGattCallback中的onCharacteristicChanged方法
				mBtGatt.setCharacteristicNotification(mReadCharacteristric,
						true);
				BluetoothGattDescriptor descriptor = mReadCharacteristric
						.getDescriptor(UUID
								.fromString("00002902-0000-1000-8000-00805f9b34fb"));
				if (descriptor != null) {
					byte[] val = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
					descriptor.setValue(val);
					mBtGatt.writeDescriptor(descriptor);
				}
			}
		}

		// 写入Characteristic成功与否的回调
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic, int status) {

			switch (status) {
				case BluetoothGatt.GATT_SUCCESS:
					Log.d(TAG, "write data success");
					break;// 写入成功
				case BluetoothGatt.GATT_FAILURE:
					Log.d(TAG, "write data failed");
					break;// 写入失败
				case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
					Log.d(TAG, "write not permitted");
					break;// 没有写入的权限
			}
		}

		// 订阅了远端设备的Characteristic信息后，
		// 当远端设备的Characteristic信息发生改变后,回调此方法
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			readCharacterisricValue(characteristic);
		}

	};

	/**
	 * 读取BluetoothGattCharacteristic中的数据
	 *
	 * @param characteristic
	 */
	private void readCharacterisricValue(
			BluetoothGattCharacteristic characteristic) {
		byte[] data = characteristic.getValue();
		StringBuffer buffer = new StringBuffer("0x");
		int i;
		for (byte b : data) {
			i = b & 0xff;
			buffer.append(Integer.toHexString(i));
		}
		Log.d(TAG, "read data:" + buffer.toString());
	}

	/**
	 * 与指定的设备建立连接
	 *
	 * @param device
	 */
	public void connect(BluetoothDevice device) {

		mBtGatt = device.connectGatt(mContext, false, mBtGattCallback);
		mState = CONNECTTING;
	}

	/**
	 * 初始化
	 *
	 * @param context
	 * @return 如果初始化成功则返回true
	 */
	public boolean init(Context context) {
		BluetoothManager btMrg = (BluetoothManager) context
				.getSystemService(Context.BLUETOOTH_SERVICE);
		if (btMrg == null)
			return false;
		mBtAdapter = btMrg.getAdapter();
		if (mBtAdapter == null)
			return false;
		mContext = context;
		return true;
	}

	// BLE设备搜索过程中的回调，在此可以根据外围设备广播的消息来对设备进行过滤
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
							 byte[] scanRecord) {

			Utils.bytesToHexString(scanRecord);// 数组反转
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
			if (discoveryServceID.indexOf(TRANSFER_SERVICE_WRITE.toString()
					.replace("-", "")) != -1) {

				Log.d(TAG, device.getName() + " has available service UUID");

				// 在这是处理匹配的设备……

			}

		}

	};

	/**
	 * 开始BLE设备扫描
	 */
	public void startScan() {
		mBtAdapter.startLeScan(mLeScanCallback);
	}

	/**
	 * 停止BLE设备扫描
	 */
	public void stopScan() {
		mBtAdapter.stopLeScan(mLeScanCallback);
	}

	/**
	 * send data and the largest length is 20 byte
	 *
	 * @param data
	 *
	 */
	private void sendData(byte[] data) {

		if (data != null && data.length > 0 && data.length < 21) {
			if (mWriteCharacteristic.setValue(data)
					&& mBtGatt.writeCharacteristic(mWriteCharacteristic)) {
				Log.d(TAG, "send data OK");
			}
		}
	}
}
