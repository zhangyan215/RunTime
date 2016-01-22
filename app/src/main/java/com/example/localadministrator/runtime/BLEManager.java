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
	// BLEDemo��Ҫ����
	private BluetoothAdapter mBtAdapter = null;
	private BluetoothGatt mBtGatt = null;
	private int mState = 0;
	private Context mContext;
	private BluetoothGattCharacteristic mWriteCharacteristic = null;
	private BluetoothGattCharacteristic mReadCharacteristric = null;

	private final String TAG = "BLE_Demo";

	// �豸����״̬
	private final int CONNECTED = 0x01;
	private final int DISCONNECTED = 0x02;
	private final int CONNECTTING = 0x03;

	// ��д��ص�Service��Characteristic��UUID
	public static final UUID TRANSFER_SERVICE_READ = UUID.fromString("34567817-2432-5678-1235-3c1d5ab44e17");
	public static final UUID TRANSFER_SERVICE_WRITE = UUID.fromString("34567817-2432-5678-1235-3c1d5ab44e18");
	public static final UUID TRANSFER_CHARACTERISTIC_READ = UUID.fromString("23487654-5678-1235-2432-3c1d5ab44e94");
	public static final UUID TRANSFER_CHARACTERISTIC_WRITE = UUID.fromString("23487654-5678-1235-2432-3c1d5ab44e93");

	// BLE�豸����ͨ�Ź����лص�
	private BluetoothGattCallback mBtGattCallback = new BluetoothGattCallback() {

		// ����״̬�����ı�ʱ�Ļص�
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

		// Զ���豸�еķ������ʱ�Ļص�
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

		// ĳCharacteristic��״̬Ϊ�ɶ�ʱ�Ļص�
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status) {

			if (status == BluetoothGatt.GATT_SUCCESS) {
				readCharacterisricValue(characteristic);

				// ����Զ���豸��characteristic��
				// ����characteristic�����ı�ʱ���ص�mBtGattCallback�е�onCharacteristicChanged����
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

		// д��Characteristic�ɹ����Ļص�
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic, int status) {

			switch (status) {
				case BluetoothGatt.GATT_SUCCESS:
					Log.d(TAG, "write data success");
					break;// д��ɹ�
				case BluetoothGatt.GATT_FAILURE:
					Log.d(TAG, "write data failed");
					break;// д��ʧ��
				case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
					Log.d(TAG, "write not permitted");
					break;// û��д���Ȩ��
			}
		}

		// ������Զ���豸��Characteristic��Ϣ��
		// ��Զ���豸��Characteristic��Ϣ�����ı��,�ص��˷���
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			readCharacterisricValue(characteristic);
		}

	};

	/**
	 * ��ȡBluetoothGattCharacteristic�е�����
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
	 * ��ָ�����豸��������
	 *
	 * @param device
	 */
	public void connect(BluetoothDevice device) {

		mBtGatt = device.connectGatt(mContext, false, mBtGattCallback);
		mState = CONNECTTING;
	}

	/**
	 * ��ʼ��
	 *
	 * @param context
	 * @return �����ʼ���ɹ��򷵻�true
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

	// BLE�豸���������еĻص����ڴ˿��Ը�����Χ�豸�㲥����Ϣ�����豸���й���
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
							 byte[] scanRecord) {

			Utils.bytesToHexString(scanRecord);// ���鷴ת
			// ��Byte�����������ʮ�����Ʊ�ʾ��ƴ�ӳ��ַ���
			StringBuffer str = new StringBuffer();
			int i = 0;
			for (byte b : scanRecord) {
				i = (b & 0xff);
				str.append(Integer.toHexString(i));
			}
			String discoveryServceID = str.toString();
			Log.d(TAG, device.getName() + " scanRecord:\n" + discoveryServceID);

			// ��ѯ�Ƿ���ָ����Service UUID��Ϣ
			if (discoveryServceID.indexOf(TRANSFER_SERVICE_WRITE.toString()
					.replace("-", "")) != -1) {

				Log.d(TAG, device.getName() + " has available service UUID");

				// �����Ǵ���ƥ����豸����

			}

		}

	};

	/**
	 * ��ʼBLE�豸ɨ��
	 */
	public void startScan() {
		mBtAdapter.startLeScan(mLeScanCallback);
	}

	/**
	 * ֹͣBLE�豸ɨ��
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
