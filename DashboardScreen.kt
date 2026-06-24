package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.Screen
import com.example.data.TaxEntry
import com.example.data.InventoryItem
import com.example.ui.theme.*
import com.example.util.Helpers
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.asImageBitmap
import com.example.ui.components.UnifiedPeriodSelector
import androidx.compose.ui.graphics.drawscope.withTransform

// Representation of a transaction line for bento logs
data class UnifiedOperation(
    val title: String,
    val subtitle: String,
    val amount: Double,
    val isExpense: Boolean
)

@Composable
fun WavingYemenFlagPure(phase: Float, modifier: Modifier = Modifier) {
    // Legacy support redirect - completely handles custom rendering inside combined canvas now
}

@Composable
fun WavingPalestineFlagPure(phase: Float, modifier: Modifier = Modifier) {
    // Legacy support redirect - completely handles custom rendering inside combined canvas now
}

@Composable
fun WavingPalestineFlag(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shared_flags")
    
    val phaseYemen by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * java.lang.Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_yemen"
    )
    
    val phasePalestine by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * java.lang.Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_palestine"
    )
    
    val glowProgress by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = modifier.padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // 1. Draw highly realistic glowing background aura
            val centerPoint = androidx.compose.ui.geometry.Offset(width / 2f, height * 0.45f)
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE4312B).copy(alpha = 0.28f * glowProgress), // Waving Red glow
                        Color(0xFF10b981).copy(alpha = 0.20f * glowProgress), // Dynamic Green glow
                        Color(0xFFD4AF37).copy(alpha = 0.18f * glowProgress), // Elegant Gold glow
                        Color.Transparent
                    ),
                    center = centerPoint,
                    radius = width * 0.55f
                ),
                radius = width * 0.55f,
                center = centerPoint
            )
            
            val crossX = width / 2f
            val crossY = height * 0.85f
            
            // Draw Yemen Flag (Left pole tilt, flags fly right)
            withTransform({
                rotate(degrees = 15f, pivot = androidx.compose.ui.geometry.Offset(crossX, crossY))
                scale(scaleX = 0.88f, scaleY = 0.88f, pivot = androidx.compose.ui.geometry.Offset(crossX, crossY))
            }) {
                val poleX = crossX - width * 0.12f
                val poleWidth = (width * 0.05f).coerceIn(3f, 6f)
                val finialRadius = (width * 0.065f).coerceIn(4f, 8f)
                
                // Draw custom 3D metallic flagpole
                drawRect(
                    color = Color(0xFF7F8C8D),
                    topLeft = androidx.compose.ui.geometry.Offset(poleX - poleWidth / 2f, height * 0.05f),
                    size = androidx.compose.ui.geometry.Size(poleWidth / 2f, height * 0.85f)
                )
                drawRect(
                    color = Color(0xFFBDC3C7),
                    topLeft = androidx.compose.ui.geometry.Offset(poleX, height * 0.05f),
                    size = androidx.compose.ui.geometry.Size(poleWidth / 2f, height * 0.85f)
                )
                drawCircle(
                    color = Color(0xFFD4AF37),
                    radius = finialRadius,
                    center = androidx.compose.ui.geometry.Offset(poleX, height * 0.05f)
                )
                
                // Yacht-style Yemen flag cloth
                val flagTop = height * 0.12f
                val flagBottom = height * 0.52f
                val flagHeight = flagBottom - flagTop
                val flagLeft = poleX + poleWidth / 2f + 1f
                val flagRight = width * 0.82f
                val flagWidth = flagRight - flagLeft
                
                val stripeHeight = flagHeight / 3f
                val sliceWidth = 1.0f
                val amplitude = height * 0.075f
                val frequency = 0.065f
                
                for (x in flagLeft.toInt() until flagRight.toInt() step sliceWidth.toInt()) {
                    val relativeX = x.toFloat() - flagLeft
                    val angle = relativeX * frequency - phaseYemen
                    val yOffset = (kotlin.math.sin(angle.toDouble()) * amplitude).toFloat()
                    val damping = (relativeX / flagWidth).coerceIn(0f, 1f)
                    val activeYOffset = yOffset * damping
                    
                    // Yemen Red (Top)
                    drawRect(
                        color = Color(0xFFCE1126),
                        topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), flagTop + activeYOffset),
                        size = androidx.compose.ui.geometry.Size(sliceWidth, stripeHeight)
                    )
                    // Yemen White (Middle)
                    drawRect(
                        color = Color.White,
                        topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), flagTop + stripeHeight + activeYOffset),
                        size = androidx.compose.ui.geometry.Size(sliceWidth, stripeHeight)
                    )
                    // Yemen Black (Bottom)
                    drawRect(
                        color = Color.Black,
                        topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), flagTop + (2f * stripeHeight) + activeYOffset),
                        size = androidx.compose.ui.geometry.Size(sliceWidth, stripeHeight)
                    )
                    
                    // High-quality waving shadow and satin highlight overlay for realistic 3D appearance
                    val shade = kotlin.math.sin(angle.toDouble()).toFloat() * 0.14f * damping
                    if (shade > 0f) {
                        drawRect(
                            color = Color.White.copy(alpha = shade),
                            topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), flagTop + activeYOffset),
                            size = androidx.compose.ui.geometry.Size(sliceWidth, flagHeight)
                        )
                    } else if (shade < 0f) {
                        drawRect(
                            color = Color.Black.copy(alpha = -shade),
                            topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), flagTop + activeYOffset),
                            size = androidx.compose.ui.geometry.Size(sliceWidth, flagHeight)
                        )
                    }
                }
            }
            
            // Draw Palestine Flag (Right pole tilt, flags fly left)
            withTransform({
                rotate(degrees = -15f, pivot = androidx.compose.ui.geometry.Offset(crossX, crossY))
                scale(scaleX = 0.88f, scaleY = 0.88f, pivot = androidx.compose.ui.geometry.Offset(crossX, crossY))
            }) {
                val poleX = crossX + width * 0.12f
                val poleWidth = (width * 0.05f).coerceIn(3f, 6f)
                val finialRadius = (width * 0.065f).coerceIn(4f, 8f)
                
                // Draw custom 3D metallic flagpole
                drawRect(
                    color = Color(0xFF7F8C8D),
                    topLeft = androidx.compose.ui.geometry.Offset(poleX - poleWidth / 2f, height * 0.05f),
                    size = androidx.compose.ui.geometry.Size(poleWidth / 2f, height * 0.85f)
                )
                drawRect(
                    color = Color(0xFFBDC3C7),
                    topLeft = androidx.compose.ui.geometry.Offset(poleX, height * 0.05f),
                    size = androidx.compose.ui.geometry.Size(poleWidth / 2f, height * 0.85f)
                )
                drawCircle(
                    color = Color(0xFFD4AF37),
                    radius = finialRadius,
                    center = androidx.compose.ui.geometry.Offset(poleX, height * 0.05f)
                )
                
                // Yacht-style Palestine flag cloth
                val flagTop = height * 0.12f
                val flagBottom = height * 0.52f
                val flagHeight = flagBottom - flagTop
                val flagRight = poleX - poleWidth / 2f - 1f
                val flagLeft = width * 0.18f
                val flagWidth = flagRight - flagLeft
                
                val stripeHeight = flagHeight / 3f
                val sliceWidth = 1.0f
                val amplitude = height * 0.075f
                val frequency = 0.065f
                
                for (x in flagLeft.toInt() until flagRight.toInt() step sliceWidth.toInt()) {
                    val relativeX = flagRight - x.toFloat()
                    val angle = relativeX * frequency - phasePalestine
                    val yOffset = (kotlin.math.sin(angle.toDouble()) * amplitude).toFloat()
                    val damping = (relativeX / flagWidth).coerceIn(0f, 1f)
                    val activeYOffset = yOffset * damping
                    
                    // Palestine Black (Top)
                    drawRect(
                        color = Color.Black,
                        topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), flagTop + activeYOffset),
                        size = androidx.compose.ui.geometry.Size(sliceWidth, stripeHeight)
                    )
                    // Palestine White (Middle)
                    drawRect(
                        color = Color.White,
                        topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), flagTop + stripeHeight + activeYOffset),
                        size = androidx.compose.ui.geometry.Size(sliceWidth, stripeHeight)
                    )
                    // Palestine Green (Bottom)
                    drawRect(
                        color = Color(0xFF009639),
                        topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), flagTop + (2f * stripeHeight) + activeYOffset),
                        size = androidx.compose.ui.geometry.Size(sliceWidth, stripeHeight)
                    )
                    
                    // Realistic wave highlight shadows
                    val shade = kotlin.math.sin(angle.toDouble()).toFloat() * 0.14f * damping
                    if (shade > 0f) {
                        drawRect(
                            color = Color.White.copy(alpha = shade),
                            topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), flagTop + activeYOffset),
                            size = androidx.compose.ui.geometry.Size(sliceWidth, flagHeight)
                        )
                    } else if (shade < 0f) {
                        drawRect(
                            color = Color.Black.copy(alpha = -shade),
                            topLeft = androidx.compose.ui.geometry.Offset(x.toFloat(), flagTop + activeYOffset),
                            size = androidx.compose.ui.geometry.Size(sliceWidth, flagHeight)
                        )
                    }
                }
                
                // Beautiful elegant Palestinian Red Triangle on the hoist side (pointing left)
                val path = androidx.compose.ui.graphics.Path().apply {
                    val tipX = flagRight - flagWidth * 0.35f
                    val tipRelativeX = flagRight - tipX
                    val tipAngle = tipRelativeX * frequency - phasePalestine
                    val tipYOffset = (kotlin.math.sin(tipAngle.toDouble()) * amplitude).toFloat() * (tipRelativeX / flagWidth).coerceIn(0f, 1f)
                    
                    moveTo(flagRight, flagTop)
                    lineTo(tipX, flagTop + flagHeight / 2f + tipYOffset)
                    lineTo(flagRight, flagBottom)
                    close()
                }
                drawPath(
                    path = path,
                    color = Color(0xFFE4312B) // Palestinian Red
                )
            }
        }
    }
}

@Composable
fun AgencyLogoBadge(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val logoEnabled = com.example.util.BrandingManager.isLogoEnabled
    val customBitmap = remember(logoEnabled) {
        if (logoEnabled) com.example.util.BrandingManager.getCustomLogoBitmap(context) else null
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.2.dp, Color(0xFF8B7340), RoundedCornerShape(8.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap.asImageBitmap(),
                contentDescription = "شعار مخصص",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = com.example.R.drawable.logo_al_aqsa),
                contentDescription = "شعار وكالة طوفان الأقصى",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun DashboardScreen(viewModel: AppViewModel, onMenuClick: (() -> Unit)? = null) {
    var showTaxDialog by remember { mutableStateOf(false) }
    var showSupplierCommitmentDialog by remember { mutableStateOf(false) }
    var selectedDashboardCurrency by remember { mutableStateOf("الريال اليمني") }
    
    // Period selection states for supplier reports dialog
    var supplierCommitmentPeriod by remember { mutableStateOf("يومي") } // "يومي", "أسبوعي", "شهري", "سنوي", "تقويم مخصص"
    var supplierCommitmentCustomType by remember { mutableStateOf("يوم") } // "يوم", "شهر", "سنة"
    var supplierCommitmentCustomValue by remember { mutableStateOf(Helpers.getCurrentDate()) }
    var showSupplierThermalPrintDialog by remember { mutableStateOf(false) }
    
    val brandingAppName by viewModel.brandingAppName.collectAsState()
    val brandingAgencyName by viewModel.brandingAgencyName.collectAsState()
    val brandingOwnerName by viewModel.brandingOwnerName.collectAsState()
    val sarToYerExchangeRate by viewModel.sarToYerExchangeRate.collectAsState()

    val todayTax by viewModel.todayTaxAmount.collectAsState()
    val todayTaxEntry by viewModel.todayTaxEntry.collectAsState()
    val allItems by viewModel.inventoryItems.collectAsState()
    val invoices by viewModel.allInvoices.collectAsState()
    val purchases by viewModel.allPurchases.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val supplies by viewModel.allSuppliers.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    val transfers by viewModel.allTransfers.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchTodayTax()
    }

    val isSarSelected = selectedDashboardCurrency == "الريال السعودي"

    // Calculations & Filtered values by selected currency
    val filteredInvoices = remember(invoices, selectedDashboardCurrency) {
        invoices.filter {
            if (isSarSelected) it.currency.contains("سعودي") else it.currency.contains("يمني")
        }
    }

    val filteredPurchases = remember(purchases, selectedDashboardCurrency) {
        purchases.filter {
            val curr = it.currency ?: "ريال يمني"
            if (isSarSelected) curr.contains("سعودي") else curr.contains("يمني")
        }
    }

    val filteredExpenses = remember(expenses, selectedDashboardCurrency) {
        expenses.filter {
            val curr = it.currency ?: "ريال يمني"
            if (isSarSelected) curr.contains("سعودي") else curr.contains("يمني")
        }
    }

    val filteredStockItems = remember(allItems, selectedDashboardCurrency) {
        allItems.filter {
            val curr = it.buyPriceCurrency.ifEmpty { "ريال يمني" }
            if (isSarSelected) curr.contains("سعودي") else curr.contains("يمني")
        }
    }

    val totalStockQty = filteredStockItems.sumOf { it.quantity }
    val totalStockVal = filteredStockItems.sumOf { it.quantity * it.buyPrice }

    val totalStockValYer = allItems.filter { (it.buyPriceCurrency.ifEmpty { "ريال يمني" }).contains("يمني") }.sumOf { it.quantity * it.buyPrice }
    val totalStockValSar = allItems.filter { (it.buyPriceCurrency.ifEmpty { "ريال يمني" }).contains("سعودي") }.sumOf { it.quantity * it.buyPrice }
    
    val dateToday = Helpers.getCurrentDate()
    val todaySalesTotalYer = invoices.filter { it.date.startsWith(dateToday) }.sumOf {
        if (it.totalAmountYer > 0.0) it.totalAmountYer else if (it.currency.contains("يمني")) it.totalAmount else 0.0
    }
    val todaySalesTotalSar = invoices.filter { it.date.startsWith(dateToday) }.sumOf {
        if (it.totalAmountSar > 0.0) it.totalAmountSar else if (it.currency.contains("سعودي")) it.totalAmount else 0.0
    }
    val todaySalesTotal = if (isSarSelected) todaySalesTotalSar else todaySalesTotalYer
    
    val todayPurchasesTotalYer = purchases.filter { it.date.startsWith(dateToday) }.sumOf {
        if (it.totalAmountYer > 0.0) it.totalAmountYer else if (it.currency.contains("يمني")) it.totalAmount else 0.0
    }
    val todayPurchasesTotalSar = purchases.filter { it.date.startsWith(dateToday) }.sumOf {
        if (it.totalAmountSar > 0.0) it.totalAmountSar else if (it.currency.contains("سعودي")) it.totalAmount else 0.0
    }
    val todayPurchasesTotal = if (isSarSelected) todayPurchasesTotalSar else todayPurchasesTotalYer
    
    val totalDebtsOutstandingYer = remember(invoices, allPayments) {
        val invoicesTotalYer = invoices.sumOf {
            if (it.totalAmountYer > 0.0) it.totalAmountYer else if (it.currency.contains("يمني")) it.totalAmount else 0.0
        }
        val invoicesPaidYer = invoices.sumOf {
            if (it.paidAmountYer > 0.0) it.paidAmountYer else if (it.currency.contains("يمني")) it.paidAmount else 0.0
        }
        val paymentsCollectedYer = allPayments.filter { (it.currency ?: "ريال يمني").contains("يمني") }.sumOf { it.amount }
        (invoicesTotalYer - (invoicesPaidYer + paymentsCollectedYer)).coerceAtLeast(0.0)
    }

    val totalDebtsOutstandingSar = remember(invoices, allPayments) {
        val invoicesTotalSar = invoices.sumOf {
            if (it.totalAmountSar > 0.0) it.totalAmountSar else if (it.currency.contains("سعودي")) it.totalAmount else 0.0
        }
        val invoicesPaidSar = invoices.sumOf {
            if (it.paidAmountSar > 0.0) it.paidAmountSar else if (it.currency.contains("سعودي")) it.paidAmount else 0.0
        }
        val paymentsCollectedSar = allPayments.filter { (it.currency ?: "ريال يمني").contains("سعودي") }.sumOf { it.amount }
        (invoicesTotalSar - (invoicesPaidSar + paymentsCollectedSar)).coerceAtLeast(0.0)
    }

    val totalDebtsOutstanding = remember(totalDebtsOutstandingYer, totalDebtsOutstandingSar, selectedDashboardCurrency) {
        if (isSarSelected) totalDebtsOutstandingSar else totalDebtsOutstandingYer
    }

    val todayExpensesTotal = filteredExpenses.filter { it.date.startsWith(dateToday) }.sumOf { it.amount }

    val todayCollectionsTotalYer = remember(invoices, allPayments) {
        val dateToday = Helpers.getCurrentDate()
        val invoicesPaidYer = invoices.filter { it.date.startsWith(dateToday) }.sumOf {
            if (it.paidAmountYer > 0.0) it.paidAmountYer else if (it.currency.contains("يمني")) it.paidAmount else 0.0
        }
        val paymentsCollectedYer = allPayments.filter { it.date.startsWith(dateToday) && (it.currency ?: "ريال يمني").contains("يمني") }.sumOf { it.amount }
        invoicesPaidYer + paymentsCollectedYer
    }

    val todayCollectionsTotalSar = remember(invoices, allPayments) {
        val dateToday = Helpers.getCurrentDate()
        val invoicesPaidSar = invoices.filter { it.date.startsWith(dateToday) }.sumOf {
            if (it.paidAmountSar > 0.0) it.paidAmountSar else if (it.currency.contains("سعودي")) it.paidAmount else 0.0
        }
        val paymentsCollectedSar = allPayments.filter { it.date.startsWith(dateToday) && (it.currency ?: "ريال يمني").contains("سعودي") }.sumOf { it.amount }
        invoicesPaidSar + paymentsCollectedSar
    }

    val todayCollectionsTotal = remember(todayCollectionsTotalYer, todayCollectionsTotalSar, selectedDashboardCurrency) {
        if (isSarSelected) todayCollectionsTotalSar else todayCollectionsTotalYer
    }

    // Today outflows & tax apply to Yemeni Rial only
    val todayTaxValYer = (todayTaxEntry?.taxAmount ?: 0.0)
    val todayStallOutflowYer = (todayTaxEntry?.stallOutflow ?: 0.0)
    val todayLaborOutflowYer = (todayTaxEntry?.laborOutflow ?: 0.0)

    val todayTaxVal = if (isSarSelected) 0.0 else todayTaxValYer
    val todayStallOutflow = if (isSarSelected) 0.0 else todayStallOutflowYer
    val todayLaborOutflow = if (isSarSelected) 0.0 else todayLaborOutflowYer

    val todayExpensesTotalYer = expenses.filter { it.date.startsWith(dateToday) && (it.currency ?: "ريال يمني").contains("يمني") }.sumOf { it.amount }
    val todayExpensesTotalSar = expenses.filter { it.date.startsWith(dateToday) && (it.currency ?: "ريال يمني").contains("سعودي") }.sumOf { it.amount }

    val todayProfitEstYer = todaySalesTotalYer - todayPurchasesTotalYer - todayTaxValYer - todayStallOutflowYer - todayLaborOutflowYer - todayExpensesTotalYer
    val todayProfitEstSar = todaySalesTotalSar - todayPurchasesTotalSar - todayExpensesTotalSar

    val todayProfitEst = if (isSarSelected) todayProfitEstSar else todayProfitEstYer

    val currencySuffix = when (selectedDashboardCurrency) {
        "الريال السعودي" -> "ر.س"
        "الريال اليمني" -> "ر.ي"
        else -> "ريال"
    }

    // Fetch live recent operations directly from tables
    val recentOperations = remember(invoices, expenses, selectedDashboardCurrency) {
        val ops = mutableListOf<UnifiedOperation>()
        for (inv in invoices) {
            val matchesFilter = when (selectedDashboardCurrency) {
                "الريال اليمني" -> inv.currency.contains("يمني")
                "الريال السعودي" -> inv.currency.contains("سعودي")
                else -> true
            }
            if (matchesFilter) {
                ops.add(
                    UnifiedOperation(
                        title = "مبيع: " + (inv.customerName.ifEmpty { "عميل نقدي" }),
                        subtitle = "فاتورة #${inv.id} • ${inv.date.split(" ").firstOrNull() ?: ""} • ${inv.currency}",
                        amount = inv.totalAmount,
                        isExpense = false
                    )
                )
            }
        }
        if (selectedDashboardCurrency != "الريال السعودي") {
            for (exp in expenses) {
                ops.add(
                    UnifiedOperation(
                        title = "مصروف: " + exp.category,
                        subtitle = "${exp.notes.ifEmpty { "منصرف عام" }} • ${exp.date.split(" ").firstOrNull() ?: ""}",
                        amount = exp.amount,
                        isExpense = true
                    )
                )
            }
        }
        ops.take(4)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PalBlackNormal)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (onMenuClick != null) {
                            IconButton(onClick = onMenuClick) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "جرد وسجلات الجانبية",
                                    tint = Color.White
                                )
                            }
                        }
                        AgencyLogoBadge()
                        Column {
                            Text(
                                text = brandingAppName.ifEmpty { "وكالة طوفان الأقصى" },
                                color = PalWhitePure,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "${brandingOwnerName.ifEmpty { "أحمد منصور" }} • نظام ${brandingAppName.ifEmpty { "طوفان الأقصى" }} المحاسبي",
                                color = PalWhiteMuted,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                    
                    // Top Bar utility connectivity indicator
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PalBlackLight, RoundedCornerShape(10.dp))
                            .border(1.dp, PalWhiteMuted.copy(0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "متصل بالشبكة المحلية",
                            tint = Color(0xFF10b981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = PalWhiteMuted.copy(0.2f), thickness = 1.dp)
            }
        },
        bottomBar = {
            BottomNavigationBar(viewModel)
        },
        containerColor = PalBlackDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            
            // Palestinian Pride Flag Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF08180E)),
                border = BorderStroke(1.2.dp, Color(0xFF1B6A31)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = brandingAppName.ifEmpty { "وكالة طوفان الأقصى 🇵🇸" },
                            color = PalGoldCalligraphy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (brandingAgencyName.isEmpty()) "لأجود أنواع القات بجميع أنواعه لصاحبها أحمد منصور" else "${brandingAgencyName} بجميع أنواعه لصاحبها ${brandingOwnerName}",
                            color = PalWhiteSoft,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(PalRedLight, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "فلسطين حرة 🇵🇸",
                                color = PalRedLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    WavingPalestineFlag(
                        modifier = Modifier
                            .size(width = 115.dp, height = 70.dp)
                    )
                }
            }

            // Currency Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("الريال اليمني", "الريال السعودي").forEach { currencyOption ->
                    val isSelected = selectedDashboardCurrency == currencyOption
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PalGreenNormal else PalBlackNormal)
                            .border(1.dp, if (isSelected) PalGreenLight else Color(0xFF222222), RoundedCornerShape(12.dp))
                            .clickable { selectedDashboardCurrency = currencyOption }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currencyOption,
                            color = if (isSelected) PalWhitePure else PalWhiteMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isSarSelected) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.2.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(PalGoldCalligraphy.copy(alpha = 0.6f), PalGreenLight.copy(alpha = 0.4f))))
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color(0xFF0F172A), Color(0xFF051D12))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PalGoldCalligraphy.copy(0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = PalGoldCalligraphy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "تسعير الصرف اليومي للمقاصة",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "معدل التحويل للريال السعودي مقابل اليمني",
                                        color = PalWhiteMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PalGoldCalligraphy.copy(0.15f))
                                    .border(1.dp, PalGoldCalligraphy.copy(0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "1 ر.س = ${sarToYerExchangeRate} ريال يمني",
                                    color = PalGoldCalligraphy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(18.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var textState by remember(sarToYerExchangeRate) { 
                                mutableStateOf(if (sarToYerExchangeRate % 1.0 == 0.0) sarToYerExchangeRate.toInt().toString() else sarToYerExchangeRate.toString()) 
                            }
                            
                            OutlinedTextField(
                                value = textState,
                                onValueChange = { clean ->
                                    val filtered = clean.filter { it.isDigit() || it == '.' }
                                    textState = filtered
                                    val parsed = filtered.toDoubleOrNull()
                                    if (parsed != null && parsed > 0.0) {
                                        viewModel.updateExchangeRate(parsed)
                                    }
                                },
                                label = { Text("قيمة الصرف الحالية", color = PalWhiteMuted, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PalGoldCalligraphy,
                                    unfocusedBorderColor = Color(0xFF1E293B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.LightGray,
                                    focusedContainerColor = Color.Black.copy(0.40f),
                                    unfocusedContainerColor = Color.Black.copy(0.20f)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1.3f)
                            )
                            
                            Row(
                                modifier = Modifier.weight(2f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(-5, -1, 1, 5).forEach { adjustValue ->
                                    val label = if (adjustValue > 0) "+$adjustValue" else adjustValue.toString()
                                    val isPositive = adjustValue > 0
                                    val containerColor = if (isPositive) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                                    val contentColor = if (isPositive) Color(0xFF34D399) else Color(0xFFFCA5A5)
                                    val borderColor = if (isPositive) Color(0xFF047857) else Color(0xFF991B1B)
                                    
                                    Button(
                                        onClick = {
                                            val newVal = (sarToYerExchangeRate + adjustValue).coerceAtLeast(1.0)
                                            viewModel.updateExchangeRate(newVal)
                                            textState = if (newVal % 1.0 == 0.0) newVal.toInt().toString() else newVal.toString()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = containerColor,
                                            contentColor = contentColor
                                        ),
                                        border = BorderStroke(1.dp, borderColor),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Text(
                                            text = label, 
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Bento Tax & Outflows Status Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Tax
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (todayTaxVal > 0) Color(0xFF052e16).copy(0.35f) else Color(0xFF2d0606).copy(0.35f)
                    ),
                    border = BorderStroke(1.dp, if (todayTaxVal > 0) Color(0xFF10b981).copy(0.40f) else Color(0xFFef4444).copy(0.40f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showTaxDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (todayTaxVal > 0) Color(0xFF10b981).copy(0.12f) else Color(0xFFef4444).copy(0.12f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (todayTaxVal > 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (todayTaxVal > 0) Color(0xFF10b981) else Color(0xFFef4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ضريبة القات اليومية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(
                                text = if (todayTaxVal > 0) Helpers.formatMoney(todayTaxVal) else "لم تسجل بعد",
                                color = if (todayTaxVal > 0) Color(0xFF10b981) else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Card 2: Stall & Labor Outflows
                val isOutflowEntered = todayStallOutflow > 0 || todayLaborOutflow > 0
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOutflowEntered) Color(0xFF052e16).copy(0.35f) else Color(0xFF2d0606).copy(0.35f)
                    ),
                    border = BorderStroke(1.dp, if (isOutflowEntered) Color(0xFF10b981).copy(0.40f) else Color(0xFFef4444).copy(0.40f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showTaxDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isOutflowEntered) Color(0xFF10b981).copy(0.12f) else Color(0xFFef4444).copy(0.12f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOutflowEntered) Icons.Default.Payments else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isOutflowEntered) Color(0xFF10b981) else Color(0xFFef4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("مفرش وعمالة اليوم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(
                                text = if (isOutflowEntered) Helpers.formatMoney(todayStallOutflow + todayLaborOutflow) else "لم تسجل بعد",
                                color = if (isOutflowEntered) Color(0xFF10b981) else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Bento Stats Grid System
            // Today Sales Wide Bento Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                border = BorderStroke(1.dp, Color(0xFF10b981).copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { viewModel.navigateTo(Screen.Sales) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF063e23).copy(alpha = 0.35f),
                                    PalBlackNormal
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Text(
                            text = "إجمالي مبيعات اليوم",
                            color = Color(0xFF10b981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Column {
                                if (isSarSelected) {
                                    Text("🇸🇦 بالريال السعودي", color = PalWhiteMuted, fontSize = 11.sp)
                                    Text(
                                        text = Helpers.formatWithCurrency(todaySalesTotalSar, "ريال سعودي"),
                                        color = PalWhitePure,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "⚡ يعادل باليمني: " + Helpers.formatWithCurrency(todaySalesTotalSar * sarToYerExchangeRate, "ريال يمني"),
                                        color = PalGoldCalligraphy,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text("🇾🇪 بالريال اليمني", color = PalWhiteMuted, fontSize = 11.sp)
                                    Text(
                                        text = Helpers.formatWithCurrency(todaySalesTotalYer, "ريال يمني"),
                                        color = PalWhitePure,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF10b981).copy(0.12f), CircleShape)
                            .align(Alignment.CenterEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF10b981),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Three Bento Cards side-by-side: Net Profit, Collections & Outstandings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Net Profit Card
                Card(
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                     border = BorderStroke(1.dp, Color(0xFF222222)),
                     modifier = Modifier
                         .weight(1f)
                         .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("صافي الربح", color = PalWhiteMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (isSarSelected) {
                                Text(
                                    text = "🇸🇦 " + Helpers.formatWithCurrency(todayProfitEstSar, "ريال سعودي"),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "≈ " + Helpers.formatMoney(todayProfitEstSar * sarToYerExchangeRate) + " يمني",
                                    color = PalGoldCalligraphy,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "🇾🇪 " + Helpers.formatWithCurrency(todayProfitEstYer, "ريال يمني"),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Collections Card
                Card(
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                     border = BorderStroke(1.dp, Color(0xFF10b981).copy(0.15f)),
                     modifier = Modifier
                         .weight(1.1f)
                         .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تحصيلات اليوم", color = Color(0xFF10b981), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (isSarSelected) {
                                Text(
                                    text = "🇸🇦 " + Helpers.formatWithCurrency(todayCollectionsTotalSar, "ريال سعودي"),
                                    color = Color(0xFF10b981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "≈ " + Helpers.formatMoney(todayCollectionsTotalSar * sarToYerExchangeRate) + " يمني",
                                    color = PalGoldCalligraphy,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "🇾🇪 " + Helpers.formatWithCurrency(todayCollectionsTotalYer, "ريال يمني"),
                                    color = Color(0xFF10b981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Debts Card
                Card(
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                     border = BorderStroke(1.dp, Color(0xFF222222)),
                     modifier = Modifier
                         .weight(1f)
                         .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي الديون المعلقة", color = PalWhiteMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (isSarSelected) {
                                Text(
                                    text = "🇸🇦 " + Helpers.formatWithCurrency(totalDebtsOutstandingSar, "ريال سعودي"),
                                    color = Color(0xFFef4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "≈ " + Helpers.formatMoney(totalDebtsOutstandingSar * sarToYerExchangeRate) + " يمني",
                                    color = PalGoldCalligraphy,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "🇾🇪 " + Helpers.formatWithCurrency(totalDebtsOutstandingYer, "ريال يمني"),
                                    color = Color(0xFFef4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Bento Stock Status Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                border = BorderStroke(1.dp, Color(0xFF222222)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { viewModel.navigateTo(Screen.Inventory) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val lowStockItemsCount = allItems.count { it.quantity < 10 }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("حالة المخزون (صعدي ممتاز)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        
                        if (lowStockItemsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFFef4444).copy(0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "كمية منخفضة لـ $lowStockItemsCount أصناف",
                                    color = Color(0xFFef4444),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFF10b981).copy(0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "المخزون ممتاز ومستقر",
                                    color = Color(0xFF10b981),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Progress Bar
                    Spacer(modifier = Modifier.height(10.dp))
                    // Progress Bar
                    val totalInventoryItems = allItems.size
                    val percentage = if (totalInventoryItems > 0) {
                        (totalInventoryItems - lowStockItemsCount).toFloat() / totalInventoryItems.toFloat()
                    } else {
                        1.0f
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF222222))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percentage)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (lowStockItemsCount > 0) Color(0xFFef4444) else Color(0xFF10b981))
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // REDESIGNED EXECUTIVE DASHBOARD MANAGEMENT PANELS (REPLACING BUTTONS)
            // ----------------------------------------------------
            Text(
                text = "المؤشرات والأداء العمالي والشامل",
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            // Dynamic grid layout of the 6 core business pillars
            Column(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Stock Value & Customers Receivable Debt
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val stockItemQtyYer = allItems.filter { (it.buyPriceCurrency.ifEmpty { "ريال يمني" }).contains("يمني") }.sumOf { it.quantity }
                    val stockItemQtySar = allItems.filter { (it.buyPriceCurrency.ifEmpty { "ريال يمني" }).contains("سعودي") }.sumOf { it.quantity }
                    DashboardKpiCard(
                        title = "قيمة أصول مخزون القات",
                        value = if (isSarSelected) {
                            "🇸🇦 " + Helpers.formatWithCurrency(totalStockValSar, "ريال سعودي")
                        } else {
                            "🇾🇪 " + Helpers.formatWithCurrency(totalStockValYer, "ريال يمني")
                        },
                        subtitle = if (isSarSelected) {
                            "تعادل: " + Helpers.formatMoney(totalStockValSar * sarToYerExchangeRate) + " يمني • المخزون: ${stockItemQtySar} حبة"
                        } else {
                            "المخزون المتوفر: ${stockItemQtyYer} حبة"
                        },
                        icon = Icons.Default.Layers,
                        accentColor = Color(0xFF3B82F6),
                        onClick = { viewModel.navigateTo(Screen.Inventory) },
                        modifier = Modifier.weight(1f)
                    )

                    val debtorCountYer = invoices.filter {
                        (if (it.debtAmountYer > 0.0) it.debtAmountYer else if (it.currency.contains("يمني")) it.debtAmount else 0.0) > 0.0
                    }.map { it.customerName }.distinct().size
                    val debtorCountSar = invoices.filter {
                        (if (it.debtAmountSar > 0.0) it.debtAmountSar else if (it.currency.contains("سعودي")) it.debtAmount else 0.0) > 0.0
                    }.map { it.customerName }.distinct().size

                    DashboardKpiCard(
                        title = "ديون المستحقات للعملاء",
                        value = if (isSarSelected) {
                            "🇸🇦 " + Helpers.formatWithCurrency(totalDebtsOutstandingSar, "ريال سعودي")
                        } else {
                            "🇾🇪 " + Helpers.formatWithCurrency(totalDebtsOutstandingYer, "ريال يمني")
                        },
                        subtitle = if (isSarSelected) {
                            "تعادل: " + Helpers.formatMoney(totalDebtsOutstandingSar * sarToYerExchangeRate) + " يمني • لـ $debtorCountSar عملاء"
                        } else {
                            "المستحق لـ $debtorCountYer عملاء"
                        },
                        icon = Icons.Default.People,
                        accentColor = Color(0xFFF97316),
                        onClick = { viewModel.navigateTo(Screen.Customers) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Supplier Balance & Operating Profit margin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val totalSupplierDebtYer = purchases.sumOf {
                        val d = if (it.totalAmountYer > 0.0 || it.totalAmountSar > 0.0) it.debtRemainingYer else if (it.currency.contains("يمني")) it.debtRemaining else 0.0
                        if (d > 0.0) d else 0.0
                    }
                    val totalSupplierDebtSar = purchases.sumOf {
                        val d = if (it.totalAmountYer > 0.0 || it.totalAmountSar > 0.0) it.debtRemainingSar else if (it.currency.contains("سعودي")) it.debtRemaining else 0.0
                        if (d > 0.0) d else 0.0
                    }
                    val totalSupplierSurplusYer = purchases.sumOf {
                        val d = if (it.totalAmountYer > 0.0 || it.totalAmountSar > 0.0) it.debtRemainingYer else if (it.currency.contains("يمني")) it.debtRemaining else 0.0
                        if (d < 0.0) -d else 0.0
                    }
                    val totalSupplierSurplusSar = purchases.sumOf {
                        val d = if (it.totalAmountYer > 0.0 || it.totalAmountSar > 0.0) it.debtRemainingSar else if (it.currency.contains("سعودي")) it.debtRemaining else 0.0
                        if (d < 0.0) -d else 0.0
                    }

                    DashboardKpiCard(
                        title = "التزامات الموردين الكلية",
                        value = if (isSarSelected) {
                            "🇸🇦 " + Helpers.formatWithCurrency(totalSupplierDebtSar, "ريال سعودي")
                        } else {
                            "🇾🇪 " + Helpers.formatWithCurrency(totalSupplierDebtYer, "ريال يمني")
                        },
                        subtitle = if (isSarSelected) {
                            "تعادل: " + Helpers.formatMoney(totalSupplierDebtSar * sarToYerExchangeRate) + " يمني"
                        } else {
                            "المستحق: ${Helpers.formatMoney(totalSupplierDebtYer)} (والفائض لنا: ${Helpers.formatMoney(totalSupplierSurplusYer)})"
                        },
                        icon = Icons.Default.LocalShipping,
                        accentColor = Color(0xFF8B5CF6),
                        onClick = { showSupplierCommitmentDialog = true },
                        modifier = Modifier.weight(1f)
                    )

                    val opMarginYer = if (todaySalesTotalYer > 0) (todayProfitEstYer / todaySalesTotalYer) * 100.0 else 0.0
                    val opMarginSar = if (todaySalesTotalSar > 0) (todayProfitEstSar / todaySalesTotalSar) * 100.0 else 0.0
                    val activeOpMargin = if (isSarSelected) opMarginSar else opMarginYer
                    val performanceLabel = if (isSarSelected) {
                        when {
                            todayProfitEstSar > 5000 -> "أداء مالي متميز 🚀"
                            todayProfitEstSar > 1000 -> "أداء مستقر ومتزن ✅"
                            else -> "تحت الملاحظة الشاملة ⚠️"
                        }
                    } else {
                        when {
                            todayProfitEstYer > 25000 -> "أداء مالي متميز 🚀"
                            todayProfitEstYer > 5000 -> "أداء مستقر ومتزن ✅"
                            else -> "تحت الملاحظة الشاملة ⚠️"
                        }
                    }

                    DashboardKpiCard(
                        title = "الهامش التشغيلي الكلي",
                        value = "${String.format(Locale.US, "%.1f", activeOpMargin)}%",
                        subtitle = performanceLabel,
                        icon = Icons.Default.BarChart,
                        accentColor = Color(0xFF10B981),
                        onClick = { viewModel.navigateTo(Screen.Accounts) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Live Daily Money Transfers & Operational Expenses
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val totalSentYer = transfers.filter { it.date.startsWith(dateToday) && (it.currency ?: "ريال يمني").contains("يمني") }.sumOf { it.amount }
                    val totalSentSar = transfers.filter { it.date.startsWith(dateToday) && (it.currency ?: "ريال يمني").contains("سعودي") }.sumOf { it.amount }
                    val activeSentVal = if (isSarSelected) totalSentSar else totalSentYer
                    val activeCurrencyKey = if (isSarSelected) "ريال سعودي" else "ريال يمني"
                    val transfersCount = transfers.filter {
                        it.date.startsWith(dateToday) && 
                        (it.currency ?: "ريال يمني").contains(if (isSarSelected) "سعودي" else "يمني")
                    }.size

                    DashboardKpiCard(
                        title = "عموم إرساليات اليوم",
                        value = Helpers.formatWithCurrency(activeSentVal, activeCurrencyKey),
                        subtitle = if (isSarSelected) {
                            "تعادل: " + Helpers.formatMoney(totalSentSar * sarToYerExchangeRate) + " يمني • مجموع: ${transfersCount} حركة"
                        } else {
                            "مجموع: ${transfersCount} حركة مالية متبادلة"
                        },
                        icon = Icons.Default.CompareArrows,
                        accentColor = Color(0xFF00ADFF),
                        onClick = { viewModel.navigateTo(Screen.Transfers) },
                        modifier = Modifier.weight(1f)
                    )

                    DashboardKpiCard(
                        title = "مصروفات التشغيل المخرجة",
                        value = if (isSarSelected) {
                            "🇸🇦 " + Helpers.formatWithCurrency(todayExpensesTotalSar, "ريال سعودي")
                        } else {
                            "🇾🇪 " + Helpers.formatWithCurrency(todayExpensesTotalYer, "ريال يمني")
                        },
                        subtitle = if (isSarSelected) {
                            "تعادل: " + Helpers.formatMoney(todayExpensesTotalSar * sarToYerExchangeRate) + " يمني"
                        } else {
                            "شاملة النثريات اليومية للوكالة"
                        },
                        icon = Icons.Default.Payments,
                        accentColor = Color(0xFFF43F5E),
                        onClick = { viewModel.navigateTo(Screen.Expenses) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Canvas analytical graphics Styled Card
            Text(
                text = "الأداء المالي البياني",
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                border = BorderStroke(1.dp, Color(0xFF222222)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مقارنة مبيعات ومصروفات الأسبوع", color = PalWhitePure, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF10b981)).clip(RoundedCornerShape(2.dp)))
                            Text(" مبيعات", color = PalWhiteMuted, fontSize = 9.sp, modifier = Modifier.padding(start = 4.dp, end = 12.dp))
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFFef4444)).clip(RoundedCornerShape(2.dp)))
                            Text(" مصروفات", color = PalWhiteMuted, fontSize = 9.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Simulated simple graph with Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Draw Grid lines
                        val numPoints = 7
                        val barGap = canvasWidth / (numPoints + 1)
                        val maxSales = 120000.0
                        val rawSales = listOf(30000f, 65000f, 40000f, 85000f, 95000f, 110000f, todaySalesTotal.toFloat())
                        val rawExp = listOf(12000f, 19000f, 8000f, 32000f, 15000f, 22000f, todayExpensesTotal.toFloat())

                        for (i in 0 until numPoints) {
                            val xPos = barGap * (i + 1)
                            
                            // Scale values
                            val salesHeight = (rawSales[i] / maxSales) * canvasHeight
                            val expHeight = (rawExp[i] / maxSales) * canvasHeight

                            // Draw Sales Bar (Green)
                            drawRect(
                                color = Color(0xFF10b981),
                                topLeft = androidx.compose.ui.geometry.Offset(xPos - 8.dp.toPx(), (canvasHeight - salesHeight).toFloat()),
                                size = androidx.compose.ui.geometry.Size(6.dp.toPx(), salesHeight.toFloat())
                            )

                            // Draw Expenses Bar (Red)
                            drawRect(
                                color = Color(0xFFef4444),
                                topLeft = androidx.compose.ui.geometry.Offset(xPos + 1.dp.toPx(), (canvasHeight - expHeight).toFloat()),
                                size = androidx.compose.ui.geometry.Size(6.dp.toPx(), expHeight.toFloat())
                            )
                        }
                    }
                }
            }

            // Real Live Recent Operations List Card
            if (recentOperations.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                    border = BorderStroke(1.dp, Color(0xFF222222)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("آخر العمليات", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "عرض الكل",
                                color = Color(0xFF10b981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { viewModel.navigateTo(Screen.Archive) }
                                    .padding(4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            recentOperations.forEachIndexed { index, op ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (op.isExpense) Color(0xFFef4444).copy(0.12f) else Color(0xFF10b981).copy(0.12f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (op.isExpense) Icons.Default.Remove else Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (op.isExpense) Color(0xFFef4444) else Color(0xFF10b981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(op.title, color = PalWhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(op.subtitle, color = PalWhiteMuted, fontSize = 10.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = (if (op.isExpense) "-" else "+") + Helpers.formatMoney(op.amount) + " $currencySuffix",
                                            color = if (op.isExpense) Color(0xFFef4444) else PalWhitePure,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isSarSelected) {
                                            Text(
                                                text = "≈ " + Helpers.formatMoney(op.amount * sarToYerExchangeRate) + " يمني",
                                                color = PalGoldCalligraphy,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                if (index < recentOperations.size - 1) {
                                    Divider(color = Color(0xFF222222), thickness = 1.dp, modifier = Modifier.padding(top = 10.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // --- Daily Tax Input Dialog ---
    if (showTaxDialog) {
        val currentEntry = todayTaxEntry ?: TaxEntry(date = Helpers.getCurrentDate(), taxAmount = 0.0)
        var taxInput by remember { mutableStateOf(if (currentEntry.taxAmount > 0) currentEntry.taxAmount.toInt().toString() else "") }
        var stallInput by remember { mutableStateOf(if (currentEntry.stallOutflow > 0) currentEntry.stallOutflow.toInt().toString() else "") }
        var laborInput by remember { mutableStateOf(if (currentEntry.laborOutflow > 0) currentEntry.laborOutflow.toInt().toString() else "") }
        var notesInput by remember { mutableStateOf(currentEntry.notes) }

        AlertDialog(
            onDismissRequest = { showTaxDialog = false },
            title = {
                Text(
                    text = "الرسوم والمصروفات اليومية الثابتة",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "يتم تسجيل الضريبة والمصروفات الثابتة مرة واحدة يومياً وتنعكس تلقائياً في التقارير الحسابية والمالية والمطبوعات.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = taxInput,
                        onValueChange = { taxInput = it },
                        label = { Text("ضريبة القات اليومية (ريال)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10b981),
                            unfocusedBorderColor = Color(0xFF222222),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = stallInput,
                        onValueChange = { stallInput = it },
                        label = { Text("خرج المفرش اليومي (ريال)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10b981),
                            unfocusedBorderColor = Color(0xFF222222),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = laborInput,
                        onValueChange = { laborInput = it },
                        label = { Text("خرج العمال اليومي (ريال)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10b981),
                            unfocusedBorderColor = Color(0xFF222222),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("شرح / ملاحظات المصروفات", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10b981),
                            unfocusedBorderColor = Color(0xFF222222),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val tax = taxInput.toDoubleOrNull() ?: 0.0
                        val stall = stallInput.toDoubleOrNull() ?: 0.0
                        val labor = laborInput.toDoubleOrNull() ?: 0.0
                        viewModel.saveTodayTaxAndOutflows(tax, stall, labor, notesInput)
                        showTaxDialog = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10b981))
                ) {
                    Text("حفظ البيانات اليومية", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTaxDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF111111)
        )
    }

    // --- Supplier Commitment Statistics Dialog ---
    if (showSupplierCommitmentDialog) {
        val dateToday = Helpers.getCurrentDate()
        val curMonthStr = dateToday.substring(0, 7)
        val curYearStr = dateToday.substring(0, 4)

        // Helper weekly check
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayTime = try { sdf.parse(dateToday)?.time ?: 0L } catch(e: Exception) { 0L }
        val checkIsWeekly: (String) -> Boolean = { dateStr ->
            val itemDate = dateStr.take(10)
            try {
                val itemTime = sdf.parse(itemDate)?.time ?: 0L
                val diff = todayTime - itemTime
                diff in 0..(7L * 24 * 60 * 60 * 1000)
            } catch (e: Exception) {
                false
            }
        }

        // 1. Filtered purchases based on Supplier Commitment Period selection (daily, weekly, monthly, yearly, custom calendar)
        val filteredCommitmentPurchases = when (supplierCommitmentPeriod) {
            "يومي" -> purchases.filter { it.date.startsWith(dateToday) }
            "أسبوعي" -> purchases.filter { checkIsWeekly(it.date) }
            "شهري" -> purchases.filter { it.date.startsWith(curMonthStr) }
            "سنوي" -> purchases.filter { it.date.startsWith(curYearStr) }
            "تقويم مخصص" -> purchases.filter { it.date.startsWith(supplierCommitmentCustomValue) }
            else -> purchases
        }
        
        // 2. Calculations for Today's/Period commitments
        val purchasesYerToday = filteredCommitmentPurchases.sumOf { if (it.totalAmountYer > 0.0) it.totalAmountYer else if (it.currency.contains("يمني")) it.totalAmount else 0.0 }
        val purchasesSarToday = filteredCommitmentPurchases.sumOf { if (it.totalAmountSar > 0.0) it.totalAmountSar else if (it.currency.contains("سعودي")) it.totalAmount else 0.0 }
        val paidYerToday = filteredCommitmentPurchases.sumOf { if (it.totalAmountYer > 0.0) it.paidAmountYer else if (it.currency.contains("يمني")) it.paidAmount else 0.0 }
        val paidSarToday = filteredCommitmentPurchases.sumOf { if (it.totalAmountSar > 0.0) it.paidAmountSar else if (it.currency.contains("سعودي")) it.paidAmount else 0.0 }

        // 3. Calculations for cumulative outstanding debts and overpayments restricted to the selected period's transactions
        val totalDebitYer = filteredCommitmentPurchases.sumOf { 
            val v = if (it.totalAmountYer > 0.0) it.debtRemainingYer else if (it.currency.contains("يمني")) it.debtRemaining else 0.0
            if (v > 0.0) v else 0.0
        }
        val totalDebitSar = filteredCommitmentPurchases.sumOf { 
            val v = if (it.totalAmountSar > 0.0) it.debtRemainingSar else if (it.currency.contains("سعودي")) it.debtRemaining else 0.0
            if (v > 0.0) v else 0.0
        }

        val totalCreditYer = filteredCommitmentPurchases.sumOf { 
            val v = if (it.totalAmountYer > 0.0) it.debtRemainingYer else if (it.currency.contains("يمني")) it.debtRemaining else 0.0
            if (v < 0.0) -v else 0.0
        }
        val totalCreditSar = filteredCommitmentPurchases.sumOf { 
            val v = if (it.totalAmountSar > 0.0) it.debtRemainingSar else if (it.currency.contains("سعودي")) it.debtRemaining else 0.0
            if (v < 0.0) -v else 0.0
        }

        // 4. Map for each supplier and their net balance
        val supplierBalances = supplies.map { sup ->
            val supPurchases = filteredCommitmentPurchases.filter { it.supplierId == sup.id }
            val netYer = supPurchases.sumOf { if (it.totalAmountYer > 0.0) it.debtRemainingYer else if (it.currency.contains("يمني")) it.debtRemaining else 0.0 }
            val netSar = supPurchases.sumOf { if (it.totalAmountSar > 0.0) it.debtRemainingSar else if (it.currency.contains("سعودي")) it.debtRemaining else 0.0 }
            Triple(sup, netYer, netSar)
        }

        val reportMsg = buildString {
            val bAppName = brandingAppName.ifEmpty { "وكالة طوفان الأقصى" }
            val bAgencyName = brandingAgencyName.ifEmpty { "لأجود أنواع القات" }
            val periodLabelText = when (supplierCommitmentPeriod) {
                "يومي" -> "اليومي ($dateToday)"
                "أسبوعي" -> "الأسبوعي (7 أيام الماضية)"
                "شهري" -> "الشهري ($curMonthStr)"
                "سنوي" -> "السنوي ($curYearStr)"
                else -> "المخصص لـ ($supplierCommitmentCustomValue)"
            }
            appendLine("📈 تقرير التزامات وأرصدة الموردين $periodLabelText 📈")
            appendLine("⚖️ $bAppName - $bAgencyName ⚖️")
            appendLine("التاريخ والوقت: ${Helpers.getCurrentDateTime()}")
            appendLine("---------------------------------------")
            appendLine("🔺 إحصائيات حركة التوريد للفترة المحددة:")
            appendLine("  • توريد بالريال اليمني: ${Helpers.formatWithCurrency(purchasesYerToday, "ريال يمني")}")
            appendLine("  • مسدد باليمني: ${Helpers.formatWithCurrency(paidYerToday, "ريال يمني")}")
            appendLine("  • توريد بالريال السعودي: ${Helpers.formatWithCurrency(purchasesSarToday, "ريال سعودي")}")
            appendLine("  • مسدد بالسعودي: ${Helpers.formatWithCurrency(paidSarToday, "ريال سعودي")}")
            appendLine("---------------------------------------")
            appendLine("💳 الالتزامات والأرصدة القائمة للفترة:")
            appendLine("  • ديون الموردين متبقية (يمني): ${Helpers.formatWithCurrency(totalDebitYer, "ريال يمني")}")
            appendLine("  • أرصدة زائدة لنا عند الموردين (يمني): ${Helpers.formatWithCurrency(totalCreditYer, "ريال يمني")}")
            appendLine("  • ديون الموردين متبقية (سعودي): ${Helpers.formatWithCurrency(totalDebitSar, "ريال سعودي")}")
            appendLine("  • أرصدة زائدة لنا عند الموردين (سعودي): ${Helpers.formatWithCurrency(totalCreditSar, "ريال سعودي")}")
            appendLine("---------------------------------------")
            appendLine("🤝 أرصدة الحسابات والتزامات كل مورد على حدة:")
            supplierBalances.forEach { (sup, netYer, netSar) ->
                val regionSuffix = if (sup.region.isNotEmpty()) " (${sup.region})" else ""
                appendLine("👤 ${sup.name}$regionSuffix:")
                if (netYer > 0.0) {
                    appendLine("  • العملة اليمني: مدين له بقيمة ${Helpers.formatWithCurrency(netYer, "ريال يمني")} ⏳")
                } else if (netYer < 0.0) {
                    appendLine("  • العملة اليمني: رصيد فائض زائد لنا قيمة ${Helpers.formatWithCurrency(-netYer, "ريال يمني")} 🟢")
                } else {
                    appendLine("  • العملة اليمني: حساب مصفى (صفر) ✅")
                }

                if (netSar > 0.0) {
                    appendLine("  • العملة السعودي: مدين له بقيمة ${Helpers.formatWithCurrency(netSar, "ريال سعودي")} ⏳")
                } else if (netSar < 0.0) {
                    appendLine("  • العملة السعودي: رصيد فائض زائد لنا قيمة ${Helpers.formatWithCurrency(-netSar, "ريال سعودي")} 🟢")
                } else {
                    appendLine("  • العملة السعودي: حساب مصفى (صفر) ✅")
                }
            }
            appendLine("---------------------------------------")
            val bOwnerName = brandingOwnerName.ifEmpty { "أحمد منصور" }
            appendLine("صاحب الوكالة: $bOwnerName")
        }

        AlertDialog(
            onDismissRequest = { showSupplierCommitmentDialog = false },
            title = {
                Text(
                    text = "التزامات وأرصدة الموردين الكلية",
                    color = PalWhitePure,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "كشف مفصل بـ حركة التوريد للفترة والالتزامات والمديونيات بكل الموردين مسجلة بدقة وبـ فصل للعملات مستقل وبشكل كامل.",
                        color = PalWhiteSoft,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right
                    )

                    // Period Selector inside Dialog
                    ScrollableTabRow(
                        selectedTabIndex = when (supplierCommitmentPeriod) {
                            "يومي" -> 0
                            "أسبوعي" -> 1
                            "شهري" -> 2
                            "سنوي" -> 3
                            "تقويم مخصص" -> 4
                            else -> 0
                        },
                        containerColor = PalBlackNormal,
                        contentColor = PalGreenLight,
                        edgePadding = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(selected = supplierCommitmentPeriod == "يومي", onClick = { supplierCommitmentPeriod = "يومي" }, text = { Text("اليومية", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                        Tab(selected = supplierCommitmentPeriod == "أسبوعي", onClick = { supplierCommitmentPeriod = "أسبوعي" }, text = { Text("الأسبوعية", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                        Tab(selected = supplierCommitmentPeriod == "شهري", onClick = { supplierCommitmentPeriod = "شهري" }, text = { Text("الشهرية", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                        Tab(selected = supplierCommitmentPeriod == "سنوي", onClick = { supplierCommitmentPeriod = "سنوي" }, text = { Text("السنوية", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                        Tab(selected = supplierCommitmentPeriod == "تقويم مخصص", onClick = { supplierCommitmentPeriod = "تقويم مخصص" }, text = { Text("مخصص 📅", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                    }

                    if (supplierCommitmentPeriod == "تقويم مخصص") {
                        UnifiedPeriodSelector(
                            initialType = supplierCommitmentCustomType,
                            initialValue = supplierCommitmentCustomValue,
                            onPeriodChanged = { type, finalVal ->
                                supplierCommitmentCustomType = type
                                supplierCommitmentCustomValue = finalVal
                            }
                        )
                    }

                    // Card 1: Today's / Period Figures
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PalBlackDark, RoundedCornerShape(8.dp))
                            .border(1.dp, PalBlackLight, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val periodLabelDetail = when (supplierCommitmentPeriod) {
                            "يومي" -> "اليومية ($dateToday)"
                            "أسبوعي" -> "الأسبوعية (7 أيام)"
                            "شهري" -> "الشهرية ($curMonthStr)"
                            "سنوي" -> "السنوية ($curYearStr)"
                            else -> "المخصصة ($supplierCommitmentCustomValue)"
                        }
                        Text("🔺 معاملات وحركة الفترة $periodLabelDetail:", color = PalGoldCalligraphy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("توريد يمني بالفترة:", color = PalWhiteSoft, fontSize = 11.sp)
                            Text(Helpers.formatWithCurrency(purchasesYerToday, "ريال يمني"), color = PalWhitePure, fontSize = 11.sp)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("سداد يمني بالفترة:", color = PalWhiteSoft, fontSize = 11.sp)
                            Text(Helpers.formatWithCurrency(paidYerToday, "ريال يمني"), color = PalGreenLight, fontSize = 11.sp)
                        }
                        Divider(color = PalBlackLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("توريد سعودي بالفترة:", color = PalWhiteSoft, fontSize = 11.sp)
                            Text(Helpers.formatWithCurrency(purchasesSarToday, "ريال سعودي"), color = PalWhitePure, fontSize = 11.sp)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("سداد سعودي بالفترة:", color = PalWhiteSoft, fontSize = 11.sp)
                            Text(Helpers.formatWithCurrency(paidSarToday, "ريال سعودي"), color = PalGreenLight, fontSize = 11.sp)
                        }
                    }

                    // Card 2: Accumulative Figures
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PalBlackDark, RoundedCornerShape(8.dp))
                            .border(1.dp, PalBlackLight, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💳 الالتزامات والأرصدة القائمة الكلية:", color = PalGoldCalligraphy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ديون متبقية علينا (يمني):", color = PalWhiteSoft, fontSize = 11.sp)
                            Text(Helpers.formatWithCurrency(totalDebitYer, "ريال يمني"), color = PalRedLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("أرصدة فائضة زائدة لنا (يمني):", color = PalWhiteSoft, fontSize = 11.sp)
                            Text(Helpers.formatWithCurrency(totalCreditYer, "ريال يمني"), color = PalGreenLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = PalBlackLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ديون متبقية علينا (سعودي):", color = PalWhiteSoft, fontSize = 11.sp)
                            Text(Helpers.formatWithCurrency(totalDebitSar, "ريال سعودي"), color = PalRedLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("أرصدة فائضة زائدة لنا (سعودي):", color = PalWhiteSoft, fontSize = 11.sp)
                            Text(Helpers.formatWithCurrency(totalCreditSar, "ريال سعودي"), color = PalGreenLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Section 3: Per-Supplier Detail
                    Text("🤝 رصيد وموقف حسابت الموردين فردياً:", color = PalWhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    if (supplierBalances.isEmpty()) {
                        Text("لا يوجد موردين مسجلين بقاعدة البيانات حالياً.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    } else {
                        supplierBalances.forEach { (sup, netYer, netSar) ->
                            val regionSuffix = if (sup.region.isNotEmpty()) " (${sup.region})" else ""
                            val supPurchases = purchases.filter { it.supplierId == sup.id }
                            val totalYerSum = supPurchases.sumOf { if (it.totalAmountYer > 0.0) it.totalAmountYer else if (it.currency.contains("يمني")) it.totalAmount else 0.0 }
                            val paidYerSum = supPurchases.sumOf { if (it.totalAmountYer > 0.0) it.paidAmountYer else if (it.currency.contains("يمني")) it.paidAmount else 0.0 }
                            val totalSarSum = supPurchases.sumOf { if (it.totalAmountSar > 0.0) it.totalAmountSar else if (it.currency.contains("سعودي")) it.totalAmount else 0.0 }
                            val paidSarSum = supPurchases.sumOf { if (it.totalAmountSar > 0.0) it.paidAmountSar else if (it.currency.contains("سعودي")) it.paidAmount else 0.0 }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PalBlackDark, RoundedCornerShape(6.dp))
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("👤 ${sup.name}$regionSuffix", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                
                                // Yer Balance
                                Row(Modifier.fillMaxWidth().padding(start = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text("يمني 🇾🇪 - كلي: ${Helpers.formatMoney(totalYerSum)} | مسدد: ${Helpers.formatMoney(paidYerSum)}", color = PalWhiteSoft, fontSize = 9.sp)
                                    val yerLabel = when {
                                        netYer > 0.0 -> "متبقي مدين: " + Helpers.formatWithCurrency(netYer, "ريال يمني")
                                        netYer < 0.0 -> "رصيد زائد لنا: " + Helpers.formatWithCurrency(-netYer, "ريال يمني")
                                        else -> "مصفى (0)"
                                    }
                                    val yerColor = if (netYer > 0) PalRedLight else if (netYer < 0) PalGreenLight else Color.Gray
                                    Text(yerLabel, color = yerColor, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                }

                                // Sar Balance
                                Row(Modifier.fillMaxWidth().padding(start = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text("سعودي 🇸🇦 - كلي: ${Helpers.formatMoney(totalSarSum)} | مسدد: ${Helpers.formatMoney(paidSarSum)}", color = PalWhiteSoft, fontSize = 9.sp)
                                    val sarLabel = when {
                                        netSar > 0.0 -> "متبقي مدين: " + Helpers.formatWithCurrency(netSar, "ريال سعودي")
                                        netSar < 0.0 -> "رصيد زائد لنا: " + Helpers.formatWithCurrency(-netSar, "ريال سعودي")
                                        else -> "مصفى (0)"
                                    }
                                    val sarColor = if (netSar > 0) PalRedLight else if (netSar < 0) PalGreenLight else Color.Gray
                                    Text(sarLabel, color = sarColor, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            Helpers.shareViaWhatsApp(context, "", reportMsg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("واتساب 💬", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val headers = listOf("المورد", "المنطقة", "رصيد يمني", "رصيد سعودي")
                            val rows = supplierBalances.map { (sup, netYer, netSar) ->
                                val yerStr = when {
                                    netYer > 0.0 -> "دين: ${Helpers.formatMoney(netYer)}"
                                    netYer < 0.0 -> "لك: ${Helpers.formatMoney(-netYer)}"
                                    else -> "سليم"
                                }
                                val sarStr = when {
                                    netSar > 0.0 -> "دين: ${Helpers.formatMoney(netSar)}"
                                    netSar < 0.0 -> "لك: ${Helpers.formatMoney(-netSar)}"
                                    else -> "سليم"
                                }
                                listOf(sup.name, sup.region, yerStr + " يمني", sarStr + " سعودي")
                            }

                            val totalsMap = mapOf(
                                "إجمالي ديون الموردين (يمني)" to Helpers.formatWithCurrency(totalDebitYer, "ريال يمني"),
                                "أرصدة زائدة لنا عند الموردين (يمني)" to Helpers.formatWithCurrency(totalCreditYer, "ريال يمني"),
                                "-----------------------------" to "-----------------------------",
                                "إجمالي ديون الموردين (سعودي)" to Helpers.formatWithCurrency(totalDebitSar, "ريال سعودي"),
                                "أرصدة زائدة لنا عند الموردين (سعودي)" to Helpers.formatWithCurrency(totalCreditSar, "ريال سعودي")
                            )

                            Helpers.generatePdfAndShare(
                                context = context,
                                title = "تقرير التزامات وأرصدة الموردين الكلي",
                                headers = headers,
                                rows = rows,
                                totals = totalsMap
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PalRedNormal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ملف PDF 📄", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showSupplierThermalPrintDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PalGoldCalligraphy),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حراري 🖨️", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { showSupplierCommitmentDialog = false }
                    ) {
                        Text("إغلاق", color = PalWhiteMuted, fontSize = 11.sp)
                    }
                }
            },
            containerColor = PalBlackNormal
        )

        if (showSupplierThermalPrintDialog) {
            com.example.ui.components.BluetoothPrintDialog(
                receiptText = reportMsg,
                onDismiss = { showSupplierThermalPrintDialog = false }
            )
        }
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = BorderStroke(1.dp, Color(0xFF222222)),
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MenuTile(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color(0xFF222222)),
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun BottomNavigationBar(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Divider(color = Color(0xFF222222), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomNavItem(
                title = "الرئيسية",
                icon = Icons.Default.Home,
                selected = currentScreen is Screen.Dashboard,
                onClick = { viewModel.navigateTo(Screen.Dashboard) }
            )
            BottomNavItem(
                title = "الحسابات",
                icon = Icons.Default.Leaderboard,
                selected = currentScreen is Screen.Accounts,
                onClick = { viewModel.navigateTo(Screen.Accounts) }
            )
            BottomNavItem(
                title = "الأرشيف",
                icon = Icons.Default.History,
                selected = currentScreen is Screen.Archive,
                onClick = { viewModel.navigateTo(Screen.Archive) }
            )
            BottomNavItem(
                title = "الإعدادات",
                icon = Icons.Default.Settings,
                selected = currentScreen is Screen.Settings,
                onClick = { viewModel.navigateTo(Screen.Settings) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (selected) Color(0xFF10b981) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (selected) Color(0xFF10b981) else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun DashboardKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
        border = BorderStroke(1.dp, Color(0xFF222222)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = PalWhiteMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(PalGoldCalligraphy.copy(0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PalGoldCalligraphy,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                color = PalWhitePure,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = accentColor.copy(0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
