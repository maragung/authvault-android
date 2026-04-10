package auth.vault.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import auth.vault.data.local.database.VaultDatabase
import auth.vault.domain.usecase.TotpGenerator
import auth.vault.util.TimeSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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
            val database = VaultDatabase.getInstance(this, ByteArray(32))
            val tokens = runBlocking { database.tokenDao().getAllTokens().first() }
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
        }
        updateTileState()
    }

    private fun updateTileState() {
        runCatching {
            val database = VaultDatabase.getInstance(this, ByteArray(32))
            val tokenCount = runBlocking { database.tokenDao().getTokenCount().first() }
            qsTile?.apply {
                state = if (tokenCount > 0) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                label = "AuthVault"
                updateTile()
            }
        }
    }
}
