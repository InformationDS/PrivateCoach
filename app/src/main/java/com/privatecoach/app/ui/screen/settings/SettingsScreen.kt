package com.privatecoach.app.ui.screen.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privatecoach.app.ui.component.PcCard
import com.privatecoach.app.ui.component.PcLineDivider
import com.privatecoach.app.ui.component.PcTextField
import com.privatecoach.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToTemplates: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.saveMessage) {
        uiState.saveMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    // SAF export launcher
    val dateStr = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) }
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.writeExportToUri(context, uri)
        }
    }

    // SAF import launcher
    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importData(context, uri)
        }
    }

    // Trigger export file picker when data is ready
    LaunchedEffect(uiState.exportContainer) {
        uiState.exportContainer?.let {
            createDocLauncher.launch("privatecoach_backup_$dateStr.json")
        }
    }

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PcBackground)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = PcSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PcSpacing.md)
        ) {
            item { Spacer(modifier = Modifier.height(PcSpacing.sm)) }

            // AI Configuration section
            item {
                Text("AI 服务配置", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                PcLineDivider()
                Spacer(modifier = Modifier.height(PcSpacing.sm))
            }

            item {
                PcTextField(
                    value = uiState.apiEndpoint,
                    onValueChange = { viewModel.setApiEndpoint(it) },
                    placeholder = "API Endpoint URL"
                )
            }

            item {
                PcTextField(
                    value = uiState.modelName,
                    onValueChange = { viewModel.setModelName(it) },
                    placeholder = "模型名称（如 gemini-2.0-flash）"
                )
            }

            item {
                PcTextField(
                    value = uiState.apiKey,
                    onValueChange = { viewModel.setApiKey(it) },
                    placeholder = "API Key",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (uiState.apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleApiKeyVisibility() }) {
                            Icon(
                                if (uiState.apiKeyVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = PcTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }

            item {
                Button(
                    onClick = { viewModel.saveSettings() },
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = PcAccentCopper, contentColor = PcBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存设置")
                }
            }

            // Data section
            item {
                Spacer(modifier = Modifier.height(PcSpacing.lg))
                Text("数据管理", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                PcLineDivider()
                Spacer(modifier = Modifier.height(PcSpacing.sm))
            }

            item {
                OutlinedButton(
                    onClick = onNavigateToTemplates,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PcAccentCopper),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("训练模板管理") }
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.prepareExport() },
                    enabled = !uiState.isExporting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PcAccentCopper),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (uiState.isExporting) "导出中..." else "导出 JSON") }
            }

            // Export result
            uiState.exportResult?.let { result ->
                item {
                    Text(
                        result,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (result.contains("成功")) PcFeelingGood else PcFeelingTired
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { openDocLauncher.launch(arrayOf("application/json")) },
                    enabled = !uiState.isImporting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PcAccentCopper),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (uiState.isImporting) "导入中..." else "导入 JSON") }
            }

            // Import result card
            uiState.importResult?.let { result ->
                item {
                    PcCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "导入完成: ${result.workoutsImported} 条训练, ${result.templatesImported} 个模板",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        result.errors.take(3).forEach { error ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                error,
                                style = MaterialTheme.typography.labelSmall,
                                color = PcFeelingTired
                            )
                        }
                        if (result.errors.size > 3) {
                            Text(
                                "...还有 ${result.errors.size - 3} 个错误",
                                style = MaterialTheme.typography.labelSmall,
                                color = PcTextSecondary
                            )
                        }
                    }
                }
            }

            // About section
            item {
                Spacer(modifier = Modifier.height(PcSpacing.lg))
                Text("关于", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
                PcLineDivider()
                Spacer(modifier = Modifier.height(PcSpacing.sm))
                Text("PrivateCoach v1.0.0", style = MaterialTheme.typography.bodySmall, color = PcTextSecondary)
                Text("纯个人使用的健身记录工具", style = MaterialTheme.typography.bodySmall, color = PcTextDisabled)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
