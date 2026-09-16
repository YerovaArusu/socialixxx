package at.yerova.socialixxx.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import at.yerova.socialixxx.api.ApiClient
import at.yerova.socialixxx.api.NetworkResult
import at.yerova.socialixxx.api.RegisterRequest
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    apiClient: ApiClient,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    // Asynchrone States
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Account erstellen",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Anzeigename (z.B. Leon)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("Abteilung (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Benutzername (für Login)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Passwort") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && displayName.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null

                    val dep = department.trim().takeIf { it.isNotEmpty() }
                    
                    val request = RegisterRequest(
                        username = username.trim(),
                        passwordHash = password, // Im PoC direktes Passwort, später Hash
                        displayName = displayName.trim(),
                        department = dep
                    )

                    val result = apiClient.register(request)
                    isLoading = false

                    when (result) {
                        is NetworkResult.Success -> {
                            onRegisterSuccess()
                        }
                        is NetworkResult.Error -> {
                            errorMessage = result.message
                        }
                    }
                }
            }
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Registrieren")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack, enabled = !isLoading) {
            Text("Zurück zum Login")
        }
    }
}