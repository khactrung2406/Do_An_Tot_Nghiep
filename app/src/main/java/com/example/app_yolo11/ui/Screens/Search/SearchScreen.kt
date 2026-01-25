package com.example.app_yolo11.ui.Screens.Search

import AppHeaderLogo // Giữ nguyên import logo của bạn
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app_yolo11.model.SeaSnails

val OceanGradientStart = Color(0xFF4FC3F7)
val OceanGradientEnd = Color(0xFF006994)
val OceanSurface = Color(0xFFF0F4F8)
val TextPrimary = Color(0xFF102A43)
val TextSecondary = Color(0xFF627D98)

val MainGradientBrush = Brush.linearGradient(
    colors = listOf(OceanGradientStart, OceanGradientEnd),
    tileMode = TileMode.Clamp
)

@Composable
fun SearchScreen(navController: NavHostController, viewModel: SearchViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current
    val suggestionChips = listOf("Ốc hương", "Ốc anh vũ", "Ốc hoàng hậu", "Ốc giấy", "Ốc bàn tay")

    Scaffold(
        containerColor = OceanSurface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))


            Box(modifier = Modifier.padding(vertical = 10.dp)) {
                AppHeaderLogo()
            }

            Spacer(modifier = Modifier.height(16.dp))

            ModernSearchBar(
                query = query,
                onQueryChanged = { newQuery ->
                    viewModel.updateQuery(newQuery)
                    viewModel.searchSnails()
                },
                onClearQuery = {
                    viewModel.clearSearch()
                    focusManager.clearFocus()
                },
                onSearchAction = {
                    focusManager.clearFocus()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))


            if (query.isEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Gợi ý phổ biến",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestionChips) { chip ->
                            ModernSuggestionChip(
                                text = chip,
                                onClick = {
                                    viewModel.updateQuery(chip)
                                    viewModel.searchSnails()
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OceanGradientEnd)
                }
            } else {
                ModernSearchResultList(
                    items = searchResults,
                    navController = navController,
                    isSearching = query.isNotEmpty(),
                    viewModel = viewModel
                )
            }
        }
    }
}


@Composable
fun ModernSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSearchAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, CircleShape, spotColor = OceanGradientEnd.copy(alpha = 0.25f))
            .background(Color.White, CircleShape)
            .border(1.dp, OceanGradientStart.copy(alpha = 0.3f), CircleShape)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = OceanGradientEnd,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Nhập tên loài ốc...", color = Color.LightGray, fontSize = 15.sp)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = OceanGradientEnd,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchAction() })
            )

            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ModernSuggestionChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFE1F5FE)) // Xanh dương cực nhạt
            .border(1.dp, Color(0xFFB3E5FC), CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = OceanGradientEnd,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}


@Composable
fun ModernSearchResultList(
    items: List<SeaSnails>,
    navController: NavHostController,
    isSearching: Boolean,
    viewModel: SearchViewModel
) {
    Column(modifier = Modifier.animateContentSize()) {
        if (isSearching) {
            Text(
                text = if (items.isEmpty()) "Không tìm thấy kết quả" else "Kết quả tìm kiếm (${items.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, start = 4.dp)
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (items.isEmpty() && isSearching) {
                item { ModernEmptyState() }
            } else {
                items(items) { item ->
                    ModernResultItem(
                        snail = item,
                        onItemClick = { id ->
                            viewModel.addToHistory(item)
                            navController.navigate("detail_screen/$id")
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ModernResultItem(snail: SeaSnails, onItemClick: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color.LightGray.copy(alpha = 0.3f))
            .clickable { onItemClick(snail.id) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(70.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (snail.imageUrl.isNotEmpty()) {

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(snail.imageUrl).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().matchParentSize(),
                            alpha = 0.3f
                        )

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(snail.imageUrl).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                            error = rememberVectorPainter(Icons.Default.ImageNotSupported),
                            placeholder = rememberVectorPainter(Icons.Default.Waves)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFFE1F5FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Waves, contentDescription = null, tint = OceanGradientStart)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = snail.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(OceanGradientEnd, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = snail.scientificName,
                        fontSize = 14.sp,
                        color = OceanGradientEnd,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@Composable
fun ModernEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(OceanSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.LightGray.copy(alpha = 0.8f),
                modifier = Modifier.size(50.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Không tìm thấy loài nào",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "Hãy thử tìm kiếm với từ khóa khác",
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}