package com.figutroca.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import com.figutroca.app.ui.components.AddStickerSheet
import com.figutroca.app.ui.components.ImportSheet
import com.figutroca.app.ui.screens.AlbumScreen
import com.figutroca.app.ui.screens.CollectionsScreen

private enum class Tab(val label: String, val icon: ImageVector) {
    ALBUM("Álbum", Icons.Rounded.GridView),
    COLLECTIONS("Coleções", Icons.Rounded.Inventory2)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiguTrocaApp(vm: AppViewModel) {
    var tab by remember { mutableStateOf(Tab.ALBUM) }
    var showAdd by remember { mutableStateOf(false) }
    var showImport by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FiguTroca") },
                actions = {
                    IconButton(onClick = { showImport = true }) {
                        Icon(Icons.Rounded.ContentPaste, contentDescription = "Importar lista")
                    }
                }
            )
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

    if (showImport) {
        ImportSheet(
            onDismiss = { showImport = false },
            onImport = { raw, mode ->
                vm.importList(raw, mode) { result ->
                    Toast.makeText(
                        context,
                        "Importado: ${result.stickers} figurinhas em ${result.teams} seleções",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }
}
