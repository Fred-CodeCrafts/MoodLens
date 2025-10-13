package com.fredcodecrafts.moodlens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fredcodecrafts.moodlens.ui.theme.GradientPrimary

// --- Variants ---
enum class ButtonVariant { Default, Secondary, Destructive, Outline }

// --- Sizes ---
enum class ButtonSize(val height: Int, val textSize: Int) {
    Small(36, 12),
    Default(48, 14),
    Large(56, 16),
    Icon(48, 0)
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
    containerColor: Color? = null,
    contentColor: Color? = null,
    shape: CornerBasedShape? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    val buttonShape = shape ?: RoundedCornerShape(12.dp)

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
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = containerColor ?: Color.Transparent,
                contentColor = contentColor ?: MaterialTheme.colorScheme.primary
            ),
            interactionSource = interactionSource,
            shape = buttonShape,
            modifier = modifier
                .padding(horizontal = 8.dp)
                .height(size.height.dp)
        ) { content() }

        else -> Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary
            ),
            interactionSource = interactionSource,
            shape = buttonShape,
            modifier = modifier
                .padding(horizontal = 8.dp)
                .height(size.height.dp)
                .background(
                    brush = when (variant) {
                        ButtonVariant.Default -> GradientPrimary
                        ButtonVariant.Secondary -> Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        )
                        ButtonVariant.Destructive -> Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.error,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        )
                        else -> Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)
                        )
                    },
                    shape = buttonShape
                )
        ) { content() }
    }
}
