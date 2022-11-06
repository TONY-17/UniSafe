package com.example.escortme.studentApp.nearby;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.pubnub.api.PubNubException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class NearbyAPI extends Fragment {


    // Devices discovered (near a user)
    private final Map<String, Endpoint> discoveredDevices = new HashMap<>();
    // Pending to be connected
    private final Map<String, Endpoint> devicesWaiting = new HashMap<>();
    // Successful connections
    private final Map<String, Endpoint> connectionsEstablished = new HashMap<>();

    /*
     * Asking another device for a connection
     * Can only request connection from a single device
     */
    private boolean askingForConnection = false;
    // Currently discovering devices (searching for devices)
    private boolean discovering = false;
    // Currently being discoverable by other devices (Be visible to other drivers)
    private boolean advertising = false;
    // Handle the nearby connections
    private ConnectionsClient connectionsClient;

    // Request these permissions at runtime
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS;
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            REQUIRED_PERMISSIONS =
                    new String[] {
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE
                    };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            REQUIRED_PERMISSIONS =
                    new String[] {
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE
                    };
        } else {
            REQUIRED_PERMISSIONS =
                    new String[] {
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE
                    };
        }
    }


    // Callback if a connection has been established/failed
    private final ConnectionLifecycleCallback lifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
            Log.d(TAG, String.format("onConnectionInitiated: ID=>%s NAME=%s", s, connectionInfo.getEndpointName()));
            // Grab the endpoint if we successfully started a connection
            Endpoint endpoint = new Endpoint(s, connectionInfo.getEndpointName());
            devicesWaiting.put(s, endpoint);
            NearbyAPI.this.onConnectionInitiated(endpoint, connectionInfo);
        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
            Log.d(TAG, String.format("onConnectionResult: ID=>%s ConnectionResolution=%s", s, connectionResolution));
            discovering = false;
            // If the connection was not successful
            if (!connectionResolution.getStatus().isSuccess()) {
                Log.w(TAG, "Connection failed:");
                onConnectionFailed(devicesWaiting.remove(s));
                return;
            }
            connectedToEndpoint(devicesWaiting.remove(s));
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            if (!connectionsEstablished.containsKey(s)) {
                Log.d(TAG, String.format(
                        "Disconnected from :%s",
                        s
                ));
            }
            disconnectedFromEndpoint(connectionsEstablished.get(s));
        }
    };
    // Callback to return data that can be possibly be sent from other devices
    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
            // Data received from another device
            Log.d(TAG, String.format(
                    "onPayloadReceived: ID=%s LOAD=%s",
                    s, payload
            ));
            try {
                onReceive(connectionsEstablished.get(s), payload);
            } catch (PubNubException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
            Log.d(TAG, String.format(
                    "onPayloadReceived: ID=%s UPDATE=%s",
                    s,
                    payloadTransferUpdate
            ));
        }
    };

    protected abstract void onReceive(Endpoint endpoint, Payload payload) throws PubNubException;

    private void disconnectedFromEndpoint(Endpoint e) {
        connectionsEstablished.remove(e.getId());
        onEndpointDisconnected(e);
    }

    protected Set<Endpoint> getDiscoveredEndpoints() {
        return new HashSet<>(discoveredDevices.values());
    }

    protected Set<Endpoint> getConnectedEndpoints() {
        return new HashSet<>(connectionsEstablished.values());
    }

    /*
    * Allows users to send and receive data through payload
     */

    protected void send(Payload payload) {
        send(payload, connectionsEstablished.keySet());
    }

    private void send(Payload payload, Set<String> endpoints) {
        connectionsClient
                .sendPayload(new ArrayList<>(endpoints), payload)
                .addOnFailureListener(
                        e -> Log.i(TAG, "onSendFailure: "));
    }
    protected abstract void onEndpointDisconnected(Endpoint e);

    protected abstract void onConnectionFailed(Endpoint remove);

    private void connectedToEndpoint(Endpoint e) {
        connectionsEstablished.put(e.getId(), e);
        onEndpointConnected(e);
    }

    protected final boolean isAskingForConnection(){
        return askingForConnection;
    };

    protected abstract void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo);

    /*
     * Make user visible to other devices that are searching for devices to connect to
     * Puts the device in advertising mode
     */

    protected void enableAdvertisingMode() {
        Log.d(TAG, "Advertising start: ");
        // Specify that we are advertising the device
        advertising = true;
        // The end point name of the device that is advertising themself
        final String userEndPointName = getName();

        AdvertisingOptions.Builder advertisingOptions = new AdvertisingOptions.Builder();
        advertisingOptions.setStrategy(getStrategy());

        connectionsClient
                .startAdvertising(
                        userEndPointName,
                        getServiceId(),
                        lifecycleCallback,
                        advertisingOptions.build())
                .addOnFailureListener(e -> {
                    // Failed to start advertising
                    advertising = false;
                    Log.d(TAG, "Failed to start advertising: ");
                    onAdvertisingFailed();
                }).addOnSuccessListener(unused -> {
                    Log.d(TAG, String.format(
                            "onSuccess: Advertising = %s",
                            userEndPointName
                    ));
                    onAdvertisingStarted();
                });
    }

    // Disconnect an End
    protected void disconnect(Endpoint endpoint) {
        connectionsClient.disconnectFromEndpoint(endpoint.getId());
        connectionsEstablished.remove(endpoint.getId());
    }

    protected void disconnectAll() {
        for (Endpoint e : connectionsEstablished.values()) {
            connectionsClient.disconnectFromEndpoint(e.getId());
        }
        // Remove all from list
        connectionsEstablished.clear();
    }

    protected void resetState() {
        connectionsClient.stopAllEndpoints();
        askingForConnection = false;
        advertising = false;
        discovering = false;
        devicesWaiting.clear();
        connectionsEstablished.clear();
        discoveredDevices.clear();
    }

    protected abstract String getName();

    protected void disableAdvertisingMode() {
        System.out.println("disableAdvertisingMode");
        advertising = false;
        connectionsClient.stopAdvertising();
    }

    protected void connectToDevice(Endpoint endpoint) {
        Log.w(TAG, String.format(
                "connecting To Device: ", endpoint
        ));
        askingForConnection = true;
        connectionsClient.requestConnection(
                getName(),
                endpoint.getId(),
                lifecycleCallback
        ).addOnFailureListener(e -> {
            askingForConnection = false;
            onConnectionFailed(endpoint);
        });
    }

    protected abstract void onEndpointConnected(Endpoint endpoint);


    // Check if a device is advertising themself or on
    protected boolean isAdvertising() {
        return advertising;
    }

    protected abstract void onAdvertisingStarted();

    protected abstract void onAdvertisingFailed();

    // Allows the connection
    // acceptConnection
    protected void allowIncomingRequest(final Endpoint endpoint) {
        connectionsClient
                .acceptConnection(endpoint.getId(), payloadCallback)
                // If we failed to accept the connection
                .addOnFailureListener(e -> {
                    Log.d(TAG, String.format("onAcceptConnectionFailure: %s ", e));
                    onAcceptRequestFailed();
                });
    }

    protected void declineIncomingRequest(Endpoint endpoint) {
        connectionsClient
                .rejectConnection(endpoint.getId())
                // we failed to reject the request
                .addOnFailureListener(e -> Log.d(TAG, String.format("onRejectConnectionFailure: ", e)));
    }

    // Start searching for drivers that are advertising themselves
    protected void startDeviceSearch() {
        System.out.println("SEARCHING FOR DEVICES ");
        discovering = true;
        discoveredDevices.clear();
        DiscoveryOptions.Builder discoveryOptions = new DiscoveryOptions.Builder();
        discoveryOptions.setStrategy(getStrategy());

        connectionsClient
                .startDiscovery(
                        getServiceId(),
                        new EndpointDiscoveryCallback() {
                            @Override
                            public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                                Log.w(TAG, String.format(
                                        "onEndpointFound: ID=%s SID=%s NAME=%s",
                                        s, discoveredEndpointInfo.getServiceId(), discoveredEndpointInfo.getEndpointName()
                                ));
                                System.out.println(String.format(
                                        "onEndpointFound: ID=%s SID=%s NAME=%s",
                                        s, discoveredEndpointInfo.getServiceId(), discoveredEndpointInfo.getEndpointName()
                                ));
                                if (getServiceId().equals(discoveredEndpointInfo.getServiceId())) {
                                    Endpoint endpoint = new Endpoint(s, discoveredEndpointInfo.getEndpointName());
                                    discoveredDevices.put(s, endpoint);
                                    onEndpointDiscovered(endpoint);
                                }
                            }

                            @Override
                            public void onEndpointLost(@NonNull String s) {
                                Log.e(TAG, "onEndpointLost: ");
                                System.out.println("onEndpointLost: ");
                            }
                        },
                        discoveryOptions.build())
                .addOnSuccessListener(unused -> onDiscoveryStarted())
                .addOnFailureListener(e -> {
                    discovering = false;
                    onDiscoveryFailed();
                });
    }

    protected abstract Strategy getStrategy();

    protected void stopDiscovering() {
        discovering = false;
        connectionsClient.stopDiscovery();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectionsClient = Nearby.getConnectionsClient(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!acceptedPermissions(getContext(), getPermissions())) {
            if (Build.VERSION.SDK_INT < 23) {
                ActivityCompat.requestPermissions(
                        getActivity(), getPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS);
            } else {
                requestPermissions(getPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS);
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            int index = 0;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Log.e(TAG, String.format("onRequestPermissionsResult: %s", permissions[index] ));
                    Toast.makeText(getContext(), "PERMISSIONS MISSING", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                    return;
                }
                index++;
            }

            getActivity().recreate();
            //recreate();
        }

    }

    protected String[] getPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    public static boolean acceptedPermissions(Context context, String... permissions) {
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(context, p)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    protected boolean isDiscovering() {
        return discovering;
    }

    protected abstract void onDiscoveryStarted();

    protected abstract void onDiscoveryFailed();

    protected abstract void onEndpointDiscovered(Endpoint endpoint);

    protected abstract String getServiceId();

    protected abstract void onAcceptRequestFailed();

    // Devices that we can discover and connect to
    protected static class Endpoint {
        private String id;
        private String name;

        public Endpoint(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
