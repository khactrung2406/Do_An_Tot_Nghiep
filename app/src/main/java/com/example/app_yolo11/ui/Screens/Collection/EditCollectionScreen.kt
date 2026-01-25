package com.example.app_yolo11.ui.Screens.Collection

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale

private val OceanGradientStart = Color(0xFF4FC3F7)
private val OceanGradientEnd = Color(0xFF006994)
private val OceanSurface = Color(0xFFF0F4F8)
private val TextPrimary = Color(0xFF102A43)
private val TextSecondary = Color(0xFF627D98)

private val MainGradientBrush = Brush.linearGradient(
    colors = listOf(OceanGradientStart, OceanGradientEnd),
    tileMode = TileMode.Clamp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCollectionScreen(
    itemId: String,
    navController: NavHostController,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val itemToEdit = items.find { it.id == itemId }
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    var name by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var existingImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var isInitialized by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(itemToEdit) {
        if (itemToEdit != null && !isInitialized) {
            name = itemToEdit.name
            scientificName = itemToEdit.scientificName ?: ""
            location = itemToEdit.location ?: ""
            description = itemToEdit.description ?: ""
            selectedDate = itemToEdit.collectedDate ?: System.currentTimeMillis()
            existingImageUrls = itemToEdit.images
            isInitialized = true
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        newImageUris = newImageUris + uris
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("Chọn", color = OceanGradientEnd) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = OceanGradientEnd,
                    todayDateBorderColor = OceanGradientEnd,
                    todayContentColor = OceanGradientEnd
                )
            )
        }
    }

    Scaffold(
        containerColor = OceanSurface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Chỉnh sửa",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = OceanSurface)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(elevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(
                            if (!isLoading) MainGradientBrush
                            else Brush.linearGradient(listOf(Color.Gray, Color.Gray))
                        )
                        .clickable(enabled = !isLoading) {
                            if (name.isBlank() || (existingImageUrls.isEmpty() && newImageUris.isEmpty())) {
                                Toast.makeText(context, "Vui lòng nhập tên và giữ ít nhất 1 ảnh", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.updateCollection(
                                    itemId, name, scientificName, description, selectedDate, location, existingImageUrls, newImageUris,
                                    onSuccess = {
                                        Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    },
                                    onError = { Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_LONG).show() }
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            "Lưu thay đổi",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (itemToEdit == null && !isInitialized) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OceanGradientEnd)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Hình ảnh mẫu vật *",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        item {
                            Card(
                                onClick = {
                                    photoLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .size(110.dp)
                                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, OceanGradientStart.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = OceanGradientEnd,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Thêm ảnh",
                                        fontSize = 12.sp,
                                        color = OceanGradientEnd,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        items(existingImageUrls) { url ->
                            EditableImageItem(
                                model = url,
                                onDelete = { existingImageUrls = existingImageUrls - url }
                            )
                        }

                        items(newImageUris) { uri ->
                            EditableImageItem(
                                model = uri,
                                onDelete = { newImageUris = newImageUris - uri }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Thông tin chi tiết",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )

                    ModernEditInput(
                        value = name,
                        onValueChange = { name = it },
                        label = "Tên loài *",
                        icon = Icons.Default.Edit
                    )

                    ModernEditInput(
                        value = scientificName,
                        onValueChange = { scientificName = it },
                        label = "Tên khoa học",
                        icon = Icons.Default.Science
                    )

                    ModernEditInput(
                        value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate),
                        onValueChange = {},
                        label = "Ngày thu thập",
                        icon = Icons.Default.CalendarToday,
                        readOnly = true,
                        onClick = { showDatePicker = true }
                    )

                    ModernEditInput(
                        value = location,
                        onValueChange = { location = it },
                        label = "Địa điểm",
                        icon = Icons.Default.LocationOn
                    )

                    ModernEditInput(
                        value = description,
                        onValueChange = { description = it },
                        label = "Ghi chú",
                        icon = Icons.Default.Description,
                        isSingleLine = false,
                        height = 120.dp
                    )
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun EditableImageItem(model: Any, onDelete: () -> Unit) {
    Box(modifier = Modifier.size(110.dp)) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 6.dp, end = 6.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            AsyncImage(
                model = model,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(Color(0xFFFF5252), CircleShape)
                .border(1.dp, Color.White, CircleShape)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun ModernEditInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    isSingleLine: Boolean = true,
    height: androidx.compose.ui.unit.Dp? = null
) {
    val modifier = Modifier.fillMaxWidth()
        .then(if (height != null) Modifier.height(height) else Modifier)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = TextSecondary) },
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        enabled = onClick == null || !readOnly,
        readOnly = readOnly,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OceanGradientEnd,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedLabelColor = OceanGradientEnd,
            unfocusedLabelColor = TextSecondary,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = isSingleLine,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        )
    )

    if (onClick != null) {
        Box(
            modifier = Modifier

                .clickable { onClick() }
        )
    }
}