package com.fredcodecrafts.moodlens.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Variants ---
enum class ButtonVariant { Default, Secondary, Destructive, Outline }

// --- Sizes ---
enum class ButtonSize(val height: Int, val textSize: Int) {
    Small(36, 12),
    Default(48, 14),
    Large(56, 16),
    Icon(48, 0) // icon-only
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Default,
    size: ButtonSize = ButtonSize.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val colors = when (variant) {
        ButtonVariant.Default -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        ButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
        ButtonVariant.Destructive -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
        ButtonVariant.Outline -> ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    val content: @Composable RowScope.() -> Unit = {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(6.dp))
        }
        if (size != ButtonSize.Icon) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = size.textSize.sp
                )
            )
        }
        if (trailingIcon != null) {
            Spacer(Modifier.width(6.dp))
            trailingIcon()
        }
    }

    when (variant) {
        ButtonVariant.Outline -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
            shape = MaterialTheme.shapes.medium,
            modifier = modifier
                .padding(horizontal = 8.dp)
                .height(size.height.dp)
        ) {
            content()
        }
        else -> Button(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
            shape = MaterialTheme.shapes.medium,
            modifier = modifier
                .padding(horizontal = 8.dp)
                .height(size.height.dp)
        ) {
            content()
        }
    }
}
