package com.fredcodecrafts.moodlens.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

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
    containerColor: Color? = null, // optional override
    contentColor: Color? = null,   // optional override
    shape: CornerBasedShape? = null // optional override
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Determine colors
    val colors = when (variant) {
        ButtonVariant.Outline -> ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor ?: Color.Transparent,
            contentColor = contentColor ?: MaterialTheme.colorScheme.primary
        )
        else -> ButtonDefaults.buttonColors(
            containerColor = containerColor ?: when (variant) {
                ButtonVariant.Default -> MaterialTheme.colorScheme.primary
                ButtonVariant.Secondary -> MaterialTheme.colorScheme.secondary
                ButtonVariant.Destructive -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            },
            contentColor = contentColor ?: when (variant) {
                ButtonVariant.Default -> MaterialTheme.colorScheme.onPrimary
                ButtonVariant.Secondary -> MaterialTheme.colorScheme.onSecondary
                ButtonVariant.Destructive -> MaterialTheme.colorScheme.onError
                else -> MaterialTheme.colorScheme.onPrimary
            }
        )
    }

    // Determine shape
    val buttonShape = shape ?: when (variant) {
        ButtonVariant.Outline -> MaterialTheme.shapes.medium
        else -> RoundedCornerShape(12.dp)
    }

    // Button content
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

    // Render the button
    when (variant) {
        ButtonVariant.Outline -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
            shape = buttonShape,
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
            shape = buttonShape,
            modifier = modifier
                .padding(horizontal = 8.dp)
                .height(size.height.dp)
        ) {
            content()
        }
    }
}
