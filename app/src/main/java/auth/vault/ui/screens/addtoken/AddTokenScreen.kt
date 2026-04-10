package auth.vault.ui.screens.addtoken

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import auth.vault.ui.theme.Orange500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTokenScreen(
    onTokenAdded: (String, String, String, String, Int, Long) -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToGallery: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    var serviceLabel by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var secretKey by remember { mutableStateOf("") }
    var selectedAlgorithm by remember { mutableStateOf("SHA1") }
    var algorithmExpanded by remember { mutableStateOf(false) }
    var digitCount by remember { mutableStateOf("6") }
    var timeStep by remember { mutableStateOf("30") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Token", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToQrScanner) {
                        Icon(Icons.Default.QrCode, contentDescription = "Scan QR")
                    }
                    IconButton(onClick = onNavigateToGallery) {
                        Icon(androidx.compose.material.icons.Icons.Default.Image, contentDescription = "Import from Gallery")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = serviceLabel,
                onValueChange = { serviceLabel = it },
                label = { Text("Service Name") },
                placeholder = { Text("e.g. Google, GitHub") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500
                )
            )

            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Email") },
                placeholder = { Text("user@example.com") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500
                )
            )

            OutlinedTextField(
                value = secretKey,
                onValueChange = { secretKey = it },
                label = { Text("Secret Key") },
                placeholder = { Text("JBSWY3DPEHPK3PXP") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500
                )
            )

            ExposedDropdownMenuBox(
                expanded = algorithmExpanded,
                onExpandedChange = { algorithmExpanded = !algorithmExpanded }
            ) {
                OutlinedTextField(
                    value = selectedAlgorithm,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Algorithm") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = algorithmExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Orange500
                    )
                )
                ExposedDropdownMenu(
                    expanded = algorithmExpanded,
                    onDismissRequest = { algorithmExpanded = false }
                ) {
                    listOf("SHA1", "SHA256", "SHA512").forEach { algo ->
                        DropdownMenuItem(
                            text = { Text(algo) },
                            onClick = {
                                selectedAlgorithm = algo
                                algorithmExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = digitCount,
                onValueChange = { if (it.length <= 2) digitCount = it },
                label = { Text("Digits") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500
                )
            )

            OutlinedTextField(
                value = timeStep,
                onValueChange = { if (it.length <= 3) timeStep = it },
                label = { Text("Time Step (seconds)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500
                )
            )

            Button(
                onClick = {
                    if (serviceLabel.isNotBlank() && secretKey.isNotBlank()) {
                        onTokenAdded(
                            serviceLabel.trim(),
                            accountName.trim(),
                            secretKey.trim().replace("\\s+".toRegex(), ""),
                            selectedAlgorithm,
                            digitCount.toIntOrNull() ?: 6,
                            timeStep.toLongOrNull() ?: 30L
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = serviceLabel.isNotBlank() && secretKey.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Orange500)
            ) {
                Text(
                    text = "Add Token",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
