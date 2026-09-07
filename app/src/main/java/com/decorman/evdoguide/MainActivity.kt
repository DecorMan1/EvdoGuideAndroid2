package com.decorman.evdoguide

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import com.decorman.evdoguide.signal.SignalRepository
import com.decorman.evdoguide.signal.SignalSnapshot

class MainActivity : ComponentActivity() {
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.values.any { it }
            setContent { EvdoGuideApp(hasInitialPermission = granted) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EvdoGuideApp(hasInitialPermission = false) }
    }

    fun requestSignalPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            )
        )
    }
}

@Composable
fun EvdoGuideApp(hasInitialPermission: Boolean) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme {
            SignalHomeScreen(hasInitialPermission = hasInitialPermission)
        }
    }
}

@Composable
private fun SignalHomeScreen(hasInitialPermission: Boolean) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val repository = remember { SignalRepository(context) }
    var snapshot by remember { mutableStateOf(SignalSnapshot.Empty) }

    LaunchedEffect(hasInitialPermission) {
        snapshot = repository.readCurrentSnapshot()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "دليل تقوية CDMA / EVDO",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Text(
            text = "أساس مهني يساعدك على قراءة قوة الإشارة، تجربة نقاط مختلفة داخل المكان، ثم اختيار أفضل موقع للراوتر أو الهاتف بدون وعود غير واقعية.",
            color = Color(0xFF475569),
            fontSize = 15.sp,
            lineHeight = 23.sp
        )

        StatusCard(snapshot)

        Button(
            onClick = {
                activity?.requestSignalPermissions()
                snapshot = repository.readCurrentSnapshot()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("منح الصلاحيات وقراءة الإشارة")
        }

        GuidanceCard()
    }
}

@Composable
private fun StatusCard(snapshot: SignalSnapshot) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("قراءة الإشارة الحالية", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            MetricRow("الشبكة", snapshot.networkTypeLabel)
            MetricRow("قوة الإشارة", snapshot.signalLevelLabel)
            MetricRow("ملاحظة", snapshot.message)
        }
    }
}

@Composable
private fun GuidanceCard() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("أول خطوة تنفيذية", fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
            Text(
                "ابدأ بخريطة قياس داخلية: احفظ قراءة الإشارة مع اسم الموقع داخل البيت أو المحل، ثم اعرض أفضل نقطة حسب dBm/level. بعد ذلك نضيف شاشة إرشاد لوضع EVDO/CDMA مع تنبيه أن التثبيت الحقيقي يحتاج Root أو صلاحيات نظام حسب الجهاز.",
                color = Color(0xFF1E40AF),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF64748B))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A), textAlign = TextAlign.End)
    }
}
