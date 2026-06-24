package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AppViewModel
import com.example.ui.Screen
import com.example.ui.theme.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.testTag

@Composable
fun SplashScreen(viewModel: AppViewModel) {
    val brandingAppName by viewModel.brandingAppName.collectAsState()
    val brandingAgencyName by viewModel.brandingAgencyName.collectAsState()
    val brandingOwnerName by viewModel.brandingOwnerName.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PalBlackDark, PalGreenDark, PalBlackDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "flag_wave")
        val flagOffset by infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Premium styled launcher logo with animated floating offset
            AgencyPremiumLogo(
                logoSize = 170.dp,
                modifier = Modifier.offset(y = flagOffset.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = brandingAppName.ifEmpty { "وكالة طوفان الأقصى" },
                color = PalGoldCalligraphy,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = if (brandingAgencyName.isEmpty()) "لأجود أنواع القات" else "${brandingAgencyName} • المالك: ${brandingOwnerName}",
                color = PalWhiteSoft,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = PalRedLight,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "نظام محاسبي متكامل وإداري ذكي",
                color = PalGreenLight,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
fun LoginScreen(viewModel: AppViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val isPassVisible by viewModel.isPasswordVisible.collectAsState()

    var logoClickCount by remember { mutableStateOf(0) }
    var showSecretPasswordDialog by remember { mutableStateOf(false) }
    var showBrandingDialog by remember { mutableStateOf(false) }
    var showQatTypesDialog by remember { mutableStateOf(false) }

    val brandingAppName by viewModel.brandingAppName.collectAsState()
    val brandingAgencyName by viewModel.brandingAgencyName.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PalBlackDark)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            
            // Premium styled logo badge with touch listener (3 consecutive taps opens visual identity control panel)
            AgencyPremiumLogo(
                logoSize = 125.dp,
                modifier = Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    logoClickCount++
                    if (logoClickCount >= 3) {
                        logoClickCount = 0
                        showSecretPasswordDialog = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "تسجيل الدخول",
                color = PalWhitePure,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Text(
                text = "نظام ${brandingAppName.ifEmpty { "وكالة طوفان الأقصى" }} المحاسبي والإداري",
                color = PalGreenLight,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = PalBlackNormal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = PalRedLight,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Username field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("اسم المستخدم", color = PalWhiteMuted) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PalGreenLight) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PalGreenLight,
                            unfocusedBorderColor = PalBlackLight,
                            focusedTextColor = PalWhitePure,
                            unfocusedTextColor = PalWhiteSoft
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور", color = PalWhiteMuted) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PalGreenLight) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.isPasswordVisible.value = !isPassVisible }) {
                                Icon(
                                    imageVector = if (isPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = PalWhiteMuted
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PalGreenLight,
                            unfocusedBorderColor = PalBlackLight,
                            focusedTextColor = PalWhitePure,
                            unfocusedTextColor = PalWhiteSoft
                        ),
                        singleLine = true,
                        visualTransformation = if (isPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = PalGreenLight)
                        )
                        Text(
                            text = "حفظ بيانات تسجيل الدخول",
                            color = PalWhiteSoft,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val success = viewModel.attemptLogin(username, password, rememberMe)
                            if (!success) {
                                errorMessage = "خطأ في اسم المستخدم أو كلمة المرور!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "دخول",
                            color = PalWhitePure,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Secret password dialog beautifully designed for visual identity control access
    if (showSecretPasswordDialog) {
        var secretPassword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSecretPasswordDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PalGoldCalligraphy.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = PalGoldCalligraphy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "الدخول للوحة الهوية البصرية ⚙️",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Right
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "الرجاء إدخال كلمة مرور المبرمج/المطور للوصول وصيانة وتغيير اسم الوكالة، المالك، الشعار وتهيئاتها البصرية بالكامل.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.5.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = secretPassword,
                        onValueChange = { 
                            secretPassword = it 
                            if (it == "mohmmed") {
                                showSecretPasswordDialog = false
                                showBrandingDialog = true
                            }
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("رمز مرور المطور", color = PalWhiteMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PalWhitePure,
                            unfocusedTextColor = PalWhiteSoft,
                            focusedBorderColor = PalGoldCalligraphy,
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color.Black.copy(0.40f),
                            unfocusedContainerColor = Color.Black.copy(0.20f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (secretPassword == "mohmmed") {
                            showSecretPasswordDialog = false
                            showBrandingDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PalGoldCalligraphy,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("دخول للوحة التحكم", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSecretPasswordDialog = false }) {
                    Text("إلغاء", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F172A),
            modifier = Modifier.border(1.2.dp, PalGoldCalligraphy.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
        )
    }

    // Branding panel dialog
    if (showBrandingDialog) {
        BrandingDialog(
            viewModel = viewModel,
            onDismiss = { showBrandingDialog = false }
        )
    }

    if (showQatTypesDialog) {
        val brandingAppName by viewModel.brandingAppName.collectAsState()
        val brandingAgencyName by viewModel.brandingAgencyName.collectAsState()
        val brandingOwnerName by viewModel.brandingOwnerName.collectAsState()
        val brandingQatTypes by viewModel.brandingQatTypes.collectAsState()

        var tempQatTypes by remember { mutableStateOf(brandingQatTypes.ifEmpty { com.example.util.BrandingManager.DEFAULT_QAT_TYPES }) }

        AlertDialog(
            onDismissRequest = { showQatTypesDialog = false },
            title = {
                Text(
                    text = "تعديل أنواع وأمثلة القات في الفواتير 🍃",
                    color = PalGoldCalligraphy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "قم بإدخال أنواع القات المفصولة بفواصل ليتم اعتمادها وتحديث قائمة أمثلة أنواع القات تلقائياً في قسم إنشاء الفواتير والمبيعات:",
                        color = PalWhiteSoft,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right
                    )
                    OutlinedTextField(
                        value = tempQatTypes,
                        onValueChange = { tempQatTypes = it },
                        label = { Text("أمثلة أنواع القات (مفصولة بفواصل)", color = PalWhiteMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PalWhitePure,
                            unfocusedTextColor = PalWhiteSoft,
                            focusedBorderColor = PalGreenLight,
                            unfocusedBorderColor = PalBlackLight,
                            focusedContainerColor = PalBlackDark,
                            unfocusedContainerColor = PalBlackDark
                        ),
                        singleLine = false,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("config_qat_types_input")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            showQatTypesDialog = false
                            showSecretPasswordDialog = true
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "تغيير اسم الوكالة، اسم المالك أو الشعار ⚙️",
                            color = PalGoldCalligraphy,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            style = androidx.compose.ui.text.TextStyle(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateBranding(
                            newName = brandingAppName.ifEmpty { "وكالة طوفان الأقصى" },
                            newAgency = brandingAgencyName.ifEmpty { "لأجود أنواع القات" },
                            newOwner = brandingOwnerName.ifEmpty { "أحمد منصور" },
                            newQatTypes = tempQatTypes,
                            logoStream = null
                        )
                        showQatTypesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal)
                ) {
                    Text("حفظ وتحديث الأمثلة ✅", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQatTypesDialog = false }) {
                    Text("إلغاء", color = PalWhiteMuted)
                }
            },
            containerColor = PalBlackNormal
        )
    }
}

@Composable
fun BrandingDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val brandingAppName by viewModel.brandingAppName.collectAsState()
    val brandingAgencyName by viewModel.brandingAgencyName.collectAsState()
    val brandingOwnerName by viewModel.brandingOwnerName.collectAsState()
    val brandingQatTypes by viewModel.brandingQatTypes.collectAsState()
    val brandingLogoEnabled by viewModel.brandingLogoEnabled.collectAsState()

    var tempAppName by remember { mutableStateOf(brandingAppName.ifEmpty { "وكالة طوفان الأقصى" }) }
    var tempAgencyName by remember { mutableStateOf(brandingAgencyName.ifEmpty { "وكالة طوفان الأقصى" }) }
    var tempOwnerName by remember { mutableStateOf(brandingOwnerName.ifEmpty { "أحمد منصور" }) }
    var tempQatTypes by remember { mutableStateOf(brandingQatTypes.ifEmpty { com.example.util.BrandingManager.DEFAULT_QAT_TYPES }) }
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val pickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selectedUri = uri
    }

    val previewBitmap = remember(selectedUri, brandingLogoEnabled) {
        if (selectedUri != null) {
            try {
                context.contentResolver.openInputStream(selectedUri!!).use {
                    android.graphics.BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                null
            }
        } else {
            if (brandingLogoEnabled) {
                com.example.util.BrandingManager.getCustomLogoBitmap(context)
            } else null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "إدارة الهوية البصرية والتخصيص",
                color = PalGoldCalligraphy,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        containerColor = PalBlackNormal,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo preview and picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PalBlackDark)
                        .border(1.5.dp, PalGoldCalligraphy, RoundedCornerShape(16.dp))
                        .clickable { pickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "معاينة الشعار المخصص",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "انقر هنا لاختيار صورة شعار جديد من الهاتف 🖼️",
                                color = PalWhiteSoft,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (selectedUri != null) {
                    Text(
                        "تم اختيار شعار مخصص جديد (جاهز للتطبيق)",
                        color = PalGreenLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = tempAppName,
                    onValueChange = { tempAppName = it },
                    label = { Text("اسم التطبيق المخصص", color = PalWhiteMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PalWhitePure,
                        unfocusedTextColor = PalWhiteSoft,
                        focusedBorderColor = PalGreenLight,
                        unfocusedBorderColor = PalBlackLight
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tempAgencyName,
                    onValueChange = { tempAgencyName = it },
                    label = { Text("اسم الوكالة", color = PalWhiteMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PalWhitePure,
                        unfocusedTextColor = PalWhiteSoft,
                        focusedBorderColor = PalGreenLight,
                        unfocusedBorderColor = PalBlackLight
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tempOwnerName,
                    onValueChange = { tempOwnerName = it },
                    label = { Text("اسم المالك المعتمد", color = PalWhiteMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PalWhitePure,
                        unfocusedTextColor = PalWhiteSoft,
                        focusedBorderColor = PalGreenLight,
                        unfocusedBorderColor = PalBlackLight
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tempQatTypes,
                    onValueChange = { tempQatTypes = it },
                    label = { Text("أنواع القات (الكماليات مفصولة بفواصل، مثال: عود صعدي، فراد صعدي)", color = PalWhiteMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PalWhitePure,
                        unfocusedTextColor = PalWhiteSoft,
                        focusedBorderColor = PalGreenLight,
                        unfocusedBorderColor = PalBlackLight
                    ),
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val logoStream = if (selectedUri != null) {
                            try {
                                context.contentResolver.openInputStream(selectedUri!!)
                            } catch (e: Exception) {
                                null
                            }
                        } else null
                        viewModel.updateBranding(tempAppName, tempAgencyName, tempOwnerName, tempQatTypes, logoStream)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PalGreenNormal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ التعديلات وتطبيق الهوية", color = PalWhitePure, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = {
                        viewModel.resetBrandingToDefaults()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PalRedLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("استعادة الهوية الافتراضية", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = PalWhiteSoft)
            ) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AgencyPremiumLogo(
    modifier: Modifier = Modifier,
    logoSize: androidx.compose.ui.unit.Dp = 120.dp
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val logoEnabled = com.example.util.BrandingManager.isLogoEnabled
    val customBitmap = remember(logoEnabled) {
        if (logoEnabled) com.example.util.BrandingManager.getCustomLogoBitmap(context) else null
    }

    Box(
        modifier = modifier
            .size(logoSize)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing radial light scheme matching Palestinian colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PalGreenDark.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Luxury custom geometric framed borders
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.5.dp, PalGoldCalligraphy, RoundedCornerShape(24.dp))
                .padding(4.dp)
                .border(1.dp, PalGreenLight.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .padding(4.dp)
        ) {
            if (customBitmap != null) {
                Image(
                    bitmap = customBitmap.asImageBitmap(),
                    contentDescription = "شعار مخصص",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.logo_al_aqsa),
                    contentDescription = "شعار وكالة طوفان الأقصى",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
