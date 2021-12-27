package com.crc.har.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crc.har.R;
import com.crc.har.base.CommonUtils;
import com.crc.har.base.Constants;
import com.crc.har.base.TextUtil;
import com.crc.har.bluetooth.SerialListener;
import com.crc.har.bluetooth.SerialService;
import com.crc.har.bluetooth.SerialSocket;

import org.w3c.dom.Text;

public class LoadingSerialActivity extends AppCompatActivity implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }

    private String deviceAddress;
    private SerialService service;

    private String strReceiveData;
//    private TextUtil.HexWatcher hexWatcher;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ImageView ivLoadingText = (ImageView) findViewById(R.id.iv_loading_text);

        deviceAddress = Constants.Companion.getMODULE_ADDRESS_HC06();
    }

    @Override
    protected void onDestroy() {
        if(connected != Connected.False) {
            disconnect();
        }
        stopService(new Intent(getApplicationContext(), SerialService.class));
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(service != null) {
            service.attach(this);
        } else {
            startService(new Intent(getApplicationContext(), SerialService.class));
        }
    }

    @Override
    protected void onStop() {
        if(service != null && !isChangingConfigurations()) {
            service.detach();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder)binder).getService();
        service.attach(this);
        if(initialStart) {
            initialStart = false;
            runOnUiThread(this::connect);
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    @Override
    public void onSerialConnect() {
        connected = Connected.True;

    }

    @Override
    public void onSerialConnectError(Exception e) {
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        disconnect();
    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
//            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void receive(byte[] data) {

        String msg = new String(data);
        strReceiveData = (String) TextUtil.toCaretString(msg, true);
        Toast.makeText(this, "string : " + strReceiveData, Toast.LENGTH_SHORT);
//        if(hexEnabled) {
//            strReceiveData = commonUtils.toHexString(data);
//        } else {
//            String msg = new String(data);
//            if(newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
//                // don't show CR as ^M if directly before LF
//                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
//                // special handling if CR and LF come in separate fragments
//                if (pendingNewline && msg.charAt(0) == '\n') {
//                    Editable edt = receiveText.getEditableText();
//                    if (edt != null && edt.length() > 1)
//                        edt.replace(edt.length() - 2, edt.length(), "");
//                }
//                pendingNewline = msg.charAt(msg.length() - 1) == '\r';
//            }
//            receiveText.append(TextUtil.toCaretString(msg, newline.length() != 0));
//        }
    }

}
