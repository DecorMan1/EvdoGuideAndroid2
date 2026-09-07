package com.decorman.evdoguide.signal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.CellSignalStrengthCdma
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

class SignalRepository(private val context: Context) {
    fun readCurrentSnapshot(): SignalSnapshot {
        if (!hasSignalPermission()) {
            return SignalSnapshot(
                networkTypeLabel = "غير متاح",
                signalLevelLabel = "تحتاج صلاحية",
                message = "امنح صلاحية الموقع/الهاتف حتى يستطيع Android إظهار معلومات الخلية."
            )
        }

        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
        val networkTypeLabel = telephonyManager.dataNetworkType.toNetworkLabel()
        val cdmaSignal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            telephonyManager.signalStrength
                ?.cellSignalStrengths
                ?.filterIsInstance<CellSignalStrengthCdma>()
                ?.firstOrNull()
        } else {
            null
        }

        val level = cdmaSignal?.level
        val dbm = cdmaSignal?.dbm
        val signalLabel = when {
            dbm != null && level != null -> "$dbm dBm / مستوى $level من 4"
            else -> "بانتظار قراءة CDMA"
        }

        return SignalSnapshot(
            networkTypeLabel = networkTypeLabel,
            signalLevelLabel = signalLabel,
            message = "هذه قراءة أولية. المرحلة التالية تحفظ القراءات مع أسماء المواقع لاختيار أقوى نقطة."
        )
    }

    private fun hasSignalPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val phoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
        return fineLocation == PackageManager.PERMISSION_GRANTED ||
            coarseLocation == PackageManager.PERMISSION_GRANTED ||
            phoneState == PackageManager.PERMISSION_GRANTED
    }
}

data class SignalSnapshot(
    val networkTypeLabel: String,
    val signalLevelLabel: String,
    val message: String
) {
    companion object {
        val Empty = SignalSnapshot(
            networkTypeLabel = "لم تبدأ القراءة",
            signalLevelLabel = "اضغط زر الصلاحيات",
            message = "جاهز لبدء فحص الإشارة."
        )
    }
}

private fun Int.toNetworkLabel(): String = when (this) {
    TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO rev.0"
    TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO rev.A"
    TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO rev.B"
    TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
    TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
    TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
    TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
    TelephonyManager.NETWORK_TYPE_UNKNOWN -> "غير معروف"
    else -> "نوع شبكة رقم $this"
}
