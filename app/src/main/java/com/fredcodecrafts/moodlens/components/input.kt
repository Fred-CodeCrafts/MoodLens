package com.fredcodecrafts.moodlens.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import com.fredcodecrafts.moodlens.ui.theme.MainBackground
import com.fredcodecrafts.moodlens.ui.theme.MainPurple
import com.fredcodecrafts.moodlens.ui.theme.TextSecondary

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    isError: Boolean = false,
    backgroundColor: Color = MainBackground, // ✅ default background color
    maxLinesBeforeScroll: Int = 5            // ✅ max visible rows before scroll
) {
    val scrollState = rememberScrollState()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState), // scroll inside once limit exceeded
        enabled = enabled,
        readOnly = readOnly,
        singleLine = false, // ✅ allow multi-line
        label = label?.let { { Text(it) } },
        placeholder = { Text(placeholder, color = TextSecondary) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
        isError = isError,
        maxLines = maxLinesBeforeScroll, // ✅ grow until N rows
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MainPurple,
            unfocusedIndicatorColor = TextSecondary,
            cursorColor = MainPurple,
            focusedLabelColor = MainPurple,
            unfocusedLabelColor = TextSecondary,
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            disabledContainerColor = backgroundColor
        )
    )
}
