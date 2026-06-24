package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.QatRepository
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.Screen
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var mainViewModel: AppViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Databases, Repositories, and ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = QatRepository(database)
        val viewModelFactory = AppViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[AppViewModel::class.java]
        mainViewModel = viewModel

        // Load contacts only if permission is already granted (non-blocking)
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.importContactsFromPhone()
        }

        enableEdgeToEdge()
        
        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(darkTheme = isDark) {
                // Enforce RTL Layout Direction for flawless Arabic rendering across all locales
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    if (currentScreen != Screen.Dashboard && currentScreen != Screen.Splash && currentScreen != Screen.Login) {
                        BackHandler {
                            viewModel.navigateToDashboard()
                        }
                    }

                    val navigateWithDrawer: (Screen) -> Unit = { screen ->
                        scope.launch { drawerState.close() }
                        viewModel.navigateTo(screen)
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = drawerState.isOpen || (currentScreen != Screen.Splash && currentScreen != Screen.Login),
                        drawerContent = {
                            if (drawerState.isOpen || (currentScreen != Screen.Splash && currentScreen != Screen.Login)) {
                                DrawerContent(
                                    currentScreen = currentScreen,
                                    onNavigate = navigateWithDrawer,
                                    viewModel = viewModel,
                                    onLogout = {
                                        scope.launch {
                                            drawerState.close()
                                            kotlinx.coroutines.delay(400)
                                            viewModel.logout()
                                        }
                                    }
                                )
                            }
                        }
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = Color(0xFF040C06)
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .widthIn(max = 900.dp)
                                        .fillMaxWidth()
                                        .background(Color(0xFF0C130D))
                                ) {
                                    Crossfade(
                                        targetState = currentScreen,
                                        modifier = Modifier.fillMaxSize(),
                                        label = "screen_navigation"
                                    ) { screen ->
                                        when (screen) {
                                            is Screen.Splash -> SplashScreen(viewModel)
                                            is Screen.Login -> LoginScreen(viewModel)
                                            is Screen.Dashboard -> DashboardScreen(
                                                viewModel = viewModel,
                                                onMenuClick = {
                                                    scope.launch { drawerState.open() }
                                                }
                                            )
                                            is Screen.Inventory -> InventoryScreen(viewModel)
                                            is Screen.Sales -> SalesScreen(viewModel)
                                            is Screen.Accounts -> AccountsScreen(viewModel)
                                            is Screen.Customers -> CustomerScreen(viewModel)
                                            is Screen.Suppliers -> SupplierScreen(viewModel)
                                            is Screen.Expenses -> ExpensesScreen(viewModel)
                                            is Screen.Transfers -> TransfersScreen(viewModel)
                                            is Screen.Reports -> ReportsScreen(viewModel)
                                            is Screen.Archive -> ArchiveScreen(viewModel)
                                            is Screen.Settings -> SettingsScreen(viewModel)
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
}

@Composable
fun DrawerContent(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    viewModel: AppViewModel,
    onLogout: () -> Unit
) {
    val bAppName by viewModel.brandingAppName.collectAsState()
    val bOwnerName by viewModel.brandingOwnerName.collectAsState()

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0C130D), // Deep rich forest dark-green matching PalBlackDark
        drawerContentColor = Color.White,
        modifier = Modifier.width(310.dp)
    ) {
        // Drawer Header with Palestinian flag & badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF040C06))
                .padding(top = 40.dp, bottom = 20.dp)
                .padding(horizontal = 24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(1.2.dp, Color(0xFFD4AF37), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AgencyLogoBadge()
                    }
                    Column {
                        Text(
                            text = bAppName.ifEmpty { "وكالة طوفان الأقصى" },
                            color = Color(0xFFD4AF37), // Luxury gold
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = bOwnerName.ifEmpty { "أحمد منصور" },
                            color = Color.LightGray.copy(0.8f),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WavingPalestineFlag(
                        modifier = Modifier
                            .size(70.dp, 44.dp)
                    )
                    Text(
                        text = "نظام الإدارة والتحاسب المتكامل 🇾🇪🇵🇸",
                        color = Color(0xFF10b981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        @Composable
        fun DrawerCategoryHeader(title: String) {
            Text(
                text = title,
                color = Color(0xFFD4AF37).copy(0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        @Composable
        fun DrawerItemCustom(
            title: String,
            icon: ImageVector,
            target: Screen,
            activeColor: Color = Color(0xFF10b981)
        ) {
            val isSelected = currentScreen::class == target::class
            val bg = if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent
            val border = if (isSelected) BorderStroke(1.dp, activeColor.copy(alpha = 0.3f)) else null
            val textColor = if (isSelected) activeColor else Color.White

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bg),
                border = border,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable { onNavigate(target) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }

        // Scrollable menu contents
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            DrawerCategoryHeader("المنصة والتحليلات")
            DrawerItemCustom("لوحة الأداء الحسابي", Icons.Default.Dashboard, Screen.Dashboard, Color(0xFF10b981))

            Spacer(modifier = Modifier.height(8.dp))
            DrawerCategoryHeader("المقاصة والمبيعات")
            DrawerItemCustom("فاتورة ومبيعات جديدة", Icons.Default.AddShoppingCart, Screen.Sales, Color(0xFF10b981))
            DrawerItemCustom("التحاسب وجرد الحسابات", Icons.Default.BarChart, Screen.Accounts, Color(0xFF3B82F6))
            DrawerItemCustom("سجل الحوالات والتحويلات", Icons.Default.CompareArrows, Screen.Transfers, Color(0xFF00ADFF))
            DrawerItemCustom("دليل وحسابات العملاء", Icons.Default.People, Screen.Customers, Color(0xFFF97316))

            Spacer(modifier = Modifier.height(8.dp))
            DrawerCategoryHeader("المخازن والتوريد")
            DrawerItemCustom("إدارة مخزون القات", Icons.Default.Layers, Screen.Inventory, Color(0xFF3B82F6))
            DrawerItemCustom("كشف الموردين والشراء", Icons.Default.LocalShipping, Screen.Suppliers, Color(0xFF8B5CF6))
            DrawerItemCustom("المصروفات والمصاريف اليومية", Icons.Default.Payments, Screen.Expenses, Color(0xFFF43F5E))

            Spacer(modifier = Modifier.height(8.dp))
            DrawerCategoryHeader("التقارير وسجلات النظام")
            DrawerItemCustom("التقارير الحسابية المتقدمة", Icons.Default.Assessment, Screen.Reports, Color(0xFFD4AF37))
            DrawerItemCustom("الأرشيف الكلي للسجلات", Icons.Default.FolderOpen, Screen.Archive, Color(0xFF06B6D4))

            Spacer(modifier = Modifier.height(8.dp))
            DrawerCategoryHeader("التهيئة والدعم")
            DrawerItemCustom("إعدادات الوكالة والتطبيق", Icons.Default.Settings, Screen.Settings, Color(0xFF94A3B8))

            Spacer(modifier = Modifier.height(8.dp))
            DrawerCategoryHeader("إغلاق الجلسة والأمان")

            var showLogoutDialog by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable { showLogoutDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "تسجيل الخروج",
                        tint = Color(0xFFF43F5E), // Rose red for logout
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "تسجيل الخروج من النظام",
                        color = Color(0xFFF43F5E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "تأكيد تسجيل الخروج",
                                color = PalWhitePure,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Right
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PalRedNormal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    tint = PalRedNormal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    text = {
                        Text(
                            text = "هل أنت متأكد من رغبتك في تسجيل الخروج من النظام؟ سيتم إغلاق جلستك الحالية لحماية أمان بياناتك الخاصة بالوكالة.",
                            color = PalWhiteSoft,
                            fontSize = 13.5.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PalRedNormal,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("تسجيل الخروج", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showLogoutDialog = false },
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("إلغاء", color = PalWhiteMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    containerColor = PalBlackNormal,
                    modifier = Modifier.border(1.2.dp, PalRedNormal.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                )
            }
        }
    }
}
