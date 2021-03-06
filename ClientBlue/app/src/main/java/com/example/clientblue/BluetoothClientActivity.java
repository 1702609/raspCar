package com.example.clientblue;
// contains two classes - BluetoothClientActivity and BluetoothRequestTask
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothClientActivity extends Activity {

	// Well known SPP UUID
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final int REQUEST_ENABLE_BT = 8675309;
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private TextView mLogTextView;
	private StickyButton mStartButton;
	private Button mClearTextButton;
	private BluetoothAdapter mAdapter;
	private BluetoothDevice mDevice;
	private Button goingF, goingB, goingL, goingR;

	@SuppressLint({ "NewApi", "InlinedApi" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mLogTextView = findViewById(R.id.textview_output);
		mStartButton = findViewById(R.id.button_start_server);
		mClearTextButton = findViewById(R.id.button_clear_text);
		goingF = findViewById(R.id.up);
		goingB = findViewById(R.id.bottom);
		goingL = findViewById(R.id.left);
		goingR = findViewById(R.id.right);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mAdapter = BluetoothAdapter.getDefaultAdapter();
		} else {
			final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			mAdapter = manager.getAdapter();
		}
		if (savedInstanceState != null) {
			mLogTextView.setText(savedInstanceState.getString("log"));
			mDevice = savedInstanceState.getParcelable("device");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) { //after selecting the paired device
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == RESULT_OK) {
				appendMessage("Bluetooth enabled!");
				//new BluetoothTask(this).execute(MESSAGE);
			} else if (resultCode == RESULT_CANCELED) {
				appendMessage("Bluetooth was not enabled. Connection cancelled.");
				unstickStartButton();
			}
			break;
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras()
						.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BluetoothDevice object
				mDevice = mAdapter.getRemoteDevice(address);
				// Attempt to connect to the device
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("log", mLogTextView.getText().toString());
		outState.putParcelable("device", mDevice);
		((NoGuavaBaseApplication<BluetoothClientActivity>) getApplication()).detachActivity(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onResume() {
		super.onResume();
		((NoGuavaBaseApplication<BluetoothClientActivity>) getApplication()).attachActivity(this);
	}

	void appendMessage(final String newText)
		{
		mLogTextView.setText(newText);
		}

	void unstickStartButton() {
		appendMessage("");
		mStartButton.unstick();
	}

	public void sendMessage(View v)
		{
		goingF.setOnTouchListener(new View.OnTouchListener()
			{
			@Override
			public boolean onTouch(View v, MotionEvent event)
				{
				switch(event.getAction())
					{
					case MotionEvent.ACTION_DOWN: //holding down
						new BluetoothRequestTask(BluetoothClientActivity.this,mDevice).execute("f\n");
						return true;
					case MotionEvent.ACTION_UP: // released
						new BluetoothRequestTask(BluetoothClientActivity.this,mDevice).execute("s\n");
						return true;
					}
				return false;
				};
			});

			goingB.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					switch(event.getAction())
					{
						case MotionEvent.ACTION_DOWN: //holding down
							new BluetoothRequestTask(BluetoothClientActivity.this,mDevice).execute("b\n");
							Toast.makeText(getApplicationContext(),"going forward", Toast.LENGTH_SHORT);
							return true;
						case MotionEvent.ACTION_UP: // released
							new BluetoothRequestTask(BluetoothClientActivity.this,mDevice).execute("s\n");
							return true;
					}
					return false;
				};
			});

			goingL.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					switch(event.getAction())
					{
						case MotionEvent.ACTION_DOWN: //holding down
							new BluetoothRequestTask(BluetoothClientActivity.this,mDevice).execute("l\n");
							return true;
						case MotionEvent.ACTION_UP: // released
							new BluetoothRequestTask(BluetoothClientActivity.this,mDevice).execute("s\n");
							return true;
					}
					return false;
				};
			});

			goingR.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					switch(event.getAction())
					{
						case MotionEvent.ACTION_DOWN: //holding down
							new BluetoothRequestTask(BluetoothClientActivity.this,mDevice).execute("r\n");
							return true;
						case MotionEvent.ACTION_UP: // released
							new BluetoothRequestTask(BluetoothClientActivity.this,mDevice).execute("s\n");
							return true;
					}
					return false;
				};
			});
			}



	public void startServer(View v) {
		if (mAdapter == null) {
			appendMessage("FATAL ERROR: Bluetooth is not supported on this device.");
		} else if (!mAdapter.isEnabled()) {
			//Ask user to enable Bluetooth
			appendMessage("Bluetooth is not currently enabled. Attempting to enable...");
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			if (mDevice == null) {
				Intent serverIntent = new Intent(BluetoothClientActivity.this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); //fetch registered device and get result
			} else
				{ //selected device
				new BluetoothRequestTask(this,mDevice);
			}
		}
	}

	public void clearLog(View v) {
		mLogTextView.setText("");
	}

	private class BluetoothRequestTask extends AsyncActivityTask<BluetoothClientActivity, String, String, String> {
		private BluetoothDevice mDevice;
		private BluetoothSocket mSocket;
		private OutputStream mOutStream;
		private InputStream mInStream;

		public BluetoothRequestTask(BluetoothClientActivity activity, BluetoothDevice device)
			{
			super(activity);
			mDevice = device;
			}

		@Override
		protected String doInBackground(String... params) {
			publishProgress("Attempting socket creation...");
			try {
				mSocket = mDevice.createRfcommSocketToServiceRecord(BluetoothClientActivity.MY_UUID);
				publishProgress("Socket created! Attempting socket connection...");
			} catch (IOException e) {
				e.printStackTrace();
				publishProgress("ERROR: Socket creation failed.");
				return null;
			}

			mAdapter.cancelDiscovery();
			try {
				mSocket.connect();
			} catch (IOException e) {
				e.printStackTrace();
				publishProgress("ERROR: Socket connection failed. Ensure that the server is up and try again.");
				return null;
			}

			publishProgress("Attempting to send data to server. Creating output stream...");
			String message = params[0];
			if (message == null) {
				message = "No message provided!";
			}
			byte[] messageBytes = message.getBytes();
			publishProgress("Output stream created! Sending message (" + message + ") to server...");
			try {
				mOutStream = mSocket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
				publishProgress("ERROR: Output stream creation failed. Ensure that the server is up and try again.");
				return null;
			}

			try {
				mOutStream.write(messageBytes);
			} catch (IOException e) {
				e.printStackTrace();
				publishProgress("ERROR: Message sending failed. Ensure that the server is up and try again.");
				return null;
			}

			publishProgress("Message sent! Preparing for server response...");
			try {
				mInStream = mSocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
				publishProgress("ERROR: Input stream creation failed. Ensure that the server is up and try again.");
				return null;
			}
			BufferedReader serverReader = new BufferedReader(new InputStreamReader(mInStream));
			String response = null;
			char[] buffer = new char[5000];
			try {
				/**
				 * WARNING! If the Android device is not connected to the server by this point,
				 * calling read() will crash the app without throwing an exception!
				 */
				serverReader.read(buffer);
				response = new String(buffer);
			} catch (IOException e) {
				e.printStackTrace();
				publishProgress("ERROR: Failed to read server response. Ensure that the server is up and try again.");
				return null;
			}
			return response;
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			getActivity().appendMessage(progress[0]);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			end();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				getActivity().appendMessage("Response from server: " + result);
			}
			end();
		}

		private void end() {
			try {
				if (mOutStream != null) {
					mOutStream.flush();
					mOutStream.close();
				}
				if (mInStream != null) {
					mInStream.close();
				}
				if (mSocket != null) {
					mSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				publishProgress("ERROR: Closing something failed.");
			}
			getActivity().unstickStartButton();
		}
	}
}