package com.example.app_yolo11.ui.Screens.ChatBot

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.app_yolo11.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

val OceanBlueDark = Color(0xFF006994)
val ChatBubbleUser = OceanBlueDark
val ChatBubbleBot = Color(0xFFEEEEEE)
val BackgroundColor = Color(0xFFF5F9FF)

suspend fun callN8nWebhook(userMessage: String, sessionId: String): String {
    return withContext(Dispatchers.IO) {
        val n8nUrl ="https://promotive-tolerantly-annamae.ngrok-free.dev/webhook/chatbotYOLO11"

        var attempt = 1
        val maxRetries = 3
        var finalResult = "Xin lỗi, kết nối không ổn định. Vui lòng thử lại."

        while (attempt <= maxRetries) {
            try {
                val url = URL(n8nUrl)
                val conn = url.openConnection() as HttpURLConnection

                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "ChatBotApp")
                conn.setRequestProperty("ngrok-skip-browser-warning", "true")
                conn.doOutput = true

                val jsonBody = JSONObject()
                jsonBody.put("message", userMessage)
                jsonBody.put("chatInput", userMessage)
                jsonBody.put("sessionId", sessionId)

                conn.outputStream.use { it.write(jsonBody.toString().toByteArray()) }

                val responseCode = conn.responseCode
                val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
                val responseRaw = stream?.bufferedReader()?.use { it.readText() } ?: ""

                if (responseRaw.isBlank()) {
                    throw Exception("Empty Response")
                }

                try {
                    if (responseRaw.trim().startsWith("[")) {
                        val jsonArray = JSONArray(responseRaw)
                        if (jsonArray.length() > 0) {
                            val firstItem = jsonArray.getJSONObject(0)
                            if (firstItem.has("output")) return@withContext firstItem.getString("output")
                            if (firstItem.has("reply")) return@withContext firstItem.getString("reply")
                            if (firstItem.has("text")) return@withContext firstItem.getString("text")
                        }
                    }

                    val jsonResponse = JSONObject(responseRaw)
                    if (jsonResponse.has("reply")) {
                        return@withContext jsonResponse.getString("reply")
                    } else if (jsonResponse.has("output")) {
                        return@withContext jsonResponse.getString("output")
                    } else if (jsonResponse.has("text")) {
                        return@withContext jsonResponse.getString("text")
                    } else {
                        return@withContext responseRaw
                    }
                } catch (e: Exception) {
                    return@withContext responseRaw
                }

            } catch (e: Exception) {
                attempt++
                if (attempt <= maxRetries) {
                    delay(1000)
                }
            }
        }
        finalResult
    }
}

@Composable
fun formatMessage(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            var currentLine = line.trim()
            if (currentLine == "---" || currentLine == "***") return@forEachIndexed

            var isHeader = false
            if (currentLine.startsWith("##")) {
                currentLine = currentLine.replace(Regex("^#+\\s*"), "")
                isHeader = true
            }
            if (currentLine.startsWith("* ") || currentLine.startsWith("- ")) {
                currentLine = "• " + currentLine.substring(2)
            }

            if (isHeader) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                    append(currentLine)
                }
            } else {
                val parts = currentLine.split("**")
                parts.forEachIndexed { i, part ->
                    if (i % 2 == 1) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(part)
                        }
                    } else {
                        append(part)
                    }
                }
            }
            if (index < lines.size - 1) append("\n")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    navController: NavController,
    viewModel: ChatBotViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    LaunchedEffect(messages.size) {
        try {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        } catch (e: Exception) { }
    }

    fun handleSendMessage() {
        if (inputText.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.sendMessage(inputText)
        inputText = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Trợ lý TheSea", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OceanBlueDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = Modifier.imePadding()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundColor)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(msg)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Hỏi về ốc...") },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = BackgroundColor,
                            unfocusedContainerColor = BackgroundColor
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (!isGenerating) handleSendMessage()
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { handleSendMessage() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(OceanBlueDark, CircleShape)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp
                    ))
                    .background(
                        if (message.isError) Color.Red.copy(alpha = 0.1f)
                        else if (isUser) ChatBubbleUser
                        else ChatBubbleBot
                    )
                    .padding(12.dp)
                    .widthIn(max = 280.dp)
            ) {
                if (message.isLoading) {
                    Text("Đang hỏi chuyên gia...", color = Color.Gray, fontSize = 14.sp)
                } else {
                    Text(
                        text = formatMessage(message.text),
                        color = if (message.isError) Color.Red else if (isUser) Color.White else Color.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}