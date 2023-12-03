package com.example.virtualcompanion

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.virtualcompanion.BluetoothUtils.Companion.TAG
import com.example.virtualcompanion.databinding.ActivityMainBinding
import com.example.virtualcompanion.view.DPadView
import com.example.virtualcompanion.view.DeviceRecyclerViewAdapter
import com.example.virtualcompanion.view.ListInteractionListener
import com.example.virtualcompanion.view.RecyclerViewProgressEmptySupport

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var recyclerViewAdapter : DeviceRecyclerViewAdapter
    private var recyclerView: RecyclerViewProgressEmptySupport? = null
    var bondingProgressDialog: ProgressDialog? = null


    lateinit var buttons: ArrayList<View>
    private lateinit var mService: BluetoothSDKService
    lateinit var vibrator: Vibrator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)
        binding.fab.setOnClickListener { view ->
            if (::mService.isInitialized) {
                mService.startDeviceDiscovery(this)
            }
        }
        vibrator = getSystemService(Vibrator::class.java)
        setupRV()
        assignButtonActions()

        Log.d(TAG,"OnCreate")

    }


    private fun setupRV() {
        // Sets up the RecyclerView.
        recyclerViewAdapter = DeviceRecyclerViewAdapter(listInteractionListener, BluetoothAdapter.getDefaultAdapter())
        recyclerView = findViewById<View>(R.id.list) as RecyclerViewProgressEmptySupport
        recyclerView!!.layoutManager = LinearLayoutManager(this)

        // Sets the view to show when the dataset is empty. IMPORTANT : this method must be called
        // before recyclerView.setAdapter().
        val emptyView = findViewById<View>(R.id.empty_list)
        recyclerView!!.setEmptyView(emptyView)
        // Sets the view to show during progress.
        val progressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        recyclerView!!.setProgressView(progressBar)
        recyclerView!!.adapter = recyclerViewAdapter
//        BluetoothSDKListenerHelper.registerBluetoothSDKListener(this, recyclerViewAdapter)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun assignButtonActions() {
        setupCustomDpadActions()
        val btnPower: View = findViewById(R.id.ntfBtnPower)
        val btnBack: View = findViewById(R.id.ntfBtnBack)

        val circularDpadUp : View = findViewById(R.id.dpad_custom)
        val btnHome: View = findViewById(R.id.home)
        val btnVolInc: View = findViewById(R.id.vol_plus)
        val btnVolDec: View = findViewById(R.id.vol_minus)
        val btnMute: View = findViewById(R.id.mute)
        val cancelController: View = findViewById(R.id.cancel)
        val btnChUp: View = findViewById(R.id.ch_up)
        val btnChDown: View = findViewById(R.id.ch_down)

        val epg: View = findViewById(R.id.guideline)


//        cancelController.setOnClickListener {
//            stopService()
//            updateViewForDeviceList()
//        }

        buttons = ArrayList<View>()

        buttons.add(cancelController)
        buttons.add(circularDpadUp)
        buttons.add(btnHome)
        buttons.add(btnBack)
        buttons.add(btnVolDec)
        buttons.add(btnVolInc)
//        buttons.add(btnPlayPause)
        buttons.add(btnPower)
//        buttons.add(btnMenu)
        buttons.add(btnMute)
//        buttons.add(btnRewind)
//        buttons.add(btnForward)
        buttons.add(btnChUp)
        buttons.add(btnChDown)
        buttons.apply {
//            add(btnRecord)
//            add(btnInfo)
//            add(epg)
//            add(btnMagenta)
        }
        binding.cancelConnection.setOnClickListener{
            resetView()
            mService.disconnectFromDevice()
        }

        setButtonsEnabled(true)
        buttons.forEach {

        }
        addRemoteKeyListeners(btnPower, RemoteControlHelper.Key.POWER)
        addRemoteKeyListeners(epg, RemoteControlHelper.Key.EPG)
        addRemoteKeyListeners(btnBack, RemoteControlHelper.Key.BACK)
        addRemoteKeyListeners(btnHome, RemoteControlHelper.Key.HOME)
        addRemoteKeyListeners(btnVolInc, RemoteControlHelper.Key.VOLUME_INC)
        addRemoteKeyListeners(btnVolDec, RemoteControlHelper.Key.VOLUME_DEC)
        addRemoteKeyListeners(btnChUp, RemoteControlHelper.Key.CHANNEL_UP)
        addRemoteKeyListeners(btnChDown, RemoteControlHelper.Key.CHANNEL_DOWN)
        addRemoteKeyListeners(btnMute, RemoteControlHelper.Key.MUTE)

    }

    private fun setButtonsEnabled(enabled: Boolean) {
        for (button in buttons) {
            button.isEnabled = enabled
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("ClickableViewAccessibility")
    private fun addRemoteKeyListeners(button: View, keys: ByteArray) {

        button.setOnTouchListener { view: View?, motionEvent: MotionEvent ->
            if(!mService.isHidDeviceConnected){
                resetView()
                return@setOnTouchListener false
            }
            val now = System.currentTimeMillis()
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val sent = mService?.let {
                    RemoteControlHelper.sendKeyDown(
                        keys[0].toInt(),
                        keys[1].toInt(), it
                    )
                }
                button.background = AppCompatResources.getDrawable(this@MainActivity,R.drawable.ic_round_white)
                if (sent == true) vibrate()
            }
            else if (motionEvent.action == MotionEvent.ACTION_UP) {
                val sent = mService?.let { RemoteControlHelper.sendKeyUp(it) }
                if (button.id == R.id.ntfBtnPower) {
                    button.background =
                        AppCompatResources.getDrawable(this@MainActivity, R.drawable.ic_round_red)
                } else {
                    button.background =
                        AppCompatResources.getDrawable(this@MainActivity, R.drawable.ic_round)
                }
                return@setOnTouchListener true
            }

            else if (motionEvent.action ==MotionEvent.ACTION_MOVE){
                Log.e(TAG,"Move");
                return@setOnTouchListener true
            }
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    25,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun addRemoteKeyListenersForDpad(action: Int, keys: ByteArray) {

        if(!mService.isHidDeviceConnected){
            resetView()
            return
        }

        if (action == MotionEvent.ACTION_DOWN) {
            val sent = mService?.let {
                RemoteControlHelper.sendKeyDown(
                    keys[0].toInt(),
                    keys[1].toInt(), it
                )
            }
//                if (sent) MainActivity.vibrate()
        }
        else if (action == MotionEvent.ACTION_UP) {
            val sent = mService?.let { RemoteControlHelper.sendKeyUp(it) }
        }

        else if (action ==MotionEvent.ACTION_MOVE){
            Log.e(TAG,"Move");
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupCustomDpadActions() {

        val dpad = findViewById<DPadView>(R.id.dpad_custom)
        dpad.modify {

            isHapticFeedbackEnabled = true
            normalColor = getColor(R.color.grey)
            pressedColor = getColor(R.color.white_75_opaque)
            padding = 20f
            directionSectionAngle = 90f

            isCenterCircleEnabled = true
            isCenterCirclePressEnabled = true

            centerCircleNormalColor = getColor(R.color.dark_grey)
            centerCirclePressedColor = getColor(R.color.white_75_opaque)

            centerCircleRatio = 5f
            centerIcon = null

            centerText = "OK"

            centerIconTint = 0

            centerTextSize = 36f

            var style = 0
            style = style or DPadView.TextStyle.BOLD.style


            centerTextStyle = style

            centerTextColor = getColor(R.color.white)
        }


        dpad.onDirectionPressListener = { direction, action ->

            when (direction) {

                DPadView.Direction.UP -> addRemoteKeyListenersForDpad(action,RemoteControlHelper.Key.MENU_UP)
                DPadView.Direction.DOWN -> addRemoteKeyListenersForDpad(action,RemoteControlHelper.Key.MENU_DOWN)
                DPadView.Direction.LEFT -> addRemoteKeyListenersForDpad(action,RemoteControlHelper.Key.MENU_LEFT)
                DPadView.Direction.RIGHT -> addRemoteKeyListenersForDpad(action,RemoteControlHelper.Key.MENU_RIGHT)
                DPadView.Direction.CENTER -> addRemoteKeyListenersForDpad(action,RemoteControlHelper.Key.MENU_PICK)
                else -> {}
            }
            val text = StringBuilder()
            val directionText = direction?.name ?: ""
            if (directionText.isNotEmpty()) {
                text.append("Direction:\t")
            }
            text.append(directionText)
            if (directionText.isNotEmpty()) {
                text.append("\nAction:\t")
                val actionText = when (action) {
                    MotionEvent.ACTION_DOWN -> "Down"
                    MotionEvent.ACTION_UP -> "Up"
                    MotionEvent.ACTION_MOVE -> "Move"
                    else -> action.toString()
                }
                text.append(actionText)
            }
//            findViewById<TextView>(R.id.tv_sample).text = text;

            dpad.onDirectionClickListener = {
                it?.let {
                    Log.i("directionPress", it.name)
                    when (it) {
//                    DPadView.Direction.UP -> addRemoteKeyListenersForDpad()

                        else -> {}
                    }
                }

                dpad.setOnClickListener {
                    Log.i("Click", "Done")
//                    addRemoteKeyListenersForDpad(action,RemoteControlHelper.Key.MENU_PICK)
                }

                dpad.onCenterLongClick = {
                    addRemoteKeyListenersForDpad(action,RemoteControlHelper.Key.MENU_PICK)
                }
            }


        }

    }

    var listInteractionListener = object :ListInteractionListener<BluetoothDevice?>{
        override fun onItemClick(item: BluetoothDevice?) {
            if (mService.isAlreadyPaired(item)) { //already paired
                if (item != null) {
                    mService.stopDeviceDiscovery()
                    mService.connectToDevice(item)
                }
            }
            else{ // try to pair
                val outcome = mService.pair(item)
                val deviceName = getDeviceName(item)
                if (outcome) {
                    // The pairing has started, shows a progress dialog.
                    Log.d(TAG, "Showing pairing dialog")
                    bondingProgressDialog =
                        ProgressDialog.show(this@MainActivity, "", "Pairing with device $deviceName...", true, false)
                } else {
                    Log.d(TAG, "Error while pairing with device $deviceName!")
                    Toast.makeText(
                        this@MainActivity,
                        "Error while pairing with device $deviceName!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

        override fun startLoading() {
            recyclerView!!.startLoading()

            // Changes the button icon.
            binding.fab!!.setImageResource(R.drawable.ic_bluetooth)
        }

        override fun endLoading(partialResults: Boolean) {
            recyclerView!!.endLoading()

            // If discovery has ended, changes the button icon.
            if (!partialResults) {
                binding.fab!!.setImageResource(R.drawable.ic_bluetooth_static)
            }
        }



        override fun endLoadingWithDialog(error: Boolean, device: BluetoothDevice?) {
            if (bondingProgressDialog != null) {
                val view = findViewById<View>(R.id.main_content)
                val message: String
                val deviceName = getDeviceName(device)

                // Gets the message to print.
                message = if (error) {
                    "Failed pairing with device $deviceName!"
                } else {
                    "Succesfully paired with device $deviceName!"
                }

                // Dismisses the progress dialog and prints a message to the user.
                bondingProgressDialog!!.dismiss()
                Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()

                // Cleans up state.
                bondingProgressDialog = null
            }
        }
        }


    /**
     * Gets the name of a device. If the device name is not available, returns the device address.
     *
     * @param device the device whose name to return.
     * @return the name of the device or its address if the name is not available.
     */
    fun getDeviceName(device: BluetoothDevice?): String? {
        var deviceName = device!!.name
        if (deviceName == null) {
            deviceName = device.address
        }
        return deviceName
    }


    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {

        bindBluetoothService()

        // Register Listener
        BluetoothSDKListenerHelper.registerBluetoothSDKListener(context, mBluetoothListener)

        return super.onCreateView(name, context, attrs)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Bind Bluetooth Service
     */
    private fun bindBluetoothService() {
        // Bind to LocalService
        Log.d(TAG,"Bind bluetoothService")
        Intent(
            applicationContext,
            BluetoothSDKService::class.java
        ).also { intent ->
            applicationContext.bindService(
                intent,
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }


    /**
     * Handle service connection
     */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BluetoothSDKService.LocalBinder
             mService = binder.getService()
            Log.d(TAG,"Service connected")
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG,"Service Disconnected")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return false
    }

    private val mBluetoothListener: IBluetoothSDKListener = object : IBluetoothSDKListener {
        override fun onDiscoveryStarted() {
            Toast.makeText(this@MainActivity,"Device discovery started",Toast.LENGTH_SHORT).show()
            recyclerViewAdapter.onDiscoveryStarted()
        }

        override fun onDiscoveryStopped() {
            Toast.makeText(this@MainActivity,"Device discovery stopped",Toast.LENGTH_SHORT).show()
            recyclerViewAdapter.onDiscoveryStopped()
        }

        override fun onDeviceDiscovered(device: BluetoothDevice?,name: String) {
            Toast.makeText(this@MainActivity,"Device Discovered ${name}",Toast.LENGTH_SHORT).show()
            recyclerViewAdapter.addDevice(device)
        }

        override fun onDeviceConnected(device: BluetoothDevice?) {
            Log.d(TAG,"onDevice Connected")
            Toast.makeText(this@MainActivity, "Device connected : $device", Toast.LENGTH_SHORT)
                .show()
            // Do stuff when is connected
            showController()
        }

        override fun onMessageReceived(device: BluetoothDevice?, message: String?) {
        }

        override fun onMessageSent(device: BluetoothDevice?) {
        }

        override fun onError(message: String?) {
            resetView()
        }

        override fun onDeviceDisconnected() {
            Log.d(TAG,"onDevice DisConnected")
            resetView()
        }

        override fun onDevicePairingEnded() {

            if (mService.isPairingInProgress) {
                val device = mService.boundingDevice
                if (device != null) {
                    recyclerViewAdapter.onDevicePairingEnded(device, mService.pairingDeviceStatus)
                }
                else{
                    recyclerViewAdapter.onDevicePairingEnded(device,mService.pairingDeviceStatus)
                }
            }
        }

    }

    private fun showController() {
        binding.controllerView.visibility = View.VISIBLE
        binding.list.visibility = View.GONE
    }

    fun resetView(){
        binding.controllerView.visibility = View.GONE
        binding.list.visibility = View.VISIBLE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothSDKListenerHelper.unregisterBluetoothSDKListener(applicationContext, mBluetoothListener)
    }
}