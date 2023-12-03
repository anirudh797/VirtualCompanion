
package com.example.virtualcompanion.view
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualcompanion.BluetoothUtils
import com.example.virtualcompanion.IBluetoothSDKListener
import com.example.virtualcompanion.R
import java.util.ArrayList

/**
 * [RecyclerView.Adapter] that can display a [BluetoothDevice] and makes a call to the
 * specified [ListInteractionListener] when the element is tapped.
 *
 */
class DeviceRecyclerViewAdapter(listener: ListInteractionListener<BluetoothDevice?>?,val bluetoothAdapter: BluetoothAdapter) :
    RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>(){
    /**
     * The devices shown in this [RecyclerView].
     */
    private val devices: MutableList<BluetoothDevice?>

    /**
     * Callback for handling interaction events.
     */
    private val listener: ListInteractionListener<BluetoothDevice?>?

    /**
     * Controller for Bluetooth functionalities.
     */
//    private var bluetooth: BluetoothController? = null

    /**
     * {@inheritDoc}
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_device_item, parent, false)
        return ViewHolder(view)
    }

    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = devices[position]
        holder.mImageView.setImageResource(getDeviceIcon(devices[position]))
        holder.mDeviceNameView.text = devices[position]!!.name
        holder.mDeviceAddressView.text = devices[position]!!.address
        holder.mView.setOnClickListener { listener?.onItemClick(holder.mItem) }
    }

    /**
     * Returns the icon shown on the left of the device inside the list.
     *
     * @param device the device for the icon to get.
     * @return a resource drawable id for the device icon.
     */
    private fun getDeviceIcon(device: BluetoothDevice?): Int {
        return if (isAlreadyPaired(device)) {
            R.drawable.ic_bluetooth_connected
        } else {
            R.drawable.ic_bluetooth_static
        }
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

    /**
     * {@inheritDoc}
     */
    override fun getItemCount(): Int {
        return devices.size
    }


    /**
     * ViewHolder for a BluetoothDevice.
     */
    inner class ViewHolder(
        /**
         * The inflated view of this ViewHolder.
         */
        val mView: View
    ) : RecyclerView.ViewHolder(mView) {
        /**
         * The icon of the device.
         */
        val mImageView: ImageView

        /**
         * The name of the device.
         */
        val mDeviceNameView: TextView

        /**
         * The MAC address of the BluetoothDevice.
         */
        val mDeviceAddressView: TextView

        /**
         * The item of this ViewHolder.
         */
        var mItem: BluetoothDevice? = null

        /**
         * {@inheritDoc}
         */
        override fun toString(): String {
            return super.toString() + " '" + BluetoothUtils.deviceToString(mItem!!) + "'"
        }



        /**
         * Instantiates a new ViewHolder.
         *
         * @param view the inflated view of this ViewHolder.
         */
        init {
            mImageView = mView.findViewById<View>(R.id.device_icon) as ImageView
            mDeviceNameView = mView.findViewById<View>(R.id.device_name) as TextView
            mDeviceAddressView = mView.findViewById<View>(R.id.device_address) as TextView
        }
    }

    /**
     * Instantiates a new DeviceRecyclerViewAdapter.
     *
     * @param listener an handler for interaction events.
     */
    init {
        devices = ArrayList()
        this.listener = listener
    }

//    override fun onDiscoveryStarted() {
//        cleanView()
//        listener!!.startLoading()
//    }
//
//    override fun onDiscoveryStopped() {
//        listener!!.endLoading(false)
//
//    }
//

    fun onDiscoveryStarted(){
        cleanView()
        listener?.startLoading()
    }

    fun onDiscoveryStopped(){
        listener?.endLoading(false)
    }


    fun addDevice(bluetoothDevice: BluetoothDevice?){
        listener?.endLoading(true)
        if(bluetoothDevice!!.name?.isNotEmpty() == true){
            devices.add(bluetoothDevice)
            notifyDataSetChanged()
        }

    }

    fun onDevicePairingEnded(device:BluetoothDevice?,pairingDeviceStatus: Int) {
        when (pairingDeviceStatus) {
            BluetoothDevice.BOND_BONDING -> {

            }

            BluetoothDevice.BOND_BONDED -> {
                listener!!.endLoadingWithDialog(false, device)

                // Updates the icon for this element.
                notifyDataSetChanged()
            }

            BluetoothDevice.BOND_NONE ->                     // Failed pairing.
                listener!!.endLoadingWithDialog(true, device)
        }
    }


    /**
     * Cleans the view.
     */
    fun cleanView() {
        Log.d(BluetoothUtils.TAG,"Cleaned RV")
        devices.clear()
        notifyDataSetChanged()
    }

}