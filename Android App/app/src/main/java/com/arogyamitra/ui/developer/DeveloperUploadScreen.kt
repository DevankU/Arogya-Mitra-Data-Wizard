package com.arogyamitra.ui.developer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arogyamitra.ui.theme.ArogyaPrimary
import com.arogyamitra.ui.theme.MarketSurfaceDark
import androidx.compose.foundation.background

@Composable
fun DeveloperUploadScreen(
    isUploading: Boolean,
    uploadSuccess: Boolean,
    onUpload: (String, String, String, String, List<String>) -> Unit,
    onReset: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            // Extract filename from URI
            selectedFileName = it.lastPathSegment?.substringAfterLast("/") ?: "Selected file"
        }
    }
    
    // Reset fields on success
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            name = ""
            description = ""
            price = ""
            selectedFileUri = null
            selectedFileName = null
            onReset()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Handle Back if needed */ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = "Developer Hub",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.size(48.dp)) // Balance
            }
        }

        // Upload Zone with File Picker
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(
                        width = 2.dp,
                        color = if (selectedFileUri != null) ArogyaPrimary else ArogyaPrimary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .background(
                        if (selectedFileUri != null) 
                            ArogyaPrimary.copy(alpha = 0.1f) 
                        else 
                            MarketSurfaceDark.copy(alpha = 0.5f)
                    )
                    .clickable { 
                        filePickerLauncher.launch(arrayOf("*/*"))
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectedFileUri != null) {
                        // File selected state
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ArogyaPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = ArogyaPrimary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("File Selected", color = ArogyaPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(selectedFileName ?: "Unknown file", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to change file", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    } else {
                        // No file selected
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ArogyaPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = ArogyaPrimary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Upload your LoRA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Tap to browse files", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }

        // Form Title
        item {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ArogyaPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adapter Details", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Fields
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                
                // Name
                InputLabel("Adapter Name")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g., Cardio-v2-Llama", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputColors(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Model (Mock Select)
                InputLabel("Base Model Compatibility")
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = { selectedModel = it },
                    placeholder = { Text("Select Model (e.g. Llama 3)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputColors(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Price
                InputLabel("Price")
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    placeholder = { Text("0.00", color = Color.Gray) },
                    leadingIcon = { Text("₹", color = ArogyaPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    suffix = { Text("INR", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputColors(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                InputLabel("Description")
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Describe the medical dataset...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = inputColors(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Publish Button
        item {
            Button(
                onClick = { 
                    onUpload(name, description, price, selectedModel, listOf("Medical", "AI")) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ArogyaPrimary),
                enabled = !isUploading && name.isNotEmpty()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text("Publish to Marketplace", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.RocketLaunch, null, tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun InputLabel(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ArogyaPrimary,
    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
    focusedContainerColor = MarketSurfaceDark.copy(alpha = 0.6f),
    unfocusedContainerColor = MarketSurfaceDark.copy(alpha = 0.6f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = ArogyaPrimary
)
