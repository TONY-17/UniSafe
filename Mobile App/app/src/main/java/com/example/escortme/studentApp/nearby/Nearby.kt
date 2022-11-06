package com.example.escortme.studentApp.nearby

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.example.escortme.InitialActivity
import com.example.escortme.R
import com.example.escortme.studentApp.ui.home.HomeFragment
import com.example.escortme.utils.Helpers
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.nio.charset.StandardCharsets


/*
 * This class handles user interactions of the Nearby BottomSheet
 * The code is only valid if the user shakes their phone which in turn puts the student in discovery mode
 * In discovery mode the student(user) is able to detect other available devices through NearbyAPI
 */
class Nearby : NearbyAPIV2() {

    // Nearby API attributes
    private val STRATEGY = Strategy.P2P_STAR
    private val SERVICE_ID = "com.example.escortme.studentApp.ui.home.SERVICE_ID"
    private val mName = InitialActivity.username

    // Global variables
    lateinit var name: TextView
    lateinit var update: TextView
    lateinit var update2: TextView
    lateinit var device: TextView
    lateinit var request: MaterialCardView
    lateinit var innerText: TextView
    lateinit var animation: LottieAnimationView
    lateinit var animationSent : LottieAnimationView
    lateinit var progress: CircularProgressIndicator
    lateinit var nearbySearch: MaterialCardView
    private var changeView: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.activity_nearby, container, false)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme);

        progress = view.findViewById(R.id.circularProgressIndicator);
        nearbySearch = view.findViewById(R.id.startNearbySearch);

        innerText = view.findViewById(R.id.innerText)
        animation = view.findViewById(R.id.nearbyAnimation)
        animationSent = view.findViewById(R.id.doneRequestHelp);
        name = view.findViewById(R.id.studentNameNearby)
        device = view.findViewById(R.id.studentDeviceNearby)
        request = view.findViewById(R.id.sendRequest);
        update = view.findViewById(R.id.textView62)
        update2 = view.findViewById(R.id.textView63)
        // Start device scan
        nearbySearch.setOnClickListener {
            nearbySearch.setBackgroundColor(Color.WHITE)
            progress.isVisible = true
            innerText.isVisible = false
            val layoutParams = nearbySearch.layoutParams
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            animation.playAnimation()
            startDeviceSearch()
        }

        request.setOnClickListener {
            /*
             * Send the emergency alert to the connect device
             */
            val emergencyAlert =
                InitialActivity.username + "," + HomeFragment.userLocation.latitude + "," + HomeFragment.userLocation.longitude

            val bytes = emergencyAlert.toByteArray(StandardCharsets.UTF_8)
            val bytesPayload = Payload.fromBytes(bytes)
            send(bytesPayload)

            request.isCheckable = false
            request.isVisible = false
            nearbySearch.isVisible = false
            animation.isVisible = false
            animationSent.isVisible = true
            animationSent.playAnimation()
            name.isVisible = false
            device.isVisible = false
            update.text = "Help is on the way"
            update2.text = "We have sent authorities your name and location and you'll receive help soon."

        }
        return view
    }


    private fun deviceNameAnim(studentName: String, emergencyLocation: String) {
        nearbySearch.visibility = View.GONE
        progress.visibility = View.GONE
        val anim = arrayOfNulls<Animation>(1)
        anim[0] = AnimationUtils.loadAnimation(
            context,
            R.anim.bottom_to_original
        )
        name.visibility = View.VISIBLE
        name.text = studentName
        device.visibility = View.VISIBLE
        device.text = emergencyLocation
        request.visibility = View.VISIBLE
        name.animation = anim[0]
        device.animation = anim[0]
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                heightSetup(it)
                behaviour.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED);

        return dialog
    }

    override fun onReceive(endpoint: Endpoint?, payload: Payload?) {
        TODO("Not yet implemented")
    }

    override fun onEndpointDisconnected(e: Endpoint?) {
        dismiss()
        disconnectAll()
        stopDiscovering()
    }

    override fun onConnectionFailed(remove: Endpoint?) {
        dismiss()
        disconnectAll()
        stopDiscovering()
    }

    override fun onConnectionInitiated(endpoint: Endpoint?, connectionInfo: ConnectionInfo?) {
        allowIncomingRequest(endpoint)
    }

    override fun getName(): String {
        return mName
    }

    override fun onEndpointConnected(endpoint: Endpoint?) {
        Helpers.success(
            activity,
            "Connected to " + endpoint!!.id
        )
        deviceNameAnim("${endpoint.name.uppercase()}", "DEVICE | ${endpoint!!.id}")
    }

    override fun onAdvertisingStarted() {
        TODO("Not yet implemented")
    }

    override fun onAdvertisingFailed() {
        TODO("Not yet implemented")
    }

    override fun getStrategy(): Strategy {
        return STRATEGY
    }

    override fun onDiscoveryStarted() {
        Toast.makeText(context, "Discovery started", Toast.LENGTH_LONG).show()
    }

    override fun onDiscoveryFailed() {
        Toast.makeText(context, "Discovery Failed", Toast.LENGTH_LONG).show()
    }

    override fun onEndpointDiscovered(endpoint: Endpoint?) {
        if (!isAskingForConnection) {
            connectToDevice(endpoint)
            changeView = true
        }
    }

    override fun getServiceId(): String {
        return SERVICE_ID
    }

    override fun onAcceptRequestFailed() {
        TODO("Not yet implemented")
    }


    private fun heightSetup(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //super.onViewCreated(view, savedInstanceState)
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        HomeFragment.isOpen = false

        disconnectAll()
        stopDiscovering()

    }
}

