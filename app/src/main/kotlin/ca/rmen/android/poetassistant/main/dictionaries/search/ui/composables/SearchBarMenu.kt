package ca.rmen.android.poetassistant.main.dictionaries.search.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/** Submenu replaces the current menu content. */
@Composable
fun PhoneMenuContent(
    items: List<SearchBarMenuItem>,
    onItemClick: (Int) -> Unit,
) {
    // Lives inside the menu content, so it resets when the menu closes.
    var openedParentId by remember { mutableStateOf<Int?>(null) }
    val parent = items.firstOrNull { it.id == openedParentId }

    if (parent == null) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.title) },
                enabled = item.enabled,
                trailingIcon = if (item.children.isNotEmpty()) {
                    { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
                } else null,
                onClick = {
                    if (item.children.isEmpty()) onItemClick(item.id)
                    else openedParentId = item.id
                },
            )
        }
    } else {
        DropdownMenuItem(
            text = { Text(parent.title) },
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            },
            onClick = { openedParentId = null },
        )
        HorizontalDivider()
        parent.children.forEach { child ->
            DropdownMenuItem(
                text = { Text(child.title) },
                enabled = child.enabled,
                onClick = { onItemClick(child.id) },
            )
        }
    }
}

/** Submenu opens in its own popup, to the left of the parent menu. */
@Composable
fun TabletMenuContent(
    items: List<SearchBarMenuItem>,
    onItemClick: (Int) -> Unit,
) {
    items.forEach { item ->
        if (item.children.isEmpty()) {
            DropdownMenuItem(
                text = { Text(item.title) },
                enabled = item.enabled,
                onClick = { onItemClick(item.id) },
            )
        } else {
            var showSubMenu by remember(item.id) { mutableStateOf(false) }

            // The Box is the anchor, so the popup is positioned relative to this row.
            Box {
                DropdownMenuItem(
                    text = { Text(item.title) },
                    enabled = item.enabled,
                    trailingIcon = {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    },
                    onClick = { showSubMenu = true },
                )
                if (showSubMenu) {
                    SubMenuPopup(onDismissRequest = { showSubMenu = false }) {
                        item.children.forEach { child ->
                            DropdownMenuItem(
                                text = { Text(child.title) },
                                enabled = child.enabled,
                                onClick = { onItemClick(child.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubMenuPopup(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val positionProvider = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                // Left of the parent menu, aligned with the clicked row.
                val x = (anchorBounds.left - popupContentSize.width).coerceAtLeast(0)
                val y = anchorBounds.top
                    .coerceAtMost(windowSize.height - popupContentSize.height)
                    .coerceAtLeast(0)
                return IntOffset(x, y)
            }
        }
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Surface(
            shape = MenuDefaults.shape,
            color = MenuDefaults.containerColor,
            tonalElevation = MenuDefaults.TonalElevation,
            shadowElevation = MenuDefaults.ShadowElevation,
        ) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 112.dp, max = 280.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = DropdownMenuVerticalPadding),
                content = content,
            )
        }
    }
}

private val DropdownMenuVerticalPadding = 8.dp