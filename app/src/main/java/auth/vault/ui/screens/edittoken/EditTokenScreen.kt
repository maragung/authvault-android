package auth.vault.ui.screens.edittoken

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
fun EditTokenScreen(
    initialServiceLabel: String,
    initialAccountName: String,
    initialSecretKey: String,
    initialAlgorithm: String,
    initialDigitCount: Int,
    initialTimeStep: Long,
    onSave: (String, String, String, String, Int, Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var serviceLabel by remember { mutableStateOf(initialServiceLabel) }
    var accountName by remember { mutableStateOf(initialAccountName) }
    var secretKey by remember { mutableStateOf(initialSecretKey) }
    var selectedAlgorithm by remember { mutableStateOf(initialAlgorithm) }
    var algorithmExpanded by remember { mutableStateOf(false) }
    var digitCount by remember { mutableStateOf(initialDigitCount.toString()) }
    var timeStep by remember { mutableStateOf(initialTimeStep.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Token", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
            )

            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Email") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
            )

            OutlinedTextField(
                value = secretKey,
                onValueChange = { secretKey = it },
                label = { Text("Secret Key") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
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
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
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
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
            )

            OutlinedTextField(
                value = timeStep,
                onValueChange = { if (it.length <= 3) timeStep = it },
                label = { Text("Time Step (seconds)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500)
            )

            Button(
                onClick = {
                    if (serviceLabel.isNotBlank() && secretKey.isNotBlank()) {
                        onSave(
                            serviceLabel.trim(),
                            accountName.trim(),
                            secretKey.trim().replace("\\s+".toRegex(), ""),
                            selectedAlgorithm,
                            digitCount.toIntOrNull() ?: 6,
                            timeStep.toLongOrNull() ?: 30L
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = serviceLabel.isNotBlank() && secretKey.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Orange500)
            ) {
                Text(
                    text = "Save Changes",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
