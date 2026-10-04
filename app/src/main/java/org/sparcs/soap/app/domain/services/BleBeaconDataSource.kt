package org.sparcs.soap.app.domain.services

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.sparcs.soap.app.domain.nearby.BeaconSighting
import org.sparcs.soap.app.domain.nearby.BeaconUuid
import org.sparcs.soap.app.domain.nearby.NearbyBeaconSourceProtocol
import org.sparcs.soap.app.domain.nearby.NearbyBluetoothAvailability
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class BleScanFailedException(val errorCode: Int) : Exception("BLE scan failed: $errorCode")

/**
 * Advertises this session's beacon and scans for others with the platform BLE
 * APIs. Legacy, non-connectable advertising only, so iPhones can see it; no
 * device name, TX power or manufacturer data leaves the phone.
 */
class BleBeaconDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : NearbyBeaconSourceProtocol {

    private val adapter: BluetoothAdapter?
        get() = context.getSystemService(BluetoothManager::class.java)?.adapter

    override val requiredPermissions: Array<String> = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE
    )

    private val hasPermissions: Boolean
        get() = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    override val canAdvertise: Boolean
        get() = adapter?.isMultipleAdvertisementSupported == true

    private fun currentAvailability(): NearbyBluetoothAvailability {
        val adapter = adapter
        return when {
            adapter == null || !context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) ->
                NearbyBluetoothAvailability.Unsupported
            !hasPermissions -> NearbyBluetoothAvailability.PermissionRequired
            !adapter.isEnabled -> NearbyBluetoothAvailability.BluetoothOff
            else -> NearbyBluetoothAvailability.Available
        }
    }

    override fun availability(): Flow<NearbyBluetoothAvailability> = callbackFlow {
        trySend(currentAvailability())
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(currentAvailability())
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    // Permissions are checked by `availability()` before collection starts, and
    // a SecurityException from a revoked permission ends the flow with an error.
    @SuppressLint("MissingPermission")
    override fun sightings(token: ByteArray): Flow<BeaconSighting> = callbackFlow {
        val adapter = adapter ?: throw IllegalStateException("Bluetooth unavailable")
        val scanner = adapter.bluetoothLeScanner ?: throw IllegalStateException("Bluetooth is off")

        val advertiser = if (adapter.isMultipleAdvertisementSupported) adapter.bluetoothLeAdvertiser else null
        val advertiseCallback = object : AdvertiseCallback() {
            override fun onStartFailure(errorCode: Int) {
                // Discovery still works one way: we can see others.
                Timber.w("Nearby: advertising failed (%d); scanning only", errorCode)
            }
        }
        advertiser?.startAdvertising(
            AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(false)
                .setTimeout(0)
                .build(),
            AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(UUID.fromString(BeaconUuid.fromToken(token))))
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build(),
            advertiseCallback
        )

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                result.scanRecord?.serviceUuids.orEmpty().forEach { uuid ->
                    BeaconUuid.token(uuid.uuid.toString())?.let { trySend(BeaconSighting(it, result.rssi)) }
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
            }

            override fun onScanFailed(errorCode: Int) {
                close(BleScanFailedException(errorCode))
            }
        }
        // Started once per visit: Android throttles apps that start scans more
        // than five times in 30 s.
        scanner.startScan(
            listOf(
                ScanFilter.Builder()
                    .setServiceUuid(
                        ParcelUuid(UUID.fromString(BeaconUuid.PREFIX_UUID)),
                        ParcelUuid(UUID.fromString(BeaconUuid.MASK_UUID))
                    )
                    .build()
            ),
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build(),
            scanCallback
        )

        awaitClose {
            // The adapter may have been turned off meanwhile; stopping then throws.
            runCatching { scanner.stopScan(scanCallback) }
            runCatching { advertiser?.stopAdvertising(advertiseCallback) }
        }
    }
}
