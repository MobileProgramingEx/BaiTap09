package com.baitap.socket;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BlueControl extends AppCompatActivity {

    ImageButton btnTb1, btnTb2, btnDis;
    TextView txt1, txtMAC;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isConnected = false;
    Set<BluetoothDevice> pairedDevices1;
    String address = null;
    private ProgressDialog progress;
    int flaglamp1;
    int flaglamp2;

    // SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // Kiểm tra quyền Bluetooth
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_SCAN,
                            android.Manifest.permission.BLUETOOTH_CONNECT,
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_BLUETOOTH_PERMISSIONS);
        }

        // Ánh xạ các đối tượng UI
        btnTb1 = findViewById(R.id.btnTb1);
        btnTb2 = findViewById(R.id.btnTb2);
        txt1 = findViewById(R.id.textV1);
        txtMAC = findViewById(R.id.textViewMAC);
        btnDis = findViewById(R.id.btnDisc);

        // Nhận địa chỉ Bluetooth từ Intent
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);

        new ConnectBT().execute(); // Gọi AsyncTask để kết nối Bluetooth

        btnTb1.setOnClickListener(v -> thietbi1()); // Gọi hàm điều khiển thiết bị 1
        btnTb2.setOnClickListener(v -> thietbi7()); // Gọi hàm điều khiển thiết bị 7
        btnDis.setOnClickListener(v -> Disconnect()); // Gọi hàm ngắt kết nối
    }

    // Hàm điều khiển thiết bị 1
    private void thietbi1() {
        if (btSocket != null) {
            try {
                String dataToSend;
                if (this.flaglamp1 == 0) {
                    this.flaglamp1 = 1;
                    this.btnTb1.setBackgroundResource(R.drawable.turnon);
                    dataToSend = "1";
                    btSocket.getOutputStream().write(dataToSend.getBytes());
                    txt1.setText("Thiết bị số 1 đang bật");
                } else {
                    this.flaglamp1 = 0;
                    this.btnTb1.setBackgroundResource(R.drawable.turnoff);
                    dataToSend = "A";
                    btSocket.getOutputStream().write(dataToSend.getBytes());
                    txt1.setText("Thiết bị số 1 đang tắt");
                }

                // In ra dữ liệu gửi đi
                Log.d("BluetoothData", "Dữ liệu gửi đi: " + dataToSend);

            } catch (IOException e) {
                msg("Lỗi");
            }
        }
    }

    // Hàm ngắt kết nối
    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Lỗi");
            }
        }
        finish();
    }

    // Hàm điều khiển thiết bị 7
    private void thietbi7() {
        if (btSocket != null) {
            try {
                String dataToSend;
                if (this.flaglamp2 == 0) {
                    this.flaglamp2 = 1;
                    this.btnTb2.setBackgroundResource(R.drawable.turnon);
                    dataToSend = "7";
                    btSocket.getOutputStream().write(dataToSend.getBytes());
                    txt1.setText("Thiết bị số 7 đang bật");
                } else {
                    this.flaglamp2 = 0;
                    this.btnTb2.setBackgroundResource(R.drawable.turnoff);
                    dataToSend = "G";
                    btSocket.getOutputStream().write(dataToSend.getBytes());
                    txt1.setText("Thiết bị số 7 đang tắt");
                }

                // In ra dữ liệu gửi đi
                Log.d("BluetoothData", "Dữ liệu gửi đi: " + dataToSend);

            } catch (IOException e) {
                msg("Lỗi");
            }
        }
    }

    // AsyncTask kết nối Bluetooth
    public class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(BlueControl.this, "Đang kết nối...", "Xin vui lòng đợi!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);

                    // Kiểm tra quyền kết nối Bluetooth
                    if (ContextCompat.checkSelfPermission(BlueControl.this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        btSocket.connect();
                    }
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Kết nối thất bại! Kiểm tra thiết bị.");
                finish();
            } else {
                msg("Kết nối thành công.");
                isConnected = true;
                pairedDevicesList1();
            }
            progress.dismiss();
        }
    }

    // Hiển thị thông báo Toast
    public void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    // Hiển thị danh sách các thiết bị đã ghép nối
    private void pairedDevicesList1() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            pairedDevices1 = myBluetooth.getBondedDevices();

            if (pairedDevices1.size() > 0) {
                for (BluetoothDevice bt : pairedDevices1) {
                    txtMAC.setText(bt.getName() + " - " + bt.getAddress());
                }
            } else {
                Toast.makeText(getApplicationContext(), "Không tìm thấy thiết bị kết nối.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Ứng dụng không có quyền BLUETOOTH_CONNECT.", Toast.LENGTH_LONG).show();
        }
    }

    // Xử lý kết quả yêu cầu quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }
    }
}
