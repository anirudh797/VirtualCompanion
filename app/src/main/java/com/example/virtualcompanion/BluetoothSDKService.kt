package com.example.virtualcompanion

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.virtualcompanion.BluetoothUtils.Companion.ACTION_DEVICE_CONNECTED
import com.example.virtualcompanion.BluetoothUtils.Companion.ACTION_DEVICE_DISCONNECTED
import com.example.virtualcompanion.BluetoothUtils.Companion.TAG
import com.google.android.things.bluetooth.BluetoothConnectionManager
import com.intentfilter.androidpermissions.PermissionManager
import com.intentfilter.androidpermissions.PermissionManager.PermissionRequestListener
import com.intentfilter.androidpermissions.models.DeniedPermissions
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Arrays
import java.util.UUID


class BluetoothSDKService : Service() {

    // Service Binder
    private val binder = LocalBinder()

    // Bluetooth stuff
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDevices: MutableSet<BluetoothDevice>
    private var connectedDevice: BluetoothDevice? = null
    private val MY_UUID = UUID.fromString("cbbfe0e2-f7f3-4206-84e0-84cbb3d09dfc")
    private val RESULT_INTENT = 15

    // Bluetooth connections
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    private var mAcceptThread: AcceptThread? = null

    val isPairingInProgress: Boolean
        get() = boundingDevice != null

    // Invoked only first time
    override fun onCreate() {
        super.onCreate()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    fun startDeviceDiscovery(context: Context) {
        binder.startDiscovery(context)
    }

    fun getBluetoothAdapter(): BluetoothAdapter {
        return bluetoothAdapter
    }

    fun stopDeviceDiscovery() {
        binder.stopDiscovery()
    }


    // Invoked every service star
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * Checks if a device is already paired.
     *
     * @param device the device to check.
     * @return true if it is already paired, false otherwise.
     */
    fun isAlreadyPaired(device: BluetoothDevice?): Boolean {
        return bluetoothAdapter!!.bondedDevices.contains(device)
    }

    fun connectToDevice(bluetoothDevice: BluetoothDevice?) {
        bluetoothAdapter.enable()
        if (isHidDeviceConnected && connectThread?.isAlive == true) {
            pushBroadcastMessage(ACTION_DEVICE_CONNECTED, bluetoothDevice, "")

        } else {
            disconnectFromDevice()
            connectThread = bluetoothDevice?.let {
                ConnectThread(it)
            }
            connectThread?.start()
        }
    }

    fun stopConnection() {
    }

    fun disconnectFromDevice() {
        connectedThread?.let {
            it.cancel()
        }
        connectThread?.let {
            it.cancel()
        }
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, bluetoothHidDevice)
        bluetoothHidDevice?.unregisterApp()
    }

//    fun getInstance(): BluetoothConnectionManager? {
//        if (bluetoothConnectionManager == null) {
//            bluetoothConnectionManager = BluetoothConnectionManager()
//        }
//        return bluetoothConnectionManager
//    }

    /**
     * Class used for the client Binder.
     */
    inner class LocalBinder : Binder() {
        /*
        Function that can be called from Activity or Fragment
        */
        /**
         * Enable the discovery, registering a broadcastreceiver {@link discoveryBroadcastReceiver}
         * The discovery filter by LABELER_SERVER_TOKEN_NAME
         */
        public fun startDiscovery(context: Context) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            registerReceiver(discoveryBroadcastReceiver, filter)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissionManager = PermissionManager.getInstance(context)
                permissionManager.checkPermissions(
                    setOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ),
                    object : PermissionRequestListener {
                        override fun onPermissionGranted() {
                            Toast.makeText(
                                context,
                                "Permissions Granted Bluetooth Scan ",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            bluetoothAdapter.startDiscovery()
                        }

                        override fun onPermissionDenied(deniedPermissions: DeniedPermissions) {
                            val deniedPermissionsText =
                                "Denied: " + Arrays.toString(deniedPermissions.toTypedArray())
                            Toast.makeText(context, deniedPermissionsText, Toast.LENGTH_SHORT)
                                .show()
                            for (deniedPermission in deniedPermissions) {
                                if (deniedPermission.shouldShowRationale()) {
                                    // Display a rationale about why this permission is required
                                }
                            }
                        }
                    })
                return
            }
            bluetoothAdapter.startDiscovery()
            pushBroadcastMessage(BluetoothUtils.ACTION_DISCOVERY_STARTED, null, null)
        }

        fun getService() = this@BluetoothSDKService


        /**
         * stop discovery
         */
        public fun stopDiscovery() {
            bluetoothAdapter.cancelDiscovery()
            pushBroadcastMessage(BluetoothUtils.ACTION_DISCOVERY_STOPPED, null, null)
        }
    }


    /**
     * Broadcast Receiver for catching ACTION_FOUND aka new device discovered
     */
    private val discoveryBroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            /*
              Our broadcast receiver for manage Bluetooth actions
            */
            val device =
                intent!!.getParcelableExtra<BluetoothDevice>(BluetoothUtils.EXTRA_DEVICE)
            val message = intent.getStringExtra(BluetoothUtils.EXTRA_NAME)
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    pushBroadcastMessage(
                        BluetoothUtils.ACTION_DEVICE_FOUND,
                        device,
                        message
                    )
                }

                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    var state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    );
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            pushBroadcastMessage(
                                BluetoothUtils.ACTION_BLUETOOTH_STATE_OFF,
                                device,
                                message
                            )
                        }
                        // Bluetooth has been turned off;

                        BluetoothAdapter.STATE_ON -> {
                            pushBroadcastMessage(
                                BluetoothUtils.ACTION_BLUETOOTH_STATE_OFF,
                                device,
                                message
                            )
                        }
                        // Bluetooth is on
                    }
                }
            }
        }
    }
    var boundingDevice: BluetoothDevice? = null

    fun pair(bluetoothDevice: BluetoothDevice?): Boolean {
        if (bluetoothAdapter!!.isDiscovering) {
            Log.d(TAG, "Bluetooth cancelling discovery.")
            stopDeviceDiscovery()
        }
        Log.d(TAG, "Bluetooth bonding with device: " + bluetoothDevice?.let {
            BluetoothUtils.deviceToString(
                it
            )
        })
        val outcome = bluetoothDevice?.createBond() ?: false
        Log.d(TAG, "Bounding outcome : $outcome")

        // If the outcome is true, we are bounding with this device.
        if (outcome) {
            boundingDevice = bluetoothDevice
        }
        pushBroadcastMessage(BluetoothDevice.ACTION_BOND_STATE_CHANGED, boundingDevice, null)

        return outcome
    }

    /**
     * Returns the status of the current pairing and cleans up the state if the pairing is done.
     *
     * @return the current pairing status.
     * @see BluetoothDevice.getBondState
     */
    val pairingDeviceStatus: Int
        get() {
            checkNotNull(boundingDevice) { "No device currently bounding" }
            val bondState = boundingDevice!!.bondState
            // If the new state is not BOND_BONDING, the pairing is finished, cleans up the state.
            if (bondState != BluetoothDevice.BOND_BONDING) {
                boundingDevice = null
            }
            return bondState
        }

    var bluetoothHidDevice: BluetoothHidDevice? = null
    var isHidDeviceConnected = false
    var mmDevice: BluetoothDevice? = null


    private val listener = object : BluetoothProfile.ServiceListener {
        @SuppressLint("MissingPermission", "SuspiciousIndentation")
        @RequiresApi(Build.VERSION_CODES.P)
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                bluetoothHidDevice = proxy as BluetoothHidDevice
                Log.d(
                    TAG,
                    "onServiceConnected profile == BluetoothProfile.HID_DEVICE $bluetoothHidDevice"
                )
                val callback: BluetoothHidDevice.Callback = object : BluetoothHidDevice.Callback() {
                    override fun onAppStatusChanged(
                        pluggedDevice: BluetoothDevice?,
                        registered: Boolean
                    ) {
                        super.onAppStatusChanged(pluggedDevice, registered)
                        Log.d(TAG, "onAppStatusChanged registered=$registered")
                        val deviceConnected = bluetoothHidDevice?.connect(
                            mmDevice
                        )
                        if (deviceConnected == true) {
                            Log.d(TAG, "Connected to " + mmDevice!!.name)
                        }
                    }

                    override fun onGetReport(
                        device: BluetoothDevice,
                        type: Byte,
                        id: Byte,
                        bufferSize: Int
                    ) {
                        super.onGetReport(device, type, id, bufferSize)
                        Log.d(TAG, "onGetReport")
                    }

                    override fun onSetReport(
                        device: BluetoothDevice,
                        type: Byte,
                        id: Byte,
                        data: ByteArray
                    ) {
                        super.onSetReport(device, type, id, data)
                        Log.d(TAG, "onSetReport")
                    }

                    override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
                        kotlin.run {  }
                        var stateStr = ""
                        when (state) {
                            BluetoothHidDevice.STATE_CONNECTED -> {
                                stateStr = "STATE_CONNECTED"
                                Log.d(TAG, "Hid Device state connected")
                                pushBroadcastMessage(
                                    ACTION_DEVICE_CONNECTED,
                                    mmDevice,
                                    message = "Connected"
                                )
                                isHidDeviceConnected = true

                            }

                            BluetoothHidDevice.STATE_DISCONNECTED -> {
                                stateStr = "STATE_DISCONNECTED"
                                Log.d(TAG, "Hid Device state disconnected")
                                isHidDeviceConnected = false
                                pushBroadcastMessage(
                                    ACTION_DEVICE_DISCONNECTED,
                                    mmDevice,
                                    message = "Connected"
                                )
                            }

                            BluetoothHidDevice.STATE_CONNECTING -> {
                                stateStr = "STATE_CONNECTING"
                                Log.d(TAG, "Hid Device State connecting")
                            }

                            BluetoothHidDevice.STATE_DISCONNECTING -> {
                                bluetoothHidDevice?.unregisterApp()
                                isHidDeviceConnected = false
                                stateStr =
                                    "STATE_DISCONNECTING"
                                Log.d(TAG, "Hid Device State Disconnecting")
                            }
                        }
                        val isProfileSupported: Boolean = HidUtils.isProfileSupported(device)
                        Log.d(TAG, "isProfileSupported $isProfileSupported")
                        Log.d(TAG, "HID " + device.name + " " + device.address + " " + stateStr)
                    }

                    override fun onSetProtocol(device: BluetoothDevice, protocol: Byte) {
                        super.onSetProtocol(device, protocol)
                        Log.d(TAG, "onSetProtocol")
                    }
                }

                Log.d(TAG, "Connected to HidDevice" + bluetoothHidDevice!!+" mmDevice $mmDevice ")
                val register = bluetoothHidDevice!!.registerApp(
                    Constants.SDP_RECORD,
                    null,
                    null,
                    { obj: Runnable -> obj.run() },
                    callback
                )
//                val deviceConnected = bluetoothHidDevice?.connect(
//                    mmDevice
//                )
//                if (deviceConnected == true) {
//                    isHidDeviceConnected = true
                    Log.d(TAG, "Connected to " + mmDevice!!.name)
                    pushBroadcastMessage(ACTION_DEVICE_CONNECTED,mmDevice,"")
//                }
                Log.d(TAG,"register App successfull $register")

            }

        }

        @RequiresApi(Build.VERSION_CODES.P)
        override fun onServiceDisconnected(profile: Int) {
            Log.d(TAG, "onServiceDisConnected profile $profile")
            isHidDeviceConnected = false
            pushBroadcastMessage(
                ACTION_DEVICE_DISCONNECTED,
                mmDevice,
                message = "Disconnected"
            )
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, bluetoothHidDevice)
            bluetoothHidDevice!!.unregisterApp()

        }


    }


    private inner class AcceptThread : Thread() {
        // Body
        val UUID: UUID = java.util.UUID.fromString("cbbfe0e2-f7f3-4206-84e0-84cbb3d09dfc")
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord("Virtual Companion", UUID)
        }

        @RequiresApi(Build.VERSION_CODES.P)
        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    pushBroadcastMessage(BluetoothUtils.ACTION_DEVICE_DISCONNECTED, mmDevice, "")
                    null

                }
                socket?.also {
                    manageMyConnectedSocket(it)
                    shouldLoop = false
                }
            }
        }
    }

    private inner class ConnectThread(val device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                mmDevice = device
                Log.d(TAG,"Connected Device from Connect Thread $mmDevice")
                manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmDevice = null
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }

        // Body
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun manageMyConnectedSocket(socket: BluetoothSocket) {
        startConnectedThread(socket)
    }

    @Synchronized
    private fun startConnectedThread(
        bluetoothSocket: BluetoothSocket?,
    ) {
        connectedThread = ConnectedThread(bluetoothSocket!!)
        connectedThread!!.start()
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        // Body
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        @RequiresApi(Build.VERSION_CODES.P)
        override fun run() {
            var numBytes: Int // bytes returned from read()

            Log.d(TAG, "Manage myConnectedSocket $mmSocket")
            val flag = bluetoothAdapter.getProfileProxy(
                this@BluetoothSDKService,
                listener,
                BluetoothProfile.HID_DEVICE
            )
            Log.d(TAG, "Manage myConnectedSocket $mmSocket profileProxy $flag")
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    pushBroadcastMessage(
                        BluetoothUtils.ACTION_CONNECTION_ERROR,
                        null,
                        "Input stream was disconnected"
                    )
                    break
                }

                val message = String(mmBuffer, 0, numBytes)

                mmDevice = mmSocket.remoteDevice
                // Send to broadcast the message
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_MESSAGE_RECEIVED,
                    mmSocket.remoteDevice,
                    message
                )

            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)

                // Send to broadcast the message
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_MESSAGE_SENT,
                    mmSocket.remoteDevice,
                    null
                )
            } catch (e: IOException) {
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_CONNECTION_ERROR,
                    null,
                    "Error occurred when sending data"
                )
                return
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_CONNECTION_ERROR,
                    null,
                    "Could not close the connect socket"
                )
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            releaseBluetooth()
            unregisterReceiver(discoveryBroadcastReceiver)
        } catch (e: Exception) {
            // already unregistered
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    fun releaseBluetooth(){
        Log.d(TAG,"service destroyed , release resources")
        connectedThread?.cancel()
        connectThread?.cancel()
        bluetoothHidDevice?.unregisterApp()
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE,bluetoothHidDevice)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private fun pushBroadcastMessage(action: String, device: BluetoothDevice?, message: String?) {
        val intent = Intent(action)
        if (device != null) {
            intent.putExtra(BluetoothUtils.EXTRA_DEVICE, device)
        }
        if (message != null) {
            intent.putExtra(BluetoothUtils.EXTRA_MESSAGE, message)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }



}