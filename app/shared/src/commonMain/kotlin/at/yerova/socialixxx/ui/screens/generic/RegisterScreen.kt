package at.yerova.socialixxx.ui.screens.generic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import at.yerova.socialixxx.LocalApiClient
import at.yerova.socialixxx.api.DepartmentBaseDto
import at.yerova.socialixxx.api.NetworkResult
import at.yerova.socialixxx.api.RegisterRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var departments by remember { mutableStateOf<List<DepartmentBaseDto>>(emptyList()) }
    var selectedDepartment by remember { mutableStateOf<DepartmentBaseDto?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val apiClient = LocalApiClient.current

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = apiClient.getDepartments()
        if (result is NetworkResult.Success) {
            departments = result.data
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Account erstellen",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = displayName, onValueChange = { displayName = it },
            label = { Text("Anzeigename (z.B. Leon)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedDepartment?.name ?: "Keine Abteilung ausgewählt",
                onValueChange = {},
                label = { Text("Abteilung (Optional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Keine Zuordnung") },
                    onClick = { selectedDepartment = null; expanded = false }
                )
                Divider()
                departments.forEach { dept ->
                    DropdownMenuItem(
                        text = { Text("${dept.kz} - ${dept.name}") },
                        onClick = {
                            selectedDepartment = dept
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username, onValueChange = { username = it },
            label = { Text("Benutzername (für Login)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Passwort") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), singleLine = true
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

                    val request = RegisterRequest(
                        username = username.trim(),
                        passwordHash = password,
                        displayName = displayName.trim(),
                        departmentId = selectedDepartment?.id
                    )

                    when (val result = apiClient.register(request)) {
                        is NetworkResult.Success -> onRegisterSuccess()
                        is NetworkResult.Error -> errorMessage = result.message
                    }
                    isLoading = false
                }
            }
        ) {
            if (isLoading) CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            else Text("Registrieren")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateBack, enabled = !isLoading) { Text("Zurück zum Login") }
    }
}