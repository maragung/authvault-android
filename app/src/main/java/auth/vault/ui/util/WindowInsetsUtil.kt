package auth.vault.ui.util

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier

fun Modifier.systemBarsPadding() = this
    .statusBarsPadding()
    .navigationBarsPadding()

fun Modifier.systemBarsAndImePadding() = this
    .statusBarsPadding()
    .navigationBarsPadding()
    .imePadding()
