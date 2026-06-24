package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Expense
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import com.example.util.BluetoothPrinterManager
import com.example.util.Helpers
import androidx.compose.ui.platform.testTag
import java.util.*

import com.example.ui.components.UnifiedPeriodSelector
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val expensesList by viewModel.allExpenses.collectAsState()

    var showExpenseDialog by remember { mutableStateOf(false) }
    var inputAmount by remember { mutableStateOf("") }
    var inputNotes by remember { mutableStateOf("") }
    var selectedExpenseCurrency by remember { mutableStateOf("ريال يمني") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var expenseReportDate by remember { mutableStateOf(Helpers.getCurrentDateTime()) }
    
    val categories = listOf("نقل ومواصلات", "إيجار المحل والجمارك", "تعبئة وتغليف وعمالة", "خدمات وتبرعات وطاقة", "مصاريف عامة أخرى")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("الكل") } // يومي, شهري, سنوي, تقويم مخصص, الكل
    var customExpenseReportType by remember { mutableStateOf("يوم") }
    var customExpenseReportValue by remember { mutableStateOf(Helpers.getCurrentDate()) }
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }

    var showBluetoothPrintTrigger by remember { mutableStateOf(false) }
    var bluetoothPrintText by remember { mutableStateOf("") }

    val todayStr = Helpers.getCurrentDate()
    val curMonthStr = todayStr.substring(0, 7)
    val curYearStr = todayStr.substring(0, 4)

    val filteredList = expensesList.filter { exp ->
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayTime = try { sdf.parse(todayStr)?.time ?: 0L } catch(e: Exception) { 0L }
        val isWeekly: (String) -> Boolean = { dateStr ->
            val itemDate = dateStr.take(10)
            try {
                val itemTime = sdf.parse(itemDate)?.time ?: 0L
                val diff = todayTime - itemTime
                diff in 0..(7L * 24 * 60 * 60 * 1000)
            } catch (e: Exception) {
                false
            }
        }
        val matchesPeriod = when (selectedPeriod) {
            "يومي" -> exp.date.startsWith(todayStr)
            "أسبوعي" -> isWeekly(exp.date)
            "شهري" -> exp.date.startsWith(curMonthStr)
            "سنوي" -> exp.date.startsWith(curYearStr)
            "تقويم مخصص" -> {
                val expDate = exp.date.take(10)
                when (customExpenseReportType) {
                    "يوم" -> expDate == customExpenseReportValue
                    "شهر" -> expDate.startsWith(customExpenseReportValue)
                    "سنة" -> expDate.startsWith(customExpenseReportValue)
                    else -> true
                }
            }
            else -> true
        }
        val matchesCategory = selectedCategoryFilter == "الكل" || exp.category == selectedCategoryFilter
        val matchesSearch = searchQuery.trim().isEmpty() || 
                exp.notes.contains(searchQuery, ignoreCase = true) || 
                exp.category.contains(searchQuery, ignoreCase = true)
        
        matchesPeriod && matchesCategory && matchesSearch
    }

    val expensesYer = filteredList.filter { it.currency.contains("يمني") }
    val expensesSar = filteredList.filter { it.currency.contains("سعودي") }

    val totalExpenseSumYer = expensesYer.sumOf { it.amount }
    val totalExpenseSumSar = expensesSar.sumOf { it.amount }

    val printExpensesMsg = buildString {
        val bAppName = com.example.util.BrandingManager.appName.ifEmpty { "وكالة طوفان الأقصى" }
        val bAgencyName = com.example.util.BrandingManager.agencyName.ifEmpty { "لأجود أنواع القات الصعدي" }
        appendLine("$bAppName $bAgencyName")
        appendLine("كشف المصروفات والمنصرفات التشغيلية")
        appendLine("فترة البحث والتصفية: $selectedPeriod")
        appendLine("الفئة المحددة: $selectedCategoryFilter")
        appendLine("تاريخ الجرد: $expenseReportDate")
        appendLine("--------------------------------")
        if (filteredList.isEmpty()) {
            appendLine("لا توجد بنود مصروفات مسجلة مسبقاً لهذه الفئة.")
        } else {
            filteredList.forEach { exp ->
                appendLine("• الفئة: ${exp.category}")
                appendLine("  البيان: ${exp.notes.ifEmpty { "بدون شرح" }}")
                appendLine("  المبلغ: ${Helpers.formatWithCurrency(exp.amount, exp.currency)} | التاريخ: ${exp.date.split(" ")[0]}")
                appendLine("- - - - - - - - - - - - - - - -")
            }
        }
        appendLine("--------------------------------")
        appendLine("إجمالي المصروفات المصدرة:")
        appendLine("👈 بالريال اليمني: ${Helpers.formatWithCurrency(totalExpenseSumYer, "ريال يمني")}")
        appendLine("👈 بالريال السعودي: ${Helpers.formatWithCurrency(totalExpenseSumSar, "ريال سعودي")}")
        val bOwnerName = com.example.util.BrandingManager.ownerName.ifEmpty { "أحمد منصور" }
        appendLine("صاحب الوكالة: $bOwnerName")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("قسم المصروفات الإدارية والعمومية", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = PalWhitePure)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        inputAmount = ""
                        inputNotes = ""
                        selectedExpenseCurrency = "ريال يمني"
                        showExpenseDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة مصروف", tint = PalGreenLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PalBlackNormal)
            )
        },
        containerColor = PalBlackDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Card
            item {
                Card(colors = CardDefaults.cardColors(containerColor = PalBlackNormal)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("إجمالي المصروفات المنصرفة بالفترة والبحث", color = PalWhiteMuted, fontSize = 11.sp)
                            Text(Helpers.formatWithCurrency(totalExpenseSumYer, "ريال يمني"), color = PalRedLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(Helpers.formatWithCurrency(totalExpenseSumSar, "ريال سعودي"), color = PalRedLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .background(PalRedLight.copy(0.12f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = PalRedLight, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // Search and Filters Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("البحث في المصروفات والمنصرفات...", color = PalWhiteMuted, fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PalWhiteMuted) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PalGreenLight, unfocusedBorderColor = PalBlackLight),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Period Selection Tabs
            item {
                val periodsToggle = listOf("يومي", "أسبوعي", "شهري", "سنوي", "تقويم مخصص", "الكل")
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    periodsToggle.forEach { period ->
                        val isSel = selectedPeriod == period
                        val bg = if (isSel) PalGreenNormal else PalBlackNormal
                        val fg = if (isSel) Color.White else Color.Gray
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .clickable { selectedPeriod = period }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(period, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (selectedPeriod == "تقويم مخصص") {
                item {
                    UnifiedPeriodSelector(
                        initialType = customExpenseReportType,
                        initialValue = customExpenseReportValue,
                        onPeriodChanged = { type, finalVal ->
                            customExpenseReportType = type
                            customExpenseReportValue = finalVal
                        }
                    )
                }
            }

            // Category Filter Scrollable Bar
            item {
                val fullCategoryFilters = listOf("الكل") + categories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    fullCategoryFilters.forEach { cat ->
                        val isSel = selectedCategoryFilter == cat
                        val bg = if (isSel) PalGoldCalligraphy else PalBlackNormal
                        val fg = if (isSel) Color.White else Color.Gray
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                                .clickable { selectedCategoryFilter = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = expenseReportDate,
                    onValueChange = { expenseReportDate = it },
                    label = { Text("تعيين وتخصيص تاريخ التقرير المطبوع", color = PalWhiteMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("expenses_report_date_input"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PalGreenLight) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PalGreenLight,
                        unfocusedBorderColor = PalBlackLight,
                        focusedTextColor = PalWhitePure,
                        unfocusedTextColor = PalWhiteSoft,
                        focusedContainerColor = PalBlackDark,
                        unfocusedContainerColor = PalBlackDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Print / Export Actions Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val headers = listOf("التصنيف وفئة المصروف", "البيان والشرح", "العملة", "المبلغ المقيد")
                            val rows = filteredList.map { exp ->
                                listOf(exp.category, exp.notes.ifEmpty { "إداري" }, exp.currency, Helpers.formatWithCurrency(exp.amount, exp.currency))
                            }
                            Helpers.generatePdfAndShare(
                                context = context,
                                title = "كشف المصروفات والمنصرفات ($selectedPeriod)",
                                headers = headers,
                                rows = rows,
                                totals = mapOf(
                                    "إجمالي المصروفات بالريال اليمني" to Helpers.formatWithCurrency(totalExpenseSumYer, "ريال يمني"),
                                    "إجمالي المصروفات بالريال السعودي" to Helpers.formatWithCurrency(totalExpenseSumSar, "ريال سعودي")
                                ),
                                customDate = expenseReportDate,
                                reportPeriod = if (selectedPeriod == "تقويم مخصص") "$customExpenseReportType : $customExpenseReportValue" else selectedPeriod
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PalRedNormal),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("PDF تصدير", color = Color.White, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            Helpers.shareViaWhatsApp(context, "", printExpensesMsg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("واتساب", color = Color.White, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
val bAppName = com.example.util.BrandingManager.appName.ifEmpty { "وكالة طوفان الأقصى" }
                            val bOwnerName = com.example.util.BrandingManager.ownerName.ifEmpty { "أحمد منصور" }
                            val smsMsg = buildString {
                                appendLine("💸 كشف بجميع المصروفات ($selectedPeriod) 💸")
                                appendLine("التاريخ: $expenseReportDate")
                                appendLine("الفئة: $selectedCategoryFilter")
                                appendLine("--- المجموع الكلي للمصروفات ---")
                                appendLine("🇾🇪 يمني: ${Helpers.formatWithCurrency(totalExpenseSumYer, "ريال يمني")}")
                                appendLine("🇸🇦 سعودي: ${Helpers.formatWithCurrency(totalExpenseSumSar, "ريال سعودي")}")
                                appendLine("إدارة $bAppName ($bOwnerName)")
                            }
                            Helpers.shareViaSMS(context, "", smsMsg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("إرسال SMS", color = Color.White, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            bluetoothPrintText = printExpensesMsg
                            showBluetoothPrintTrigger = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PalGoldCalligraphy),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("طباعة 🖨️", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            item {
                Divider(color = PalBlackLight)
            }

            // Expenses List display
            if (filteredList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد بنود مصروفات مطابقة للبحث مسبقاً", color = PalWhiteMuted, fontSize = 12.sp)
                    }
                }
            } else {
                items(filteredList) { exp ->
                    Card(colors = CardDefaults.cardColors(containerColor = PalBlackNormal)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PalRedLight.copy(0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = PalRedLight, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exp.category, color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("شرح: ${exp.notes.ifEmpty { "مصاريف إدارية عمومية" }}", color = PalWhiteMuted, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(Helpers.formatWithCurrency(exp.amount, exp.currency), color = PalRedLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(exp.date.split(" ")[0], color = PalWhiteMuted, fontSize = 9.sp)
                            }
                            IconButton(onClick = {
                                editingExpense = exp
                                selectedCategory = exp.category
                                inputAmount = exp.amount.toInt().toString()
                                inputNotes = exp.notes
                                selectedExpenseCurrency = exp.currency
                                showExpenseDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل المصروف", tint = PalGreenLight)
                            }
                            IconButton(onClick = {
                                expenseToDelete = exp
                                showDeleteConfirm = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = PalWhiteMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBluetoothPrintTrigger) {
        com.example.ui.components.BluetoothPrintDialog(
            receiptText = bluetoothPrintText,
            onDismiss = { showBluetoothPrintTrigger = false }
        )
    }

    if (showExpenseDialog) {
        AlertDialog(
            onDismissRequest = { 
                showExpenseDialog = false
                editingExpense = null
            },
            title = { Text(if (editingExpense != null) "تعديل بند المصروف" else "قيد بند مصروفات تشغيلي", color = PalWhitePure, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تصنيف المصروف:", color = PalWhiteMuted, fontSize = 12.sp)
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (selectedCategory == cat) PalGreenDark else Color.Transparent)
                                .clickable { selectedCategory = cat }
                                .padding(8.dp)
                        ) {
                            Text(cat, color = PalWhitePure, fontSize = 13.sp)
                        }
                        Divider(color = PalBlackLight)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("مبلغ المصروف", color = PalWhiteMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PalGreenLight, unfocusedBorderColor = PalBlackLight),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("نوع العملة لمبلغ المصروف:", color = PalWhiteMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ريال يمني", "ريال سعودي").forEach { curr ->
                            val isSel = selectedExpenseCurrency == curr
                            val bg = if (isSel) PalGreenNormal else PalBlackLight
                            val fg = if (isSel) Color.White else Color.Gray
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .clickable { selectedExpenseCurrency = curr }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(curr, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { inputNotes = it },
                        label = { Text("ملاحظات أخرى وشرح بند الصادر", color = PalWhiteMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PalGreenLight, unfocusedBorderColor = PalBlackLight),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = inputAmount.toDoubleOrNull() ?: 0.0
                        if (amount <= 0.0) {
                            android.widget.Toast.makeText(context, "الرجاء تحديد مبلغ مصروفات صحيح!", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (editingExpense != null) {
                            viewModel.updateExpense(editingExpense!!.copy(category = selectedCategory, amount = amount, notes = inputNotes, currency = selectedExpenseCurrency))
                            editingExpense = null
                        } else {
                            viewModel.recordExpense(selectedCategory, amount, inputNotes, currency = selectedExpenseCurrency)
                        }
                        showExpenseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal)
                ) {
                    Text(if (editingExpense != null) "حفظ التعديل" else "قيد الصادر", color = PalWhitePure)
                }
            },
            containerColor = PalBlackNormal
        )
    }

    if (showDeleteConfirm && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "تأكيد حذف المصروف",
                    color = PalWhitePure,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف هذا المصروف بقيمة (${com.example.util.Helpers.formatMoney(expenseToDelete!!.amount)}) من فئة (${expenseToDelete!!.category})؟ لا يمكن التراجع عن هذه العملية.",
                    color = PalWhiteSoft,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(expenseToDelete!!)
                        showDeleteConfirm = false
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PalRedNormal),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تأكيد الحذف", color = PalWhitePure, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text("إلغاء", color = PalWhiteMuted, fontWeight = FontWeight.Normal)
                }
            },
            containerColor = PalBlackNormal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val invoices by viewModel.allInvoices.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    val taxHistory by viewModel.allTaxHistory.collectAsState()
    val supplies by viewModel.allSuppliers.collectAsState()
    val items by viewModel.inventoryItems.collectAsState()
    val purchases by viewModel.allPurchases.collectAsState()

    var selectedPeriod by remember { mutableStateOf(0) } // 0: Daily, 1: Weekly, 2: Monthly, 3: Yearly, 4: Custom date
    var customPeriodType by remember { mutableStateOf("يوم") }
    var customPeriodValue by remember { mutableStateOf(Helpers.getCurrentDate()) }
    var accountingReportPrintDate by remember { mutableStateOf(Helpers.getCurrentDateTime()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التحاسب التجاري والتقارير العامة", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = PalWhitePure)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PalBlackNormal)
            )
        },
        containerColor = PalBlackDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedPeriod,
                containerColor = PalBlackNormal,
                contentColor = PalGreenLight,
                edgePadding = 8.dp
            ) {
                Tab(selected = selectedPeriod == 0, onClick = { selectedPeriod = 0 }, text = { Text("اليومية", fontSize = 12.sp) })
                Tab(selected = selectedPeriod == 1, onClick = { selectedPeriod = 1 }, text = { Text("أسبوعية", fontSize = 12.sp) })
                Tab(selected = selectedPeriod == 2, onClick = { selectedPeriod = 2 }, text = { Text("الشهرية", fontSize = 12.sp) })
                Tab(selected = selectedPeriod == 3, onClick = { selectedPeriod = 3 }, text = { Text("السنوية", fontSize = 12.sp) })
                Tab(selected = selectedPeriod == 4, onClick = { selectedPeriod = 4 }, text = { Text("تقويم مخصص", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
            }

            if (selectedPeriod == 4) {
                UnifiedPeriodSelector(
                    initialType = customPeriodType,
                    initialValue = customPeriodValue,
                    onPeriodChanged = { type, finalVal ->
                        customPeriodType = type
                        customPeriodValue = finalVal
                    }
                )
            }

            // Calculations based on Period selection
            val todayStr = Helpers.getCurrentDate()
            val curMonthStr = todayStr.substring(0, 7)
            val curYearStr = todayStr.substring(0, 4)

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val todayTime = try { sdf.parse(todayStr)?.time ?: 0L } catch(e: Exception) { 0L }
            val isWeekly: (String) -> Boolean = { dateStr ->
                val itemDate = dateStr.take(10)
                try {
                    val itemTime = sdf.parse(itemDate)?.time ?: 0L
                    val diff = todayTime - itemTime
                    diff in 0..(7L * 24 * 60 * 60 * 1000)
                } catch (e: Exception) {
                    false
                }
            }

            val filteredInvoices = when (selectedPeriod) {
                0 -> invoices.filter { it.date.startsWith(todayStr) }
                1 -> invoices.filter { isWeekly(it.date) }
                2 -> invoices.filter { it.date.startsWith(curMonthStr) }
                3 -> invoices.filter { it.date.startsWith(curYearStr) }
                else -> invoices.filter { it.date.startsWith(customPeriodValue) }
            }
            val filteredExpenses = when (selectedPeriod) {
                0 -> expenses.filter { it.date.startsWith(todayStr) }
                1 -> expenses.filter { isWeekly(it.date) }
                2 -> expenses.filter { it.date.startsWith(curMonthStr) }
                3 -> expenses.filter { it.date.startsWith(curYearStr) }
                else -> expenses.filter { it.date.startsWith(customPeriodValue) }
            }
            val filteredPurchases = when (selectedPeriod) {
                0 -> purchases.filter { it.date.startsWith(todayStr) }
                1 -> purchases.filter { isWeekly(it.date) }
                2 -> purchases.filter { it.date.startsWith(curMonthStr) }
                3 -> purchases.filter { it.date.startsWith(curYearStr) }
                else -> purchases.filter { it.date.startsWith(customPeriodValue) }
            }
            val filteredTaxHistory = when (selectedPeriod) {
                0 -> taxHistory.filter { it.date.startsWith(todayStr) }
                1 -> taxHistory.filter { isWeekly(it.date) }
                2 -> taxHistory.filter { it.date.startsWith(curMonthStr) }
                3 -> taxHistory.filter { it.date.startsWith(curYearStr) }
                else -> taxHistory.filter { it.date.startsWith(customPeriodValue) }
            }

            val totalSalesYer = filteredInvoices.sumOf { it.totalAmountYer }
            val totalExpensesYer = filteredExpenses.filter { it.currency.contains("يمني") }.sumOf { it.amount }
            val totalPurchasesYer = filteredPurchases.sumOf { it.totalAmountYer }
            val totalTaxAmountYer = filteredTaxHistory.sumOf { it.taxAmount }
            val totalStallOutflowYer = filteredTaxHistory.sumOf { it.stallOutflow }
            val totalLaborOutflowYer = filteredTaxHistory.sumOf { it.laborOutflow }
            val netApproxProfitYer = totalSalesYer - totalPurchasesYer - totalTaxAmountYer - totalStallOutflowYer - totalLaborOutflowYer - totalExpensesYer

            val totalSalesSar = filteredInvoices.sumOf { it.totalAmountSar }
            val totalExpensesSar = filteredExpenses.filter { it.currency.contains("سعودي") }.sumOf { it.amount }
            val totalPurchasesSar = filteredPurchases.sumOf { it.totalAmountSar }
            val netApproxProfitSar = totalSalesSar - totalPurchasesSar - totalExpensesSar

            val pLabel = when (selectedPeriod) {
                0 -> "اليومية"
                1 -> "الأسبوعية"
                2 -> "الشهرية"
                3 -> "السنوية"
                else -> "تقويم مخصص"
            }
            val pValDetail = when (selectedPeriod) {
                0 -> todayStr
                1 -> "7 أيام الماضية"
                2 -> curMonthStr
                3 -> curYearStr
                else -> customPeriodValue
            }

            Text("ملخص أداء الفترة المختارة ($pLabel - $pValDetail)", color = PalWhitePure, fontWeight = FontWeight.Bold)

            Card(colors = CardDefaults.cardColors(containerColor = PalBlackNormal)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🇾🇪 الحساب والربحية بالريال اليمني", color = PalGreenLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي المبيعات (+):", color = PalWhiteSoft, fontSize = 12.sp)
                        Text(Helpers.formatWithCurrency(totalSalesYer, "ريال يمني"), color = PalGreenLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي المشتريات (-):", color = PalWhiteSoft, fontSize = 12.sp)
                        Text("- ${Helpers.formatWithCurrency(totalPurchasesYer, "ريال يمني")}", color = PalRedLight, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي ضريبة القات (-):", color = PalWhiteSoft, fontSize = 12.sp)
                        Text("- ${Helpers.formatWithCurrency(totalTaxAmountYer, "ريال يمني")}", color = PalRedLight, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي خرج المفرش (-):", color = PalWhiteSoft, fontSize = 12.sp)
                        Text("- ${Helpers.formatWithCurrency(totalStallOutflowYer, "ريال يمني")}", color = PalRedLight, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي خرج العمال (-):", color = PalWhiteSoft, fontSize = 12.sp)
                        Text("- ${Helpers.formatWithCurrency(totalLaborOutflowYer, "ريال يمني")}", color = PalRedLight, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي مصروفات التشغيل (-):", color = PalWhiteSoft, fontSize = 12.sp)
                        Text("- ${Helpers.formatWithCurrency(totalExpensesYer, "ريال يمني")}", color = PalRedLight, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("صافي أرباح الريال اليمني:", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val ratingColor = if (netApproxProfitYer >= 0) PalGoldCalligraphy else PalRedLight
                        Text(Helpers.formatWithCurrency(netApproxProfitYer, "ريال يمني"), color = ratingColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Divider(color = PalBlackLight, modifier = Modifier.padding(vertical = 4.dp))

                    Text("🇸🇦 الحساب والربحية بالريال السعودي", color = PalGreenLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي المبيعات (+):", color = PalWhiteSoft, fontSize = 12.sp)
                        Text(Helpers.formatWithCurrency(totalSalesSar, "ريال سعودي"), color = PalGreenLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي المشتريات (-):", color = PalWhiteSoft, fontSize = 12.sp)
                        Text("- ${Helpers.formatWithCurrency(totalPurchasesSar, "ريال سعودي")}", color = PalRedLight, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي مصروفات التشغيل (-):", color = PalWhiteSoft, fontSize = 12.sp)
                        Text("- ${Helpers.formatWithCurrency(totalExpensesSar, "ريال سعودي")}", color = PalRedLight, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("صافي أرباح الريال السعودي:", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val ratingColor = if (netApproxProfitSar >= 0) PalGoldCalligraphy else PalRedLight
                        Text(Helpers.formatWithCurrency(netApproxProfitSar, "ريال سعودي"), color = ratingColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = accountingReportPrintDate,
                onValueChange = { accountingReportPrintDate = it },
                label = { Text("تعيين وتخصيص تاريخ التقرير المطبوع", color = PalWhiteMuted, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth().testTag("accounts_report_date_input"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PalGreenLight) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PalGreenLight,
                    unfocusedBorderColor = PalBlackLight,
                    focusedTextColor = PalWhitePure,
                    unfocusedTextColor = PalWhiteSoft,
                    focusedContainerColor = PalBlackDark,
                    unfocusedContainerColor = PalBlackDark
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Button(
                onClick = {
                    val headers = listOf("البند والبيان المحاسبي", "المبلغ بالريال اليمني", "المبلغ بالريال السعودي")
                    val rows = listOf(
                        listOf("إجمالي المبيعات المحققة (+)", Helpers.formatWithCurrency(totalSalesYer, "ريال يمني"), Helpers.formatWithCurrency(totalSalesSar, "ريال سعودي")),
                        listOf("إجمالي المشتريات والمخازن (-)", Helpers.formatWithCurrency(totalPurchasesYer, "ريال يمني"), Helpers.formatWithCurrency(totalPurchasesSar, "ريال سعودي")),
                        listOf("ضرائب القات المسددة (-)", Helpers.formatWithCurrency(totalTaxAmountYer, "ريال يمني"), "0.00 ريال سعودي"),
                        listOf("خرج المفرش المعتمد (-)", Helpers.formatWithCurrency(totalStallOutflowYer, "ريال يمني"), "0.00 ريال سعودي"),
                        listOf("خرج العمال اليومي المعتمد (-)", Helpers.formatWithCurrency(totalLaborOutflowYer, "ريال يمني"), "0.00 ريال سعودي"),
                        listOf("إجمالي المصاريف والتشغيل (-)", Helpers.formatWithCurrency(totalExpensesYer, "ريال يمني"), Helpers.formatWithCurrency(totalExpensesSar, "ريال سعودي"))
                    )
                    Helpers.generatePdfAndShare(
                        context = context,
                        title = "التقرير المحاسبي العام ($pLabel) للوكالة",
                        headers = headers,
                        rows = rows,
                        totals = mapOf(
                            "صافي أرباح الريال اليمني" to Helpers.formatWithCurrency(netApproxProfitYer, "ريال يمني"),
                            "صافي أرباح الريال السعودي" to Helpers.formatWithCurrency(netApproxProfitSar, "ريال سعودي")
                        ),
                        customDate = accountingReportPrintDate,
                        reportPeriod = if (selectedPeriod == 4) "$customPeriodType : $customPeriodValue" else pValDetail
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("تصدير التقرير المحاسبي PDF", color = PalWhitePure)
            }
        }
    }
}

sealed class ArchiveItem {
    abstract val id: Int
    abstract val date: String
    abstract val amount: Double
    abstract val typeArabic: String

    data class Invoice(val data: com.example.data.SalesInvoice) : ArchiveItem() {
        override val id = data.id
        override val date = data.date
        override val amount = data.totalAmount
        override val typeArabic = "مبيعات"
    }

    data class Purchase(val data: com.example.data.SupplierPurchase, val supplierName: String) : ArchiveItem() {
        override val id = data.id
        override val date = data.date
        override val amount = data.totalAmount
        override val typeArabic = "شراء"
    }

    data class GeneralExpense(val data: com.example.data.Expense) : ArchiveItem() {
        override val id = data.id
        override val date = data.date
        override val amount = data.amount
        override val typeArabic = "مصروف"
    }

    data class Transfer(val data: com.example.data.MoneyTransfer) : ArchiveItem() {
        override val id = data.id
        override val date = data.date
        override val amount = data.amount
        override val typeArabic = "حوالة"
    }

    data class CustomerDebt(val data: com.example.data.SalesInvoice) : ArchiveItem() {
        override val id = -data.id
        override val date = data.date
        override val amount = data.debtAmount
        override val typeArabic = "دين عميل"
    }

    data class SupplierDebt(val data: com.example.data.SupplierPurchase, val supplierName: String) : ArchiveItem() {
        override val id = -data.id - 100000
        override val date = data.date
        override val amount = data.debtRemaining
        override val typeArabic = "دين مورد"
    }

    data class Collection(val data: com.example.data.CustomerPayment, val customerName: String) : ArchiveItem() {
        override val id = data.id + 200000
        override val date = data.date
        override val amount = data.amount
        override val typeArabic = "تحصيل"
    }
}

data class ArchiveUIModel(
    val title: String,
    val sub: String,
    val valStr: String,
    val colorVal: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val details: List<Pair<String, String>> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    
    val invoices by viewModel.allInvoices.collectAsState()
    val purchases by viewModel.allPurchases.collectAsState()
    val suppliers by viewModel.allSuppliers.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    val transfers by viewModel.allTransfers.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()

    var searchStr by remember { mutableStateOf("") }
    var showShareArchiveDialog by remember { mutableStateOf(false) }
    var selectedSharePeriod by remember { mutableStateOf("الكل") } // الكل, يومي, شهري, سنوي, تقويم مخصص
    var selectedShareCategory by remember { mutableStateOf("الكل") } // الكل, المبيعات, المشتريات, المصروفات, الحوالات, الديون, التحصيلات
    var customShareType by remember { mutableStateOf("date") }
    var customShareValue by remember { mutableStateOf("") }
    var customSharePrintDate by remember { mutableStateOf(com.example.util.Helpers.getCurrentDate()) }
    
    var showBluetoothPrintTrigger by remember { mutableStateOf(false) }
    var bluetoothPrintText by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) } // 0: الكل, 1: مبيعات, 2: مشتريات, 3: مصروفات, 4: حوالات, 5: ديون, 6: تحصيلات
    val categories = listOf("الكل", "المبيعات", "المشتريات", "المصروفات", "الحوالات", "الديون", "التحصيلات")

    // Compile into single list of ArchiveItems
    val allItems = remember(invoices, purchases, suppliers, expenses, transfers, payments, customers) {
        val list = mutableListOf<ArchiveItem>()
        invoices.forEach { list.add(ArchiveItem.Invoice(it)) }
        purchases.forEach { p ->
            val supName = suppliers.find { it.id == p.supplierId }?.name ?: "مورد غير معروف"
            list.add(ArchiveItem.Purchase(p, supName))
        }
        expenses.forEach { list.add(ArchiveItem.GeneralExpense(it)) }
        transfers.forEach { list.add(ArchiveItem.Transfer(it)) }

        // Add Debts (الديون)
        invoices.filter { it.debtAmount > 0 }.forEach { list.add(ArchiveItem.CustomerDebt(it)) }
        purchases.filter { it.debtRemaining > 0 }.forEach { p ->
            val supName = suppliers.find { it.id == p.supplierId }?.name ?: "مورد غير معروف"
            list.add(ArchiveItem.SupplierDebt(p, supName))
        }

        // Add Collections (التحصيلات)
        payments.forEach { pay ->
            val custName = customers.find { it.id == pay.customerId }?.name ?: "عميل غير معروف"
            list.add(ArchiveItem.Collection(pay, custName))
        }

        // Sort by date descending
        list.sortByDescending { it.date }
        list
    }

    // Filter by type & search text
    val filtered = remember(allItems, searchStr, selectedCategoryIndex) {
        allItems.filter { item ->
            // Category filter
            val matchesCategory = when (selectedCategoryIndex) {
                0 -> true
                1 -> item is ArchiveItem.Invoice
                2 -> item is ArchiveItem.Purchase
                3 -> item is ArchiveItem.GeneralExpense
                4 -> item is ArchiveItem.Transfer
                5 -> item is ArchiveItem.CustomerDebt || item is ArchiveItem.SupplierDebt
                6 -> item is ArchiveItem.Collection
                else -> true
            }
            if (!matchesCategory) return@filter false

            // Search filter
            if (searchStr.trim().isEmpty()) return@filter true
            
            val query = searchStr.trim().lowercase()
            val matchesSearch = when (item) {
                is ArchiveItem.Invoice -> {
                    item.data.customerName.lowercase().contains(query) ||
                            item.data.date.contains(query) ||
                            item.data.id.toString().contains(query) ||
                            item.data.totalAmount.toString().contains(query)
                }
                is ArchiveItem.Purchase -> {
                    item.supplierName.lowercase().contains(query) ||
                            item.data.date.contains(query) ||
                            item.data.id.toString().contains(query) ||
                            item.data.totalAmount.toString().contains(query)
                }
                is ArchiveItem.GeneralExpense -> {
                    item.data.category.lowercase().contains(query) ||
                            item.data.notes.lowercase().contains(query) ||
                            item.data.date.contains(query) ||
                            item.data.amount.toString().contains(query)
                }
                is ArchiveItem.Transfer -> {
                    item.data.sender.lowercase().contains(query) ||
                            item.data.receiver.lowercase().contains(query) ||
                            item.data.notes.lowercase().contains(query) ||
                            item.data.date.contains(query) ||
                            item.data.amount.toString().contains(query)
                }
                is ArchiveItem.CustomerDebt -> {
                    item.data.customerName.lowercase().contains(query) ||
                            item.data.date.contains(query) ||
                            item.data.id.toString().contains(query) ||
                            item.data.debtAmount.toString().contains(query)
                }
                is ArchiveItem.SupplierDebt -> {
                    item.supplierName.lowercase().contains(query) ||
                            item.data.date.contains(query) ||
                            item.data.id.toString().contains(query) ||
                            item.data.debtRemaining.toString().contains(query)
                }
                is ArchiveItem.Collection -> {
                    item.customerName.lowercase().contains(query) ||
                            item.data.notes.lowercase().contains(query) ||
                            item.data.date.contains(query) ||
                            item.data.amount.toString().contains(query)
                }
            }
            matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("أرشيف ومحفوظات السجلات الكلي", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = PalWhitePure)
                    }
                },
                actions = {
                    IconButton(onClick = { showShareArchiveDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "تصدير الأرشيف", tint = PalGreenLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PalBlackNormal)
            )
        },
        containerColor = PalBlackDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchStr,
                onValueChange = { searchStr = it },
                placeholder = { Text("البحث في الأرشيف (بالاسم، التاريخ، المبلغ، الملاحظات)...", color = PalWhiteMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PalWhiteMuted) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PalGreenLight, unfocusedBorderColor = PalBlackLight),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Category filters horizontal scrollable list
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(categories) { index, catName ->
                    val isSelected = selectedCategoryIndex == index
                    AssistChip(
                        onClick = { selectedCategoryIndex = index },
                        label = { Text(catName, color = if (isSelected) PalBlackDark else PalWhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) PalGreenLight else PalBlackNormal
                        ),
                        border = if (isSelected) {
                            BorderStroke(1.dp, PalGreenLight)
                        } else {
                            BorderStroke(1.dp, PalBlackLight)
                        }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("لا توجد فواتير أو حركات مؤرشفة مطابقة لعملية البحث", color = PalWhiteMuted, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { item ->
                        val uiModel = when (item) {
                            is ArchiveItem.Invoice -> {
                                val cName = item.data.customerName.ifEmpty { "عميل نقدي" }
                                val formattedVal = when {
                                    item.data.totalAmountYer > 0.0 && item.data.totalAmountSar > 0.0 -> {
                                        "+ ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountSar, "ريال سعودي")}"
                                    }
                                    item.data.totalAmountYer > 0.0 -> {
                                        "+ ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountYer, "ريال يمني")}"
                                    }
                                    item.data.totalAmountSar > 0.0 -> {
                                        "+ ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountSar, "ريال سعودي")}"
                                    }
                                    else -> {
                                        "+ ${com.example.util.Helpers.formatWithCurrency(item.amount, item.data.currency)}"
                                    }
                                }
                                ArchiveUIModel(
                                    title = "فاتورة مبيعات للعميل: $cName",
                                    sub = "رقم الفاتورة: #${item.data.id} • طريقة الدفع: ${item.data.paymentMethod} • التاريخ: ${item.data.date}",
                                    valStr = formattedVal,
                                    colorVal = PalGreenLight,
                                    icon = Icons.Default.ReceiptLong,
                                    details = listOf(
                                        "رقم الفاتورة" to "#${item.data.id}",
                                        "طريقة الدفع" to item.data.paymentMethod,
                                        "التاريخ" to item.data.date
                                    )
                                )
                            }
                            is ArchiveItem.Purchase -> {
                                val formattedVal = when {
                                    item.data.totalAmountYer > 0.0 && item.data.totalAmountSar > 0.0 -> {
                                        "- [${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountSar, "ريال سعودي")}]"
                                    }
                                    item.data.totalAmountYer > 0.0 -> {
                                        "- ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountYer, "ريال يمني")}"
                                    }
                                    item.data.totalAmountSar > 0.0 -> {
                                        "- ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountSar, "ريال سعودي")}"
                                    }
                                    else -> {
                                        "- ${com.example.util.Helpers.formatWithCurrency(item.amount, item.data.currency)}"
                                    }
                                }
                                ArchiveUIModel(
                                    title = "سند شراء بضاعة من المورد: ${item.supplierName}",
                                    sub = "رقم التوريد: #${item.data.id} • طريقة الدفع: ${item.data.paymentMethod} • التاريخ: ${item.data.date}",
                                    valStr = formattedVal,
                                    colorVal = PalRedLight,
                                    icon = Icons.Default.ShoppingCart,
                                    details = listOf(
                                        "رقم التوريد" to "#${item.data.id}",
                                        "طريقة الدفع" to item.data.paymentMethod,
                                        "التاريخ" to item.data.date
                                    )
                                )
                            }
                            is ArchiveItem.GeneralExpense -> {
                                ArchiveUIModel(
                                    title = "مصروف عام: ${item.data.category}",
                                    sub = "البيان: ${item.data.notes.ifEmpty { "لا يوجد" }} • التاريخ: ${item.data.date}",
                                    valStr = "- ${com.example.util.Helpers.formatWithCurrency(item.data.amount, item.data.currency)}",
                                    colorVal = PalWhiteMuted,
                                    icon = Icons.Default.TrendingDown,
                                    details = listOf(
                                        "الفئة" to item.data.category,
                                        "البيان" to item.data.notes.ifEmpty { "لا يوجد" },
                                        "التاريخ" to item.data.date
                                    )
                                )
                            }
                            is ArchiveItem.Transfer -> {
                                ArchiveUIModel(
                                    title = "حوالة مالية: من (${item.data.sender}) إلى (${item.data.receiver})",
                                    sub = "رقم المعاملة: ${item.data.referenceNumber.ifEmpty { "غير محدد" }} • البيان: ${item.data.notes} • التاريخ: ${item.data.date}",
                                    valStr = com.example.util.Helpers.formatWithCurrency(item.data.amount, item.data.currency),
                                    colorVal = Color(0xFF3b82f6),
                                    icon = Icons.Default.CompareArrows,
                                    details = listOf(
                                        "من" to item.data.sender,
                                        "إلى" to item.data.receiver,
                                        "رقم المعاملة" to item.data.referenceNumber.ifEmpty { "لا يوجد" },
                                        "التاريخ" to item.data.date
                                    )
                                )
                            }
                            is ArchiveItem.CustomerDebt -> {
                                val cName = item.data.customerName.ifEmpty { "عميل" }
                                val formattedVal = when {
                                    item.data.debtAmountYer > 0.0 && item.data.debtAmountSar > 0.0 -> {
                                        "- [${com.example.util.Helpers.formatWithCurrency(item.data.debtAmountYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.debtAmountSar, "ريال سعودي")}]"
                                    }
                                    item.data.debtAmountYer > 0.0 -> {
                                        "- ${com.example.util.Helpers.formatWithCurrency(item.data.debtAmountYer, "ريال يمني")}"
                                    }
                                    item.data.debtAmountSar > 0.0 -> {
                                        "- ${com.example.util.Helpers.formatWithCurrency(item.data.debtAmountSar, "ريال سعودي")}"
                                    }
                                    else -> {
                                        "- ${com.example.util.Helpers.formatWithCurrency(item.amount, item.data.currency)}"
                                    }
                                }
                                ArchiveUIModel(
                                    title = "دين آجل على العميل: $cName",
                                    sub = "رقم الفاتورة المرجعية: #${item.data.id} • التاريخ: ${item.data.date}",
                                    valStr = formattedVal,
                                    colorVal = PalRedLight,
                                    icon = Icons.Default.TrendingDown,
                                    details = listOf(
                                        "العميل" to cName,
                                        "الفاتورة المرجعية" to "#${item.data.id}",
                                        "التاريخ" to item.data.date
                                    )
                                )
                            }
                            is ArchiveItem.SupplierDebt -> {
                                val formattedVal = when {
                                    item.data.debtRemainingYer > 0.0 && item.data.debtRemainingSar > 0.0 -> {
                                        "- [${com.example.util.Helpers.formatWithCurrency(item.data.debtRemainingYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.debtRemainingSar, "ريال سعودي")}]"
                                    }
                                    item.data.debtRemainingYer > 0.0 -> {
                                        "- ${com.example.util.Helpers.formatWithCurrency(item.data.debtRemainingYer, "ريال يمني")}"
                                    }
                                    item.data.debtRemainingSar > 0.0 -> {
                                        "- ${com.example.util.Helpers.formatWithCurrency(item.data.debtRemainingSar, "ريال سعودي")}"
                                    }
                                    else -> {
                                        "- ${com.example.util.Helpers.formatWithCurrency(item.amount, item.data.currency)}"
                                    }
                                }
                                ArchiveUIModel(
                                    title = "دين آجل للمورد: ${item.supplierName}",
                                    sub = "رقم فاتورة الشراء: #${item.data.id} • التاريخ: ${item.data.date}",
                                    valStr = formattedVal,
                                    colorVal = PalRedLight,
                                    icon = Icons.Default.TrendingDown,
                                    details = listOf(
                                        "المورد" to item.supplierName,
                                        "سند المشتريات مـرجع" to "#${item.data.id}",
                                        "التاريخ" to item.data.date
                                    )
                                )
                            }
                            is ArchiveItem.Collection -> {
                                ArchiveUIModel(
                                    title = "تحصيل دفعة مالية من العميل: ${item.customerName}",
                                    sub = "رقم السند: #${item.data.id} • البيان: ${item.data.notes} • التاريخ: ${item.data.date}",
                                    valStr = "+ ${com.example.util.Helpers.formatWithCurrency(item.data.amount, item.data.currency)}",
                                    colorVal = PalGreenLight,
                                    icon = Icons.Default.Payments,
                                    details = listOf(
                                        "العميل" to item.customerName,
                                        "رقم السند" to "#${item.data.id}",
                                        "البيان" to item.data.notes.ifEmpty { "سداد حساب" },
                                        "التاريخ" to item.data.date
                                    )
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                            border = BorderStroke(1.dp, Color(0xFF222222)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // Row 1: Icon & Operation Name (Taking full remaining horizontal width)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(uiModel.colorVal.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(uiModel.icon, contentDescription = null, tint = uiModel.colorVal, modifier = Modifier.size(18.dp))
                                    }
                                    Text(
                                        text = uiModel.title,
                                        color = PalWhitePure,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Row 2: Value Row (Clearly laid out below the title)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "المبلغ الصافي:",
                                        color = PalWhiteMuted,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = uiModel.valStr,
                                        color = uiModel.colorVal,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }
                                
                                if (uiModel.details.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        uiModel.details.forEach { detail ->
                                            Row(
                                                modifier = Modifier
                                                    .background(Color(0xFF141416), RoundedCornerShape(6.dp))
                                                    .border(BorderStroke(1.dp, Color(0xFF242427)), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = detail.first + ":", color = PalWhiteMuted, fontSize = 10.sp)
                                                Text(text = detail.second, color = PalWhiteSoft, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                } else if (uiModel.sub.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = uiModel.sub,
                                        color = PalWhiteMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareArchiveDialog) {
        val today = com.example.util.Helpers.getCurrentDate()
        
        // 1. First, compute period-filtered items (independently of category filter)
        val periodFilteredItems = remember(allItems, selectedSharePeriod, customShareValue, customShareType) {
            var list = allItems.toList()
            val sdfStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val todayT = try { sdfStr.parse(today)?.time ?: 0L } catch(e: Exception) { 0L }
            val isWeeklyRecord: (String) -> Boolean = { dateStr ->
                val itemDate = dateStr.take(10)
                try {
                    val itemTime = sdfStr.parse(itemDate)?.time ?: 0L
                    val diff = todayT - itemTime
                    diff in 0..(7L * 24 * 60 * 60 * 1000)
                } catch (e: Exception) {
                    false
                }
            }

            // Filter by period
            list = when (selectedSharePeriod) {
                "يومي" -> list.filter { it.date.startsWith(today) }
                "أسبوعي" -> list.filter { isWeeklyRecord(it.date) }
                "شهري" -> list.filter { it.date.startsWith(today.substring(0, 7)) }
                "سنوي" -> list.filter { it.date.startsWith(today.substring(0, 4)) }
                "تقويم مخصص" -> {
                    val targetVal = customShareValue.ifEmpty { today }
                    list.filter { item ->
                        val itemDate = item.date.take(10)
                        when (customShareType) {
                            "date" -> itemDate == targetVal
                            "month" -> itemDate.startsWith(targetVal)
                            "year" -> itemDate.startsWith(targetVal)
                            else -> true
                        }
                    }
                }
                else -> list
            }
            list.sortedByDescending { it.date }
        }

        // 2. Compute sortedItems (category-filtered) for display/export details
        val sortedItems = remember(periodFilteredItems, selectedShareCategory) {
            val list = when (selectedShareCategory) {
                "المبيعات" -> periodFilteredItems.filter { it is ArchiveItem.Invoice }
                "المشتريات" -> periodFilteredItems.filter { it is ArchiveItem.Purchase }
                "المصروفات" -> periodFilteredItems.filter { it is ArchiveItem.GeneralExpense }
                "الحوالات" -> periodFilteredItems.filter { it is ArchiveItem.Transfer }
                "الديون" -> periodFilteredItems.filter { it is ArchiveItem.CustomerDebt || it is ArchiveItem.SupplierDebt }
                "التحصيلات" -> periodFilteredItems.filter { it is ArchiveItem.Collection }
                else -> periodFilteredItems
            }
            list.sortedByDescending { it.date }
        }

        // Calculations for totals BASED ON period-filtered items to ensure absolute correctness and completeness!
        val totalIn = periodFilteredItems.filterIsInstance<ArchiveItem.Invoice>().sumOf { it.amount }
        val totalOutBuy = periodFilteredItems.filterIsInstance<ArchiveItem.Purchase>().sumOf { it.amount }
        val totalOutExp = periodFilteredItems.filterIsInstance<ArchiveItem.GeneralExpense>().sumOf { it.amount }
        val totalTransfers = periodFilteredItems.filterIsInstance<ArchiveItem.Transfer>().sumOf { it.amount }
        val totalCustomerDebts = periodFilteredItems.filterIsInstance<ArchiveItem.CustomerDebt>().sumOf { it.amount }
        val totalSupplierDebts = periodFilteredItems.filterIsInstance<ArchiveItem.SupplierDebt>().sumOf { it.amount }
        val totalCollections = periodFilteredItems.filterIsInstance<ArchiveItem.Collection>().sumOf { it.amount }

        val totalInYer = periodFilteredItems.filterIsInstance<ArchiveItem.Invoice>().sumOf {
            if (it.data.totalAmountYer > 0.0) it.data.totalAmountYer else if (it.data.currency.contains("يمني")) it.amount else 0.0
        }
        val totalInSar = periodFilteredItems.filterIsInstance<ArchiveItem.Invoice>().sumOf {
            if (it.data.totalAmountSar > 0.0) it.data.totalAmountSar else if (it.data.currency.contains("سعودي")) it.amount else 0.0
        }

        val totalOutBuyYer = periodFilteredItems.filterIsInstance<ArchiveItem.Purchase>().sumOf {
            if (it.data.totalAmountYer > 0.0) it.data.totalAmountYer else if (it.data.currency.contains("يمني")) it.amount else 0.0
        }
        val totalOutBuySar = periodFilteredItems.filterIsInstance<ArchiveItem.Purchase>().sumOf {
            if (it.data.totalAmountSar > 0.0) it.data.totalAmountSar else if (it.data.currency.contains("سعودي")) it.amount else 0.0
        }

        val totalOutExpYer = periodFilteredItems.filterIsInstance<ArchiveItem.GeneralExpense>().filter { it.data.currency.contains("يمني") }.sumOf { it.amount }
        val totalOutExpSar = periodFilteredItems.filterIsInstance<ArchiveItem.GeneralExpense>().filter { it.data.currency.contains("سعودي") }.sumOf { it.amount }

        val totalTransfersYer = periodFilteredItems.filterIsInstance<ArchiveItem.Transfer>().filter { it.data.currency.contains("يمني") }.sumOf { it.amount }
        val totalTransfersSar = periodFilteredItems.filterIsInstance<ArchiveItem.Transfer>().filter { it.data.currency.contains("سعودي") }.sumOf { it.amount }

        val totalCustomerDebtsYer = periodFilteredItems.filterIsInstance<ArchiveItem.CustomerDebt>().sumOf {
            if (it.data.debtAmountYer > 0.0) it.data.debtAmountYer else if (it.data.currency.contains("يمني")) it.amount else 0.0
        }
        val totalCustomerDebtsSar = periodFilteredItems.filterIsInstance<ArchiveItem.CustomerDebt>().sumOf {
            if (it.data.debtAmountSar > 0.0) it.data.debtAmountSar else if (it.data.currency.contains("سعودي")) it.amount else 0.0
        }

        val totalSupplierDebtsYer = periodFilteredItems.filterIsInstance<ArchiveItem.SupplierDebt>().sumOf {
            if (it.data.debtRemainingYer > 0.0) it.data.debtRemainingYer else if (it.data.currency.contains("يمني")) it.amount else 0.0
        }
        val totalSupplierDebtsSar = periodFilteredItems.filterIsInstance<ArchiveItem.SupplierDebt>().sumOf {
            if (it.data.debtRemainingSar > 0.0) it.data.debtRemainingSar else if (it.data.currency.contains("سعودي")) it.amount else 0.0
        }

        val totalCollectionsYer = periodFilteredItems.filterIsInstance<ArchiveItem.Collection>().filter { it.data.currency.contains("يمني") }.sumOf { it.amount }
        val totalCollectionsSar = periodFilteredItems.filterIsInstance<ArchiveItem.Collection>().filter { it.data.currency.contains("سعودي") }.sumOf { it.amount }

        AlertDialog(
            onDismissRequest = { showShareArchiveDialog = false },
            title = {
                Text(
                    text = "مشاركة وتصدير الأرشيف الكلي",
                    color = PalWhitePure,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
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
                        text = "فلترة وتصدير الأرشيف لمشاركتها عبر التقارير التفصيلية أو الواتساب أو الطباعة الحرارية المباشرة:",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(color = PalBlackLight)

                    // Period choices
                    Text("اختر المدى الزمني للتقرير بالأرشيف:", color = PalWhiteSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                    val sharePeriods = listOf("الكل", "يومي", "أسبوعي", "شهري", "سنوي", "تقويم مخصص")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            sharePeriods.take(3).forEach { period ->
                                val isSel = selectedSharePeriod == period
                                val bg = if (isSel) PalGreenNormal else PalBlackLight
                                val fg = if (isSel) Color.White else Color.Gray
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickable { selectedSharePeriod = period }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (period) {
                                            "الكل" -> "كامل السجلات"
                                            "يومي" -> "اليوم"
                                            "أسبوعي" -> "أسبوعي"
                                            else -> period
                                        },
                                        color = fg,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            sharePeriods.drop(3).forEach { period ->
                                val isSel = selectedSharePeriod == period
                                val bg = if (isSel) PalGreenNormal else PalBlackLight
                                val fg = if (isSel) Color.White else Color.Gray
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickable { selectedSharePeriod = period }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (period) {
                                            "شهري" -> "الشهر"
                                            "سنوي" -> "العام"
                                            "تقويم مخصص" -> "تقويم مخصص"
                                            else -> period
                                        },
                                        color = fg,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (selectedSharePeriod == "تقويم مخصص") {
                        UnifiedPeriodSelector(
                            initialType = if (customShareType == "date") "يوم" else if (customShareType == "month") "شهر" else "سنة",
                            initialValue = customShareValue.ifEmpty { com.example.util.Helpers.getCurrentDate() },
                            onPeriodChanged = { type, formattedValue ->
                                customShareType = when (type) {
                                    "يوم" -> "date"
                                    "شهر" -> "month"
                                    "سنة" -> "year"
                                    else -> "date"
                                }
                                customShareValue = formattedValue
                                customSharePrintDate = formattedValue
                            }
                        )
                        
                        OutlinedTextField(
                            value = customSharePrintDate,
                            onValueChange = { customSharePrintDate = it },
                            label = { Text("تعيين وتخصيص تاريخ التقرير المطبوع", color = PalWhiteSoft) },
                            placeholder = { Text("اكتب تاريخ أو فترة تخصيص التقرير...", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PalGreenLight,
                                unfocusedBorderColor = PalBlackLight,
                                focusedTextColor = PalWhitePure,
                                unfocusedTextColor = PalWhiteSoft,
                                focusedContainerColor = PalBlackDark,
                                unfocusedContainerColor = PalBlackDark
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Category choices
                    Text("اختر تجميع السجلات حسب الفئة:", color = PalWhiteSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                    val shareCategories = listOf("الكل", "المبيعات", "المشتريات", "المصروفات", "الحوالات", "الديون", "التحصيلات")
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            shareCategories.take(3).forEach { cat ->
                                val isSel = selectedShareCategory == cat
                                val bg = if (isSel) PalGreenNormal else PalBlackLight
                                val fg = if (isSel) Color.White else Color.Gray
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickable { selectedShareCategory = cat }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            shareCategories.slice(3..4).forEach { cat ->
                                val isSel = selectedShareCategory == cat
                                val bg = if (isSel) PalGreenNormal else PalBlackLight
                                val fg = if (isSel) Color.White else Color.Gray
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickable { selectedShareCategory = cat }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            shareCategories.drop(5).forEach { cat ->
                                val isSel = selectedShareCategory == cat
                                val bg = if (isSel) PalGreenNormal else PalBlackLight
                                val fg = if (isSel) Color.White else Color.Gray
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickable { selectedShareCategory = cat }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Divider(color = PalBlackLight)

                    // Summary statistics
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PalBlackDark, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("ملخص حركات التصدير المحددة:", color = PalWhiteMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "المبيعات") {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي المبيعات (يمني):", color = PalWhiteSoft, fontSize = 11.sp)
                                Text(com.example.util.Helpers.formatWithCurrency(totalInYer, "ريال يمني"), color = PalGreenLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي المبيعات (سعودي):", color = PalWhiteSoft, fontSize = 11.sp)
                                Text(com.example.util.Helpers.formatWithCurrency(totalInSar, "ريال سعودي"), color = PalGreenLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "المشتريات") {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي التزويد والمشتريات (يمني):", color = PalWhiteSoft, fontSize = 11.sp)
                                Text("- " + com.example.util.Helpers.formatWithCurrency(totalOutBuyYer, "ريال يمني"), color = PalRedLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي التزويد والمشتريات (سعودي):", color = PalWhiteSoft, fontSize = 11.sp)
                                Text("- " + com.example.util.Helpers.formatWithCurrency(totalOutBuySar, "ريال سعودي"), color = PalRedLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "المصروفات") {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي المصروفات (يمني):", color = PalWhiteSoft, fontSize = 11.sp)
                                Text("- " + com.example.util.Helpers.formatWithCurrency(totalOutExpYer, "ريال يمني"), color = PalWhitePure, fontSize = 11.sp)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي المصروفات (سعودي):", color = PalWhiteSoft, fontSize = 11.sp)
                                Text("- " + com.example.util.Helpers.formatWithCurrency(totalOutExpSar, "ريال سعودي"), color = PalWhitePure, fontSize = 11.sp)
                            }
                        }
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "الحوالات") {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي قيمة الحوالات (يمني):", color = PalWhiteSoft, fontSize = 11.sp)
                                Text(com.example.util.Helpers.formatWithCurrency(totalTransfersYer, "ريال يمني"), color = Color(0xFF3b82f6), fontSize = 11.sp)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي قيمة الحوالات (سعودي):", color = PalWhiteSoft, fontSize = 11.sp)
                                Text(com.example.util.Helpers.formatWithCurrency(totalTransfersSar, "ريال سعودي"), color = Color(0xFF3b82f6), fontSize = 11.sp)
                            }
                        }
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "الديون") {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي الديون الآجلة للعملاء:", color = PalWhiteSoft, fontSize = 11.sp)
                                Text(com.example.util.Helpers.formatWithCurrency(totalCustomerDebtsYer, "ريال يمني") + " / " + com.example.util.Helpers.formatWithCurrency(totalCustomerDebtsSar, "ريال سعودي"), color = PalRedLight, fontSize = 11.sp)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي حركات آجل الموردين:", color = PalWhiteSoft, fontSize = 11.sp)
                                Text(com.example.util.Helpers.formatWithCurrency(totalSupplierDebtsYer, "ريال يمني") + " / " + com.example.util.Helpers.formatWithCurrency(totalSupplierDebtsSar, "ريال سعودي"), color = PalRedLight, fontSize = 11.sp)
                            }
                        }
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "التحصيلات") {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إجمالي التحصيلات المستلمة:", color = PalWhiteSoft, fontSize = 11.sp)
                                Text(com.example.util.Helpers.formatWithCurrency(totalCollectionsYer, "ريال يمني") + " / " + com.example.util.Helpers.formatWithCurrency(totalCollectionsSar, "ريال سعودي"), color = PalGreenLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider(color = PalBlackLight)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("مجموع العمليات المفلترة:", color = PalWhitePure, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${sortedItems.size} عملية", color = PalGoldCalligraphy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val shareMsgText = buildString {
                        val bAppName = com.example.util.BrandingManager.appName.ifEmpty { "وكالة طوفان الأقصى" }
                        val bAgencyName = com.example.util.BrandingManager.agencyName.ifEmpty { "لأجود أنواع القات الصعدي" }
                        appendLine("$bAppName $bAgencyName")
                        appendLine("تصدير الأرشيف ومحفوظات السجلات الموحد الكامل")
                        val pStr = if (selectedSharePeriod == "تقويم مخصص") customSharePrintDate else selectedSharePeriod
                        appendLine("نوع المدى الزمني: $pStr")
                        appendLine("نوع السجلات: $selectedShareCategory")
                        appendLine("التاريخ والوقت: ${com.example.util.Helpers.getCurrentDateTime()}")
                        appendLine("=================================")
                        if (sortedItems.isEmpty()) {
                            appendLine("لا توجد عمليات مسجلة متطابقة مع شروط الفلترة لهذه الفترة.")
                        } else {
                            sortedItems.forEachIndexed { idx, item ->
                                val dateClean = item.date
                                val currency = when (item) {
                                    is ArchiveItem.Invoice -> item.data.currency
                                    is ArchiveItem.Purchase -> item.data.currency
                                    is ArchiveItem.GeneralExpense -> item.data.currency
                                    is ArchiveItem.Transfer -> item.data.currency
                                    is ArchiveItem.CustomerDebt -> item.data.currency
                                    is ArchiveItem.SupplierDebt -> item.data.currency
                                    is ArchiveItem.Collection -> item.data.currency
                                }
                                when (item) {
                                    is ArchiveItem.Invoice -> {
                                        val cName = item.data.customerName.ifEmpty { "عميل نقدي" }
                                        appendLine("${idx + 1}) [مبيعات] فاتورة بيع للعميل ($cName) #${item.data.id}")
                                        appendLine("   طريقة الدفع: ${item.data.paymentMethod} • التاريخ: $dateClean")
                                        val valStr = when {
                                            item.data.totalAmountYer > 0.0 && item.data.totalAmountSar > 0.0 -> {
                                                "${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountSar, "ريال سعودي")}"
                                            }
                                            else -> com.example.util.Helpers.formatWithCurrency(item.amount, currency)
                                        }
                                        appendLine("   المبلغ الصافي: + $valStr")
                                    }
                                    is ArchiveItem.Purchase -> {
                                        appendLine("${idx + 1}) [مشتريات] تزويد بضاعة من المورد (${item.supplierName}) #${item.data.id}")
                                        appendLine("   طريقة الدفع: ${item.data.paymentMethod} • التاريخ: $dateClean")
                                        val valStr = when {
                                            item.data.totalAmountYer > 0.0 && item.data.totalAmountSar > 0.0 -> {
                                                "${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountSar, "ريال سعودي")}"
                                            }
                                            else -> com.example.util.Helpers.formatWithCurrency(item.amount, currency)
                                        }
                                        appendLine("   المبلغ الصافي: - $valStr")
                                    }
                                    is ArchiveItem.GeneralExpense -> {
                                        appendLine("${idx + 1}) [مصروف] مصروف عام فئة: (${item.data.category})")
                                        appendLine("   ملاحظة: ${item.data.notes} • التاريخ: $dateClean")
                                        appendLine("   القيمة المخصومة: - ${com.example.util.Helpers.formatWithCurrency(item.data.amount, currency)}")
                                    }
                                    is ArchiveItem.Transfer -> {
                                        appendLine("${idx + 1}) [حوالة] حوالة من (${item.data.sender}) إلى (${item.data.receiver})")
                                        appendLine("   رقم المرجع: ${item.data.referenceNumber} • الملاحظة: ${item.data.notes}")
                                        appendLine("   الصافي: ${com.example.util.Helpers.formatWithCurrency(item.data.amount, currency)} • التاريخ: $dateClean")
                                    }
                                    is ArchiveItem.CustomerDebt -> {
                                        val cName = item.data.customerName.ifEmpty { "عميل" }
                                        appendLine("${idx + 1}) [دين عميل] دين آجل على العميل ($cName)")
                                        appendLine("   رقم الفاتورة: #${item.data.id} • التاريخ: $dateClean")
                                        val valStr = when {
                                            item.data.debtAmountYer > 0.0 && item.data.debtAmountSar > 0.0 -> {
                                                "${com.example.util.Helpers.formatWithCurrency(item.data.debtAmountYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.debtAmountSar, "ريال سعودي")}"
                                            }
                                            else -> com.example.util.Helpers.formatWithCurrency(item.amount, currency)
                                        }
                                        appendLine("   قيمة الدين: - $valStr")
                                    }
                                    is ArchiveItem.SupplierDebt -> {
                                        appendLine("${idx + 1}) [دين مورد] دين آجل للمورد (${item.supplierName})")
                                        appendLine("   رقم الفاتورة: #${item.data.id} • التاريخ: $dateClean")
                                        val valStr = when {
                                            item.data.debtRemainingYer > 0.0 && item.data.debtRemainingSar > 0.0 -> {
                                                "${com.example.util.Helpers.formatWithCurrency(item.data.debtRemainingYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.debtRemainingSar, "ريال سعودي")}"
                                            }
                                            else -> com.example.util.Helpers.formatWithCurrency(item.amount, currency)
                                        }
                                        appendLine("   قيمة الدين: - $valStr")
                                    }
                                    is ArchiveItem.Collection -> {
                                        appendLine("${idx + 1}) [تحصيل] دفعة مالية مستلمة من العميل (${item.customerName})")
                                        appendLine("   رقم السند: #${item.data.id} • البيان: ${item.data.notes} • التاريخ: $dateClean")
                                        appendLine("   المبلغ المستلم: + ${com.example.util.Helpers.formatWithCurrency(item.data.amount, currency)}")
                                    }
                                }
                                appendLine("   -------------------")
                            }
                        }
                        appendLine("=================================")
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "المبيعات") {
                            appendLine("📊 إجمالي مبيعات (+):")
                            appendLine("   - بالريال اليمني: ${com.example.util.Helpers.formatWithCurrency(totalInYer, "ريال يمني")}")
                            appendLine("   - بالريال السعودي: ${com.example.util.Helpers.formatWithCurrency(totalInSar, "ريال سعودي")}")
                        }
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "المشتريات") {
                            appendLine("📊 إجمالي مشتريات وتوريد (-):")
                            appendLine("   - بالريال اليمني: ${com.example.util.Helpers.formatWithCurrency(totalOutBuyYer, "ريال يمني")}")
                            appendLine("   - بالريال السعودي: ${com.example.util.Helpers.formatWithCurrency(totalOutBuySar, "ريال سعودي")}")
                        }
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "المصروفات") {
                            appendLine("📊 إجمالي مصروفات تشغيلية (-):")
                            appendLine("   - بالريال اليمني: ${com.example.util.Helpers.formatWithCurrency(totalOutExpYer, "ريال يمني")}")
                            appendLine("   - بالريال السعودي: ${com.example.util.Helpers.formatWithCurrency(totalOutExpSar, "ريال سعودي")}")
                        }
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "الحوالات") {
                            appendLine("📊 إجمالي قيمة الحوالات:")
                            appendLine("   - بالريال اليمني: ${com.example.util.Helpers.formatWithCurrency(totalTransfersYer, "ريال يمني")}")
                            appendLine("   - بالريال السعودي: ${com.example.util.Helpers.formatWithCurrency(totalTransfersSar, "ريال سعودي")}")
                        }
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "الديون") {
                            appendLine("📊 إجمالي ديون العملاء:")
                            appendLine("   - بالريال اليمني: ${com.example.util.Helpers.formatWithCurrency(totalCustomerDebtsYer, "ريال يمني")}")
                            appendLine("   - بالريال السعودي: ${com.example.util.Helpers.formatWithCurrency(totalCustomerDebtsSar, "ريال سعودي")}")
                            appendLine("📊 إجمالي ديون الموردين:")
                            appendLine("   - بالريال اليمني: ${com.example.util.Helpers.formatWithCurrency(totalSupplierDebtsYer, "ريال يمني")}")
                            appendLine("   - بالريال السعودي: ${com.example.util.Helpers.formatWithCurrency(totalSupplierDebtsSar, "ريال سعودي")}")
                        }
                        if (selectedShareCategory == "الكل" || selectedShareCategory == "التحصيلات") {
                            appendLine("📊 إجمالي التحصيلات:")
                            appendLine("   - بالريال اليمني: ${com.example.util.Helpers.formatWithCurrency(totalCollectionsYer, "ريال يمني")}")
                            appendLine("   - بالريال السعودي: ${com.example.util.Helpers.formatWithCurrency(totalCollectionsSar, "ريال سعودي")}")
                        }
                        appendLine("=================================")
                        appendLine("📊 إجمالي العمليات المتبقية: ${sortedItems.size} عملية")
                        val bOwnerName = com.example.util.BrandingManager.ownerName.ifEmpty { "أحمد منصور" }
                        appendLine("صاحب العمل: $bOwnerName")
                    }

                    // Action buttons with spacing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = {
                                val printPeriod = if (selectedSharePeriod == "تقويم مخصص") customSharePrintDate else selectedSharePeriod
                                val bAppName = com.example.util.BrandingManager.appName.ifEmpty { "وكالة طوفان الأقصى" }
                                val titlePdf = "تصدير تفصيلي للأرشيف $bAppName ($printPeriod)"
                                val headers = listOf("نوع السند", "البيان والتفاصيل", "القيمة المالية", "التاريخ والوقت")
                                val pdfRows = sortedItems.map { item ->
                                    val currency = when (item) {
                                        is ArchiveItem.Invoice -> item.data.currency
                                        is ArchiveItem.Purchase -> item.data.currency
                                        is ArchiveItem.GeneralExpense -> item.data.currency
                                        is ArchiveItem.Transfer -> item.data.currency
                                        is ArchiveItem.CustomerDebt -> item.data.currency
                                        is ArchiveItem.SupplierDebt -> item.data.currency
                                        is ArchiveItem.Collection -> item.data.currency
                                    }
                                    val (typeStr, descStr, valueStr) = when (item) {
                                        is ArchiveItem.Invoice -> {
                                            val cName = item.data.customerName.ifEmpty { "عميل نقدي" }
                                            val valText = when {
                                                item.data.totalAmountYer > 0.0 && item.data.totalAmountSar > 0.0 -> {
                                                    "${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountSar, "ريال سعودي")}"
                                                }
                                                else -> com.example.util.Helpers.formatWithCurrency(item.amount, currency)
                                            }
                                            Triple("مبيعات", "فاتورة بيع للعميل: $cName (#${item.data.id})", "+ $valText")
                                        }
                                        is ArchiveItem.Purchase -> {
                                            val valText = when {
                                                item.data.totalAmountYer > 0.0 && item.data.totalAmountSar > 0.0 -> {
                                                    "${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.totalAmountSar, "ريال سعودي")}"
                                                }
                                                else -> com.example.util.Helpers.formatWithCurrency(item.amount, currency)
                                            }
                                            Triple("مشتريات", "شراء وتوريد بضاعة: ${item.supplierName} (#${item.data.id})", "- $valText")
                                        }
                                        is ArchiveItem.GeneralExpense -> {
                                            Triple("مصروف", "مصروف عام: ${item.data.category} - ${item.data.notes}", "- ${com.example.util.Helpers.formatWithCurrency(item.data.amount, currency)}")
                                        }
                                        is ArchiveItem.Transfer -> {
                                            Triple("حوالة", "من ${item.data.sender} إلى ${item.data.receiver} - ${item.data.notes}", com.example.util.Helpers.formatWithCurrency(item.data.amount, currency))
                                        }
                                        is ArchiveItem.CustomerDebt -> {
                                            val cName = item.data.customerName.ifEmpty { "عميل" }
                                            val valText = when {
                                                item.data.debtAmountYer > 0.0 && item.data.debtAmountSar > 0.0 -> {
                                                    "${com.example.util.Helpers.formatWithCurrency(item.data.debtAmountYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.debtAmountSar, "ريال سعودي")}"
                                                }
                                                else -> com.example.util.Helpers.formatWithCurrency(item.amount, currency)
                                            }
                                            Triple("دين عميل", "دين آجل على العميل: $cName (#${item.data.id})", "- $valText")
                                        }
                                        is ArchiveItem.SupplierDebt -> {
                                            val valText = when {
                                                item.data.debtRemainingYer > 0.0 && item.data.debtRemainingSar > 0.0 -> {
                                                    "${com.example.util.Helpers.formatWithCurrency(item.data.debtRemainingYer, "ريال يمني")} + ${com.example.util.Helpers.formatWithCurrency(item.data.debtRemainingSar, "ريال سعودي")}"
                                                }
                                                else -> com.example.util.Helpers.formatWithCurrency(item.amount, currency)
                                            }
                                            Triple("دين مورد", "دين آجل للمورد: ${item.supplierName} (#${item.data.id})", "- $valText")
                                        }
                                        is ArchiveItem.Collection -> {
                                            Triple("تحصيل", "تحصيل دفعة من العميل: ${item.customerName} (#${item.data.id}) - ${item.data.notes}", "+ ${com.example.util.Helpers.formatWithCurrency(item.data.amount, currency)}")
                                        }
                                    }
                                    listOf(typeStr, descStr, valueStr, item.date)
                                }
                                val pdfTotals = mutableMapOf<String, String>()
                                if (selectedShareCategory == "الكل" || selectedShareCategory == "المبيعات") {
                                    pdfTotals["إجمالي مبيعات بالريال اليمني"] = com.example.util.Helpers.formatWithCurrency(totalInYer, "ريال يمني")
                                    pdfTotals["إجمالي مبيعات بالريال السعودي"] = com.example.util.Helpers.formatWithCurrency(totalInSar, "ريال سعودي")
                                }
                                if (selectedShareCategory == "الكل" || selectedShareCategory == "المشتريات") {
                                    pdfTotals["إجمالي مشتريات بالريال اليمني"] = com.example.util.Helpers.formatWithCurrency(totalOutBuyYer, "ريال يمني")
                                    pdfTotals["إجمالي مشتريات بالريال السعودي"] = com.example.util.Helpers.formatWithCurrency(totalOutBuySar, "ريال سعودي")
                                }
                                if (selectedShareCategory == "الكل" || selectedShareCategory == "المصروفات") {
                                    pdfTotals["إجمالي مصروفات بالريال اليمني"] = com.example.util.Helpers.formatWithCurrency(totalOutExpYer, "ريال يمني")
                                    pdfTotals["إجمالي مصروفات بالريال السعودي"] = com.example.util.Helpers.formatWithCurrency(totalOutExpSar, "ريال سعودي")
                                }
                                if (selectedShareCategory == "الكل" || selectedShareCategory == "الحوالات") {
                                    pdfTotals["إجمالي حوالات بالريال اليمني"] = com.example.util.Helpers.formatWithCurrency(totalTransfersYer, "ريال يمني")
                                    pdfTotals["إجمالي حوالات بالريال السعودي"] = com.example.util.Helpers.formatWithCurrency(totalTransfersSar, "ريال سعودي")
                                }
                                if (selectedShareCategory == "الكل" || selectedShareCategory == "الديون") {
                                    pdfTotals["إجمالي ديون العملاء بالريال اليمني"] = com.example.util.Helpers.formatWithCurrency(totalCustomerDebtsYer, "ريال يمني")
                                    pdfTotals["إجمالي ديون العملاء بالريال السعودي"] = com.example.util.Helpers.formatWithCurrency(totalCustomerDebtsSar, "ريال سعودي")
                                    pdfTotals["إجمالي ديون الموردين بالريال اليمني"] = com.example.util.Helpers.formatWithCurrency(totalSupplierDebtsYer, "ريال يمني")
                                    pdfTotals["إجمالي ديون الموردين بالريال السعودي"] = com.example.util.Helpers.formatWithCurrency(totalSupplierDebtsSar, "ريال سعودي")
                                }
                                if (selectedShareCategory == "الكل" || selectedShareCategory == "التحصيلات") {
                                    pdfTotals["إجمالي التحصيلات بالريال اليمني"] = com.example.util.Helpers.formatWithCurrency(totalCollectionsYer, "ريال يمني")
                                    pdfTotals["إجمالي التحصيلات بالريال السعودي"] = com.example.util.Helpers.formatWithCurrency(totalCollectionsSar, "ريال سعودي")
                                }
                                com.example.util.Helpers.generatePdfAndShare(
                                    context = context,
                                    title = titlePdf,
                                    headers = headers,
                                    rows = pdfRows,
                                    totals = pdfTotals,
                                    customDate = if (selectedSharePeriod == "تقويم مخصص") customSharePrintDate else com.example.util.Helpers.getCurrentDateTime(),
                                    reportPeriod = if (selectedSharePeriod == "تقويم مخصص") {
                                        val displayType = when (customShareType) {
                                            "date" -> "يوم"
                                            "month" -> "شهر"
                                            "year" -> "سنة"
                                            else -> customShareType
                                        }
                                        "$displayType : $customShareValue"
                                    } else {
                                        selectedSharePeriod
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PalRedNormal),
                            modifier = Modifier.weight(1.3f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("تصدير PDF 📄", color = Color.White, fontSize = 9.sp)
                        }

                        Button(
                            onClick = {
                                com.example.util.Helpers.shareViaWhatsApp(context, "", shareMsgText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("واتساب 💬", color = Color.White, fontSize = 9.sp)
                        }

                        Button(
                            onClick = {
                                bluetoothPrintText = shareMsgText
                                showBluetoothPrintTrigger = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PalGoldCalligraphy),
                            modifier = Modifier.weight(1.1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("طباعة 🖨️", color = Color.White, fontSize = 9.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShareArchiveDialog = false }) {
                    Text("إغلاق", color = PalWhiteMuted)
                }
            },
            containerColor = PalBlackNormal
        )
    }

    if (showBluetoothPrintTrigger) {
        com.example.ui.components.BluetoothPrintDialog(
            receiptText = bluetoothPrintText,
            onDismiss = { showBluetoothPrintTrigger = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val printerManager = remember { BluetoothPrinterManager.getInstance(context) }
    
    val brandingAppName by viewModel.brandingAppName.collectAsState()
    val brandingAgencyName by viewModel.brandingAgencyName.collectAsState()
    val brandingOwnerName by viewModel.brandingOwnerName.collectAsState()
    val brandingLogoEnabled by viewModel.brandingLogoEnabled.collectAsState()
    val brandingQatTypes by viewModel.brandingQatTypes.collectAsState()

    var showBrandingCustomizer by remember { mutableStateOf(false) }

    var showPassDialog by remember { mutableStateOf(false) }
    var inputNewPass by remember { mutableStateOf("") }

    val printers = remember { printerManager.getBondedPrinters() }
    var selectedPrinterName by remember { mutableStateOf(printerManager.connectedDeviceName) }

    val appLockEnabled by viewModel.isAppLockEnabled.collectAsState()

    var localBackupsList by remember { mutableStateOf(emptyList<java.io.File>()) }
    var showBackupRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showBackupDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedBackupForAction by remember { mutableStateOf<java.io.File?>(null) }

    val refreshBackupsList = {
        val backupDir = java.io.File(context.filesDir, "tofan_backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        localBackupsList = backupDir.listFiles { f -> f.isFile && f.name.endsWith(".db") }?.toList()?.sortedByDescending { f -> f.lastModified() } ?: emptyList()
    }

    LaunchedEffect(Unit) {
        refreshBackupsList()
    }

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim_transition")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_rotation"
    )
    val bounceOffsetYAnimFloat by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_bounce"
    )

    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val success = Helpers.restoreDatabase(context, uri)
            if (success) {
                android.widget.Toast.makeText(context, "تم استعادة قاعدة البيانات بنجاح! جارٍ إعادة تشغيل التطبيق لتحديث السجلات.", android.widget.Toast.LENGTH_LONG).show()
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(intent)
                (context as? android.app.Activity)?.finish()
                kotlin.system.exitProcess(0)
            } else {
                android.widget.Toast.makeText(context, "فشل استعادة قاعدة البيانات! تأكد من ملف النسخة الاحتياطية الصحيح.", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    val createDocumentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            try {
                com.example.data.AppDatabase.checkpoint()
                val dbFile = context.getDatabasePath("tofan_al_aqsa_qat_db")
                if (dbFile.exists()) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        dbFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    android.widget.Toast.makeText(context, "تم تصدير قاعدة البيانات وحفظها بنجاح في ملفات الهاتف! 💾📁", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    android.widget.Toast.makeText(context, "فشل: لم يتم العثور على قاعدة البيانات حالياً!", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "فشل تصدير قاعدة البيانات: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    val customBitmap = remember(brandingLogoEnabled) {
        if (brandingLogoEnabled) com.example.util.BrandingManager.getCustomLogoBitmap(context) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات نظام ${brandingAppName.ifEmpty { "وكالة طوفان الأقصى" }}", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = PalWhitePure)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PalBlackNormal)
            )
        },
        containerColor = PalBlackDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // App Logo & Info Header Card (Moving HD Logo & Information)
            // ----------------------------------------------------
            // Section 1: معلومات التطبيق (App Info)
            // ----------------------------------------------------
            Text(
                text = "معلومات التطبيق 📋",
                color = PalGoldCalligraphy,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                border = BorderStroke(1.2.dp, PalGoldCalligraphy.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (customBitmap != null) {
                            Image(
                                bitmap = customBitmap.asImageBitmap(),
                                contentDescription = "شعار مخصص",
                                modifier = Modifier
                                    .size(90.dp)
                                    .graphicsLayer(
                                        scaleX = scaleAnim,
                                        scaleY = scaleAnim,
                                        rotationZ = rotationAnim
                                    )
                                    .offset(y = bounceOffsetYAnimFloat.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.5.dp, PalGoldCalligraphy, RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.logo_al_aqsa),
                                contentDescription = "الشعار الافتراضي",
                                modifier = Modifier
                                    .size(90.dp)
                                    .graphicsLayer(
                                        scaleX = scaleAnim,
                                        scaleY = scaleAnim,
                                        rotationZ = rotationAnim
                                    )
                                    .offset(y = bounceOffsetYAnimFloat.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.5.dp, PalGoldCalligraphy, RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Text(
                        text = "نظام ${brandingAppName.ifEmpty { "وكالة طوفان الأقصى" }}",
                        color = PalWhitePure,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "نظام محاسبي وإداري ذكي وراقي متكامل لبيع وتجارة وتصدير أصناف القات الصعدي بجميع أنواعها.",
                        color = PalWhiteSoft,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(
                        color = PalWhiteMuted.copy(0.12f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "v3.5.0 (إصدار HD المتألق)", color = PalGreenLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text(text = "🛡️ رقم إصدار النظام:", color = PalWhiteMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = brandingOwnerName.ifEmpty { "أحمد منصور" }, color = PalWhitePure, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text(text = "👤 المالك وصاحب الوكالة:", color = PalWhiteMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // Section 2: الوكالة والهوية البصرية (Agency & Visual Identity)
            // ----------------------------------------------------
            Text(
                text = "الوكالة والهوية البصرية 🎨",
                color = PalGoldCalligraphy,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                border = BorderStroke(1.2.dp, PalWhiteMuted.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "الهوية البصرية الحالية للوكالة:",
                        color = PalWhitePure,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PalBlackLight.copy(0.4f), RoundedCornerShape(12.dp))
                            .border(0.8.dp, PalWhiteMuted.copy(0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = brandingAppName.ifEmpty { "وكالة طوفان الأقصى" }, color = PalGreenLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "⚙️ اسم التطبيق المعروض:", color = PalWhiteMuted, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = brandingAgencyName.ifEmpty { "وكالة طوفان الأقصى" }, color = PalWhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "⚙️ اسم الوكالة بالكامل:", color = PalWhiteMuted, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = brandingOwnerName.ifEmpty { "أحمد منصور" }, color = PalWhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "⚙️ اسم المالك والوكيل:", color = PalWhiteMuted, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = brandingQatTypes.ifEmpty { "عود صعدي, فراد صعدي" }, color = PalGoldCalligraphy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "⚙️ أصناف القات المحددة:", color = PalWhiteMuted, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (brandingLogoEnabled) "مفعل ومدرج بنشاط ✅" else "شعار طوفان الأقصى الافتراضي 🛡️",
                                color = if (brandingLogoEnabled) PalGreenLight else PalGoldCalligraphy,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "⚙️ حالة شعار الهوية:", color = PalWhiteMuted, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = { showBrandingCustomizer = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = PalWhitePure, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تعديل وضبط الهوية البصرية وشعار الوكالة", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }

            // ----------------------------------------------------
            // Section 3: إعدادات النظام (System Settings)
            // ----------------------------------------------------
            Text(
                text = "إعدادات النظام والأمان والعملات ⚙️",
                color = PalGoldCalligraphy,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                border = BorderStroke(1.2.dp, PalWhiteMuted.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Dark mode option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = PalGoldCalligraphy, modifier = Modifier.size(20.dp))
                            Text("الوضع الداكن (Dark Mode)", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        }
                        Switch(
                            checked = viewModel.isDarkMode.collectAsState().value,
                            onCheckedChange = { viewModel.toggleTheme() },
                            colors = SwitchDefaults.colors(checkedThumbColor = PalGreenLight)
                        )
                    }

                    HorizontalDivider(color = PalWhiteMuted.copy(0.12f), thickness = 1.dp)

                    // Lock app option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PalGoldCalligraphy, modifier = Modifier.size(20.dp))
                            Text("قفل بالتطبيع السري عند الفتح", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        }
                        Switch(
                            checked = appLockEnabled,
                            onCheckedChange = { viewModel.toggleAppLock(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PalGreenLight)
                        )
                    }

                    HorizontalDivider(color = PalWhiteMuted.copy(0.12f), thickness = 1.dp)

                    // Change password setting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPassDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = PalGoldCalligraphy, modifier = Modifier.size(20.dp))
                            Text("تغيير كلمة مرور المدير الأدمن للوكالة", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        }
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = PalWhiteMuted)
                    }

                    HorizontalDivider(color = PalWhiteMuted.copy(0.12f), thickness = 1.dp)

                    // Currency Settings
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = PalGoldCalligraphy, modifier = Modifier.size(20.dp))
                            Text("سعر صرف الريال السعودي مقابل اليمني المعتمد:", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        }

                        val rateVal by viewModel.sarToYerExchangeRate.collectAsState()
                        var rateInput by remember { mutableStateOf(rateVal.toString()) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = rateInput,
                                onValueChange = { rateInput = it },
                                label = { Text("سعر الصرف (مثال: 150)", color = PalWhiteMuted, fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PalGreenLight,
                                    unfocusedBorderColor = PalWhiteMuted.copy(alpha = 0.3f),
                                    focusedTextColor = PalWhitePure,
                                    unfocusedTextColor = PalWhitePure
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val d = rateInput.toDoubleOrNull()
                                    if (d != null && d > 0) {
                                        viewModel.updateExchangeRate(d)
                                        android.widget.Toast.makeText(context, "تم حفظ سعر الصرف المعتمد بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "الرجاء إدخال رقم صحيح!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("حفظ", color = PalWhitePure, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("سعر الصرف الحالي: 1 ريال سعودي = $rateVal ريال يمني كإحصاء مالي", color = PalGoldCalligraphy, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ----------------------------------------------------
            // Section 4: الطباعة الحرارية (Thermal Printing)
            // ----------------------------------------------------
            Text(
                text = "الطباعة الحرارية والفواتير 🖨️",
                color = PalGoldCalligraphy,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                border = BorderStroke(1.2.dp, PalWhiteMuted.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = PalGoldCalligraphy, modifier = Modifier.size(20.dp))
                        Text("اختر طابعة البلوتوث الافتراضية للفواتير:", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }

                    if (printers.isEmpty()) {
                        Text(
                            text = "لا توجد أجهزة متصلة بالبلوتوث مقترنة. يرجى إقران طابعتك من إعدادات الهاتف.",
                            color = PalWhiteMuted,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            printers.forEach { p ->
                                val isSelected = selectedPrinterName.contains(p.first)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) PalGreenDark.copy(0.4f) else Color.Transparent)
                                        .border(if (isSelected) 1.2.dp else 0.5.dp, if (isSelected) PalGreenLight else PalWhiteMuted.copy(0.15f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            val (deviceType, isPrinter) = printerManager.getDeviceTypeNameAndIsPrinter(p.first, p.second)
                                            if (!isPrinter) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "عذراً! الجهاز المختار هو ($deviceType) وليس طابعة حرارية مدعومة.",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                android.widget.Toast.makeText(context, "جاري توصيل الطابعة وتعيينها...", android.widget.Toast.LENGTH_SHORT).show()
                                                printerManager.connectPrinter(p.first, p.second) { success ->
                                                    if (success) {
                                                        selectedPrinterName = "${p.first} (${p.second})"
                                                        android.widget.Toast.makeText(context, "تم ربط وتعيين الطابعة بنجاح كجهاز افتراضي للوكالة!", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(p.first, color = if (isSelected) PalGreenLight else PalWhitePure, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                        Text(p.second, color = PalWhiteMuted, fontSize = 10.sp)
                                    }
                                    if (isSelected) {
                                        Text("طابعتك الافتراضية مفعّلة بنشاط 🖨️", color = PalGreenLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = PalWhiteMuted.copy(0.12f), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Text("حدد عرض ورق الطباعة الافتراضي لإخراج الفاتورة:", color = PalWhiteMuted, fontSize = 12.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { printerManager.savePrinterConfig(58, "") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (printerManager.currentPrinterSize == 58) PalGreenNormal else PalBlackLight
                            ),
                            border = BorderStroke(1.dp, if (printerManager.currentPrinterSize == 58) PalGreenLight else PalWhiteMuted.copy(0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("58mm عريضة لثنائيات المحارير", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { printerManager.savePrinterConfig(80, "") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (printerManager.currentPrinterSize == 80) PalGreenNormal else PalBlackLight
                            ),
                            border = BorderStroke(1.dp, if (printerManager.currentPrinterSize == 80) PalGreenLight else PalWhiteMuted.copy(0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("80mm عريضة للفواتير الثقيلة", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // Section 5: النسخ الاحتياطي (Backup & Restore)
            // ----------------------------------------------------
            Text(
                text = "النسخ الاحتياطي والأرشفة 🔄",
                color = PalGoldCalligraphy,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                border = BorderStroke(1.2.dp, PalWhiteMuted.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = PalGoldCalligraphy, modifier = Modifier.size(20.dp))
                        Text("خيارات مزامنة وأمان وأرشفة السجلات بالكامل:", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                com.example.data.AppDatabase.checkpoint()
                                val backupDir = java.io.File(context.filesDir, "tofan_backups")
                                if (!backupDir.exists()) {
                                    backupDir.mkdirs()
                                }
                                val datePart = Helpers.getCurrentDate().replace("-", "_")
                                val timePart = System.currentTimeMillis()
                                val backupName = "tofan_backup_${datePart}_${timePart}.db"
                                val backupFile = java.io.File(backupDir, backupName)
                                
                                val dbFile = context.getDatabasePath("tofan_al_aqsa_qat_db")
                                try {
                                    if (dbFile.exists()) {
                                        dbFile.inputStream().use { input ->
                                            backupFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        refreshBackupsList()
                                        android.widget.Toast.makeText(context, "تم إنشاء النسخة الاحتياطية بنجاح وحفظها كملف أرشيف!", android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "تأكيد: لم يتم دمج ملف قاعدة البيانات حالاً!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "فشل إنشاء النسخة: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إنشاء نسخة احتياطية محلية جديدة 📁", color = PalWhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val ok = Helpers.backupDatabase(context)
                                if (ok) {
                                    android.widget.Toast.makeText(context, "تم تصدير نسخة احتياطية ومفادرتها سحابياً بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                                } else {
                                    android.widget.Toast.makeText(context, "فشل تصدير ومشاركة النسخة الاحتياطية!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PalBlackLight),
                            border = BorderStroke(1.dp, PalWhiteMuted.copy(0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = PalGoldCalligraphy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مشاركة وتصدير السندات سحابياً 📤", color = PalWhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val dateString = Helpers.getCurrentDate().replace("-", "_")
                                createDocumentLauncher.launch("tofan_backup_$dateString.db")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حفظ نسخة احتياطية في ملفات الهاتف الخارجية 💾", color = PalWhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PalBlackLight),
                            border = BorderStroke(1.dp, PalWhiteMuted.copy(0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = PalGoldCalligraphy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استعادة واستيراد نسخة احتياطية من ملفات الهاتف 📂", color = PalWhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("أرشيف النسخ الاحتياطية السريعة المتوفرة (${localBackupsList.size}):", color = PalWhiteSoft, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    if (localBackupsList.isEmpty()) {
                        Text(
                            text = "لا توجد أي نسخ احتياطية مؤرشفة محلياً حالياً.",
                            color = PalWhiteMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            localBackupsList.forEach { file ->
                                val dateStr = java.text.SimpleDateFormat("yyyy/MM/dd hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified()))
                                val sizeStr = if (file.length() < 1024) "${file.length()} B" else String.format("%.1f KB", file.length() / 1024.0)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = PalBlackLight),
                                    border = BorderStroke(1.dp, PalWhiteMuted.copy(0.12f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "نسخة احتياطية: ${file.name.substringBeforeLast(".db")}", color = PalWhitePure, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = "تاريخ الحفظ: $dateStr", color = PalWhiteMuted, fontSize = 9.5.sp)
                                                Text(text = "حجم الملف: $sizeStr", color = PalGreenLight, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Button(
                                                    onClick = {
                                                        selectedBackupForAction = file
                                                        showBackupRestoreConfirmDialog = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = PalGreenDark),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("استعادة 🔄", fontSize = 10.5.sp, color = PalWhitePure, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = {
                                                        selectedBackupForAction = file
                                                        showBackupDeleteConfirmDialog = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = PalRedNormal),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("حذف 🗑️", fontSize = 10.5.sp, color = PalWhitePure, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // Section 6: المطور والدعم الفني (Developer & Tech Support)
            // ----------------------------------------------------
            Text(
                text = "المطور والدعم الفني للنظام 💻",
                color = PalGoldCalligraphy,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(PalGoldCalligraphy, PalGreenLight.copy(alpha = 0.8f))
                    )
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Profile custom glowing avatar with rotating/sparking gradient
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(PalBlackLight)
                            .border(
                                width = 2.5.dp,
                                brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                    colors = listOf(PalGoldCalligraphy, PalGreenLight, PalGoldCalligraphy)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(PalBlackDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = PalGoldCalligraphy,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "المهندس/ محمد أمين ردمان الحاج",
                            color = PalWhitePure,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.5.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Eng. Mohammed Amin Redman Al-Haj",
                            color = PalGoldCalligraphy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .background(PalGoldCalligraphy.copy(alpha = 0.12f), RoundedCornerShape(50))
                                .border(1.dp, PalGoldCalligraphy.copy(alpha = 0.35f), RoundedCornerShape(50))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "مستشار ومطور البرمجيات وحلول الأعمال الرقمية",
                                color = PalGreenLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    Text(
                        text = "منشئ ومصمم وبناء وتطوير التطبيق المحاسبي والإداري والمالي المتكامل لـ ${brandingAgencyName.ifEmpty { "وكالة طوفان الأقصى" }} لتجارة وتوريد أصناف القات الصعدي (${brandingQatTypes.ifEmpty { "عود صعدي, فراد صعدي" }}) بجميع أنواعها.",
                        color = PalWhiteSoft,
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(
                        color = PalWhiteMuted.copy(0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Unified Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Phone direct call
                        Button(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_DIAL,
                                        android.net.Uri.parse("tel:+967780961823")
                                    ).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "تعذر إجراء الاتصال تلقائياً، يرجى الاتصال بالرقم: +967780961823",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PalBlackLight),
                            border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "اتصال مباشر",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اتصال مباشر",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // WhatsApp chat
                        Button(
                            onClick = {
                                val whatsappUrl = "https://wa.me/967780961823"
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(whatsappUrl)
                                    ).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "تعذر فتح الواتساب تلقائياً، الرقم هو: +967780961823",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "مراسلة واتساب",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مراسلة واتساب",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showLogoutConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PalRedNormal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الخروج من النظام المحاسبي", color = PalWhitePure, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            if (showBrandingCustomizer) {
                BrandingDialog(
                    viewModel = viewModel,
                    onDismiss = { showBrandingCustomizer = false }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "تأكّيد تسجيل الخروج",
                    color = PalWhitePure,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في تسجيل الخروج خارج نظام وكالة طوفان الأقصى المحاسبي؟",
                    color = PalWhiteSoft,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PalRedNormal),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("نعم، خروج", color = PalWhitePure, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutConfirmDialog = false }
                ) {
                    Text("إلغاء", color = PalWhiteMuted, fontWeight = FontWeight.Normal)
                }
            },
            containerColor = PalBlackNormal
        )
    }



    if (showPassDialog) {
        AlertDialog(
            onDismissRequest = { showPassDialog = false },
            title = { Text("تغيير كلمة المرور", color = PalWhitePure, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
            text = {
                OutlinedTextField(
                    value = inputNewPass,
                    onValueChange = { inputNewPass = it },
                    label = { Text("أدخل كلمة المرور الجديدة للأدمن", color = PalWhiteMuted) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PalGreenLight, unfocusedBorderColor = PalBlackLight),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputNewPass.trim().isNotEmpty()) {
                            viewModel.changeAdminPassword(inputNewPass)
                            android.widget.Toast.makeText(context, "تم تحديث كلمة المرور الجديدة بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        showPassDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal)
                ) {
                    Text("تأكيد وحفظ", color = PalWhitePure)
                }
            },
            containerColor = PalBlackNormal
        )
    }

    if (showBackupRestoreConfirmDialog && selectedBackupForAction != null) {
        AlertDialog(
            onDismissRequest = { showBackupRestoreConfirmDialog = false },
            title = {
                Text(
                    text = "تأكيد استعادة قاعدة البيانات 🔄",
                    color = PalWhitePure,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في استعادة هذا الأرشيف بالكامل؟ سيتم استبدال قاعدة البيانات الحالية النشطة بالكامل ببيانات هذه النسخة، وسيتم إعادة تشغيل التطبيق تلقائياً لتطبيق التغييرات بشكل صحيح وقاطع.",
                    color = PalWhiteSoft,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = selectedBackupForAction!!
                        val dbFile = context.getDatabasePath("tofan_al_aqsa_qat_db")
                        val dbWal = java.io.File(dbFile.path + "-wal")
                        val dbShm = java.io.File(dbFile.path + "-shm")
                        try {
                            // Safely close active Room connection and reset singleton instance
                            try {
                                com.example.data.AppDatabase.closeDatabase()
                            } catch(ex: Exception) {
                                // ignore
                            }

                            val tempFile = java.io.File.createTempFile("db_restore", null, context.cacheDir)
                            file.inputStream().use { input ->
                                tempFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }

                            if (!tempFile.exists() || tempFile.length() == 0L) {
                                throw Exception("فشل قراءة ملف النسخة الاحتياطية أو الملف فارغ")
                            }

                            if (dbFile.exists()) dbFile.delete()
                            if (dbWal.exists()) dbWal.delete()
                            if (dbShm.exists()) dbShm.delete()

                            tempFile.copyTo(dbFile, overwrite = true)
                            tempFile.delete()
                            
                            android.widget.Toast.makeText(context, "تمت استعادة النسخة بنجاح! جاري جلب ومزامنة أحدث السجلات والمشتريات وإعادة تشغيل النظام المحاسبي...", android.widget.Toast.LENGTH_LONG).show()
                            
                            // Re-launch App clean
                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            context.startActivity(intent)
                            (context as? android.app.Activity)?.finish()
                            java.lang.Runtime.getRuntime().exit(0)
                        } catch(e: Exception) {
                            android.widget.Toast.makeText(context, "فشل استعادة قاعدة البيانات: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                        showBackupRestoreConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("استعادة وإعادة التشغيل 🔄", color = PalWhitePure, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBackupRestoreConfirmDialog = false }
                ) {
                    Text("إلغاء", color = PalWhiteMuted, fontWeight = FontWeight.Normal)
                }
            },
            containerColor = PalBlackNormal
        )
    }

    if (showBackupDeleteConfirmDialog && selectedBackupForAction != null) {
        AlertDialog(
            onDismissRequest = { showBackupDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "تأكيد حذف النسخة الاحتياطية 🗑️",
                    color = PalWhitePure,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف ملف هذه النسخة الاحتياطية نهائياً من أرشيف التطبيق؟ لا يمكن التراجع عن هذه العملية.",
                    color = PalWhiteSoft,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = selectedBackupForAction!!
                        if (file.exists()) {
                            file.delete()
                            refreshBackupsList()
                            android.widget.Toast.makeText(context, "تم حذف ملف النسخة الاحتياطية بنجاح بنسق نهائي! 🗑️", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        showBackupDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PalRedNormal),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تأكيد الحذف 🗑️", color = PalWhitePure, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBackupDeleteConfirmDialog = false }
                ) {
                    Text("إلغاء", color = PalWhiteMuted, fontWeight = FontWeight.Normal)
                }
            },
            containerColor = PalBlackNormal
        )
    }
}
