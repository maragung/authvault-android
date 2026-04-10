package auth.vault.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import auth.vault.data.local.dao.TokenDao
import auth.vault.domain.usecase.TotpGenerator
import auth.vault.util.TimeSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VaultTileServiceEntryPoint {
    fun tokenDao(): TokenDao
}

class VaultQuickTileService : TileService() {

    private val totpGenerator = TotpGenerator()
    private val timeSource = TimeSource()

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                VaultTileServiceEntryPoint::class.java
            )
            val tokenDao = entryPoint.tokenDao()
            val tokens = runBlocking { tokenDao.getAllTokens().first() }
            if (tokens.isNotEmpty()) {
                val adjustedTime = timeSource.currentSeconds()
                val firstToken = tokens.first()
                val code = totpGenerator.generateCode(
                    firstToken.secretKey,
                    adjustedTime,
                    firstToken.digitCount,
                    firstToken.algorithm
                )
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("AuthVault OTP", code))
            }
        }.onFailure {
            Timber.e(it, "Failed to copy OTP from quick tile")
        }
        updateTileState()
    }

    private fun updateTileState() {
        runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                VaultTileServiceEntryPoint::class.java
            )
            val tokenDao = entryPoint.tokenDao()
            val tokenCount = runBlocking { tokenDao.getTokenCount().first() }
            qsTile?.apply {
                state = if (tokenCount > 0) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                label = "AuthVault"
                updateTile()
            }
        }.onFailure {
            Timber.e(it, "Failed to update tile state")
        }
    }
}
