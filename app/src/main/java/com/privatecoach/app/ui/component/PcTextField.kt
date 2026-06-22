package com.privatecoach.app.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.VisualTransformation
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcBackground
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcTextDisabled
import com.privatecoach.app.ui.theme.PcTextSecondary

@Composable
fun PcTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PcDivider, PcShapes.small),
        placeholder = {
            Text(
                text = placeholder,
                color = PcTextDisabled
            )
        },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            disabledTextColor = PcTextDisabled,
            focusedContainerColor = PcBackground,
            unfocusedContainerColor = PcBackground,
            cursorColor = PcAccentCopper,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLabelColor = PcTextSecondary,
            unfocusedLabelColor = PcTextSecondary
        )
    )
}
