package com.baitap.socket;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button btnPaired;
    ListView listDanhSach;
    public static int REQUEST_BLUETOOTH = 1;
    public static int REQUEST_PERMISSION = 2;
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnPaired = findViewById(R.id.btnTimthietbi);
        listDanhSach = findViewById(R.id.listTb);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_LONG).show();
            finish();
        } else if (!myBluetooth.isEnabled()) {
            Intent turnBTOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Thiết bị bluetooth chưa bật", Toast.LENGTH_LONG).show();
            }
            Toast.makeText(getApplicationContext(), "Thiết bị bluetooth chưa bật", Toast.LENGTH_LONG).show();
            startActivityForResult(turnBTOn, REQUEST_BLUETOOTH);
        }

        btnPaired.setOnClickListener(v -> pairedDevicesList());
    }

    private void pairedDevicesList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Ứng dụng chưa được cấp quyền truy cập Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }

        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "Không tìm thấy thiết bị đã ghép nối", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listDanhSach.setAdapter(adapter);
        listDanhSach.setOnItemClickListener(myListClickListener);
    }

    private final AdapterView.OnItemClickListener myListClickListener = (av, v, arg2, arg3) -> {
        String info = ((TextView) v).getText().toString();
        String address = info.substring(info.length() - 17);

        Intent i = new Intent(MainActivity.this, BlueControl.class);
        i.putExtra(EXTRA_ADDRESS, address);
        startActivity(i);
    };

    // Xử lý khi người dùng chọn quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Ứng dụng cần quyền để hoạt động", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
