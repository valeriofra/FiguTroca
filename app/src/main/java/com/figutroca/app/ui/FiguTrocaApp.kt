package com.figutroca.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.figutroca.app.ui.components.AddStickerSheet
import com.figutroca.app.ui.screens.AlbumScreen
import com.figutroca.app.ui.screens.CollectionsScreen
import com.figutroca.app.ui.screens.ListKind
import com.figutroca.app.ui.screens.ListsScreen

private enum class Tab(val label: String, val icon: ImageVector) {
    ALBUM("Álbum", Icons.Rounded.GridView),
    DUPLICATES("Repetidas", Icons.Rounded.SwapHoriz),
    MISSING("Faltam", Icons.Rounded.Checklist),
    COLLECTIONS("Coleções", Icons.Rounded.Inventory2)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiguTrocaApp(vm: AppViewModel) {
    var tab by remember { mutableStateOf(Tab.ALBUM) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("FiguTroca") })
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == Tab.ALBUM) {
                ExtendedFloatingActionButton(
                    onClick = { showAdd = true },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Figurinhas") }
                )
            }
        }
    ) { padding ->
        Crossfade(targetState = tab, label = "tab", modifier = Modifier.fillMaxSize()) { current ->
            when (current) {
                Tab.ALBUM -> AlbumScreen(vm, padding)
                Tab.DUPLICATES -> ListsScreen(vm, ListKind.DUPLICATES, padding)
                Tab.MISSING -> ListsScreen(vm, ListKind.MISSING, padding)
                Tab.COLLECTIONS -> CollectionsScreen(vm, padding)
            }
        }
    }

    if (showAdd) {
        AddStickerSheet(
            onDismiss = { showAdd = false },
            onAddRange = { from, to, group -> vm.addRange(from, to, group) },
            onAddCodes = { raw, group -> vm.addCodes(raw, group) }
        )
    }
}
