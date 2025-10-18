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

        else -> {
            // Wrap Button in a Box so gradient or solid background becomes the *actual* surface
            Box(
                modifier = modifier
                    .padding(horizontal = 8.dp)
                    .height(size.height.dp)
                    .background(
                        when {
                            containerColor != null -> containerColor
                            else -> when (variant) {
                                ButtonVariant.Default -> Color.Transparent // will be overridden by GradientPrimary below
                                ButtonVariant.Secondary -> MaterialTheme.colorScheme.secondary
                                ButtonVariant.Destructive -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        },
                        shape = buttonShape
                    )
                    .then(
                        if (containerColor == null && variant == ButtonVariant.Default)
                            Modifier.background(GradientPrimary, shape = buttonShape)
                        else Modifier
                    )
            ) {
                Button(
                    onClick = onClick,
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    ),
                    interactionSource = interactionSource,
                    shape = buttonShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) { content() }
            }
        }
    }
}
