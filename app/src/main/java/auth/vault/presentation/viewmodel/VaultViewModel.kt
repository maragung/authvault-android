package auth.vault.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.vault.data.local.entity.AuthTokenEntity
import auth.vault.data.repository.VaultRepository
import auth.vault.domain.usecase.PasswordHasher
import auth.vault.domain.usecase.TotpGenerator
import auth.vault.domain.usecase.VaultLockTimeout
import auth.vault.util.ClipboardManagerUtil
import auth.vault.util.DispatchProvider
import auth.vault.util.HapticFeedbackUtil
import auth.vault.util.TimeSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VaultUiState(
    val tokens: List<TokenDisplayItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLocked: Boolean = true,
    val timeoutMinutes: Int = 5,
    val hideTokenContent: Boolean = false,
    val searchQuery: String = "",
    val showEmptyState: Boolean = true,
    val sortOrder: String = "last_accessed",
    val hapticEnabled: Boolean = true,
    val showPreviousCodes: Boolean = false,
    val currentVaultName: String = "Main Vault",
    val activeCategory: String = "",
    val activeTag: String = ""
)

data class TokenDisplayItem(
    val tokenId: Long,
    val serviceLabel: String,
    val accountName: String,
    val currentCode: String,
    val remainingSeconds: Long,
    val iconColor: String,
    val usageCount: Int,
    val isPinned: Boolean = false,
    val isSteam: Boolean = false,
    val tokenTags: String = "",
    val previousCode1: String = "",
    val previousCode2: String = "",
    val counterValue: Long = 0L
)

sealed class UnlockResult {
    data object Success : UnlockResult()
    data object InvalidPassword : UnlockResult()
    data object FirstTimeSetup : UnlockResult()
    data class Error(val message: String) : UnlockResult()
}

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val totpGenerator: TotpGenerator,
    private val vaultLockTimeout: VaultLockTimeout,
    private val dispatchProvider: DispatchProvider,
    private val timeSource: TimeSource,
    private val passwordHasher: PasswordHasher,
    private val clipboardManager: ClipboardManagerUtil,
    private val hapticFeedback: HapticFeedbackUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        observeSettings()
        startTokenTicker()
    }

    private fun observeSettings() {
        vaultRepository.getTimeoutMinutes()
            .onEach { minutes -> _uiState.value = _uiState.value.copy(timeoutMinutes = minutes) }
            .launchIn(viewModelScope)

        vaultRepository.shouldHideTokenContent()
            .onEach { hide -> _uiState.value = _uiState.value.copy(hideTokenContent = hide) }
            .launchIn(viewModelScope)

        vaultRepository.getSortOrder()
            .onEach { order -> _uiState.value = _uiState.value.copy(sortOrder = order) }
            .launchIn(viewModelScope)

        vaultRepository.isHapticEnabled()
            .onEach { enabled -> _uiState.value = _uiState.value.copy(hapticEnabled = enabled) }
            .launchIn(viewModelScope)

        vaultRepository.shouldShowPreviousCodes()
            .onEach { show -> _uiState.value = _uiState.value.copy(showPreviousCodes = show) }
            .launchIn(viewModelScope)

        vaultRepository.getVaultNames()
            .onEach { name -> _uiState.value = _uiState.value.copy(currentVaultName = name) }
            .launchIn(viewModelScope)
    }

    private fun startTokenTicker() {
        viewModelScope.launch(dispatchProvider.io) {
            while (true) {
                try {
                    val entities = vaultRepository.getAllTokens().first()
                    val timeOffset = vaultRepository.getTimeOffsetSeconds().first()
                    val adjustedTime = timeSource.currentSeconds() + timeOffset
                    val sortedEntities = when (_uiState.value.sortOrder) {
                        "name" -> entities.sortedBy { it.serviceLabel.lowercase() }
                        "pinned" -> entities.sortedWith(
                            compareByDescending<AuthTokenEntity> { it.isPinned }
                                .thenByDescending { it.lastAccessed }
                        )
                        else -> entities.sortedByDescending { it.lastAccessed }
                    }
                    val filteredEntities = when {
                        _uiState.value.activeCategory.isNotEmpty() ->
                            sortedEntities.filter { it.tokenCategory == _uiState.value.activeCategory }
                        _uiState.value.activeTag.isNotEmpty() ->
                            sortedEntities.filter { it.tokenTags.contains(_uiState.value.activeTag) }
                        else -> sortedEntities
                    }
                    val displayItems = filteredEntities.mapNotNull { entity ->
                        try {
                            val code = if (entity.isSteam) {
                                generateSteamCode(entity.secretKey, adjustedTime)
                            } else {
                                totpGenerator.generateCode(
                                    entity.secretKey,
                                    adjustedTime,
                                    entity.digitCount,
                                    entity.algorithm
                                )
                            }
                            val remainingSeconds = totpGenerator.getRemainingSeconds(
                                adjustedTime,
                                entity.timeStep
                            )
                            TokenDisplayItem(
                                tokenId = entity.tokenId,
                                serviceLabel = entity.serviceLabel,
                                accountName = entity.accountName,
                                currentCode = code,
                                remainingSeconds = remainingSeconds,
                                iconColor = entity.iconColor,
                                usageCount = entity.usageCount,
                                isPinned = entity.isPinned,
                                isSteam = entity.isSteam,
                                tokenTags = entity.tokenTags,
                                previousCode1 = entity.previousCode1,
                                previousCode2 = entity.previousCode2,
                                counterValue = entity.counterValue
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        tokens = displayItems,
                        showEmptyState = displayItems.isEmpty()
                    )
                } catch (e: Exception) {
                }
                delay(1000L)
            }
        }
    }

    private fun generateSteamCode(secretKey: String, timestamp: Long): String {
        return runCatching {
            val code = totpGenerator.generateCode(secretKey, timestamp, 5, "SHA1")
            "BCDFGHJKMPQRTVWXY2346789".let { chars ->
                code.map { c ->
                    val idx = c.digitToIntOrNull(32) ?: 0
                    chars.getOrElse(idx % chars.length) { '2' }
                }.joinToString("")
            }
        }.getOrElse { "ERROR" }
    }

    fun unlockVault(password: String, onComplete: (UnlockResult) -> Unit) {
        viewModelScope.launch(dispatchProvider.io) {
            val result = try {
                val storedHash = vaultRepository.getMasterPassHash()
                if (storedHash == null) {
                    val hash = passwordHasher.hashPassword(password)
                    vaultRepository.setMasterPassHash(hash)
                    vaultLockTimeout.recordUnlock()
                    UnlockResult.FirstTimeSetup
                } else if (passwordHasher.verifyPassword(password, storedHash)) {
                    vaultLockTimeout.recordUnlock()
                    UnlockResult.Success
                } else {
                    UnlockResult.InvalidPassword
                }
            } catch (e: Exception) {
                UnlockResult.Error(e.message ?: "Authentication failed")
            }

            if (result is UnlockResult.Success || result is UnlockResult.FirstTimeSetup) {
                _isAuthenticated.value = true
                _uiState.value = _uiState.value.copy(isLocked = false)
                if (_uiState.value.hapticEnabled) hapticFeedback.unlockSuccess()
            }
            onComplete(result)
        }
    }

    fun checkVaultTimeout() {
        viewModelScope.launch(dispatchProvider.io) {
            try {
                val autoLock = vaultRepository.shouldAutoLockBackground().first()
                val timedOut = vaultLockTimeout.isVaultTimedOut()
                if (autoLock || timedOut) {
                    _uiState.value = _uiState.value.copy(isLocked = true)
                    _isAuthenticated.value = false
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLocked = true)
            }
        }
    }

    fun lockVault() {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setVaultLocked(true)
        }
        _uiState.value = _uiState.value.copy(isLocked = true)
        _isAuthenticated.value = false
        if (_uiState.value.hapticEnabled) hapticFeedback.lockVibrate()
    }

    fun copyTokenToClipboard(code: String, serviceName: String) {
        clipboardManager.copyToClipboard(code, "AuthVault - $serviceName")
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setActiveCategory(category: String) {
        _uiState.value = _uiState.value.copy(activeCategory = category)
    }

    fun setActiveTag(tag: String) {
        _uiState.value = _uiState.value.copy(activeTag = tag)
    }

    fun togglePinToken(tokenId: Long) {
        viewModelScope.launch(dispatchProvider.io) {
            val token = vaultRepository.getTokenById(tokenId) ?: return@launch
            vaultRepository.togglePinToken(tokenId, !token.isPinned)
            if (_uiState.value.hapticEnabled) hapticFeedback.longPressVibrate()
        }
    }

    fun addToken(
        serviceLabel: String,
        accountName: String,
        secretKey: String,
        algorithm: String,
        digitCount: Int,
        timeStep: Long,
        isSteam: Boolean = false,
        tags: String = ""
    ) {
        viewModelScope.launch(dispatchProvider.io) {
            try {
                val trimmedSecret = secretKey.trim().replace("\\s+".toRegex(), "")
                if (trimmedSecret.isBlank()) return@launch
                val token = AuthTokenEntity(
                    serviceLabel = serviceLabel.trim(),
                    accountName = accountName.trim(),
                    secretKey = trimmedSecret,
                    algorithm = algorithm,
                    digitCount = digitCount.coerceIn(4, 10),
                    timeStep = timeStep.coerceIn(10L, 120L),
                    isSteam = isSteam,
                    tokenTags = tags,
                    createdAt = timeSource.currentTimeMillis()
                )
                vaultRepository.addToken(token)
            } catch (e: Exception) {
            }
        }
    }

    fun deleteToken(tokenId: Long) {
        viewModelScope.launch(dispatchProvider.io) {
            try {
                vaultRepository.removeTokenById(tokenId)
                if (_uiState.value.hapticEnabled) hapticFeedback.deleteVibrate()
            } catch (e: Exception) {
            }
        }
    }

    fun updateToken(
        tokenId: Long,
        serviceLabel: String,
        accountName: String,
        secretKey: String,
        algorithm: String,
        digitCount: Int,
        timeStep: Long,
        isSteam: Boolean = false,
        tags: String = ""
    ) {
        viewModelScope.launch(dispatchProvider.io) {
            try {
                val existing = vaultRepository.getTokenById(tokenId) ?: return@launch
                val trimmedSecret = secretKey.trim().replace("\\s+".toRegex(), "")
                if (trimmedSecret.isBlank()) return@launch
                val updated = AuthTokenEntity(
                    tokenId = tokenId,
                    serviceLabel = serviceLabel.trim(),
                    accountName = accountName.trim(),
                    secretKey = trimmedSecret,
                    algorithm = algorithm,
                    digitCount = digitCount.coerceIn(4, 10),
                    timeStep = timeStep.coerceIn(10L, 120L),
                    isSteam = isSteam,
                    tokenTags = tags,
                    iconColor = existing.iconColor,
                    isPinned = existing.isPinned,
                    createdAt = existing.createdAt,
                    lastAccessed = existing.lastAccessed,
                    usageCount = existing.usageCount,
                    previousCode1 = existing.previousCode1,
                    previousCode2 = existing.previousCode2,
                    counterValue = existing.counterValue
                )
                vaultRepository.updateToken(updated)
            } catch (e: Exception) {
            }
        }
    }

    fun setTokenColor(tokenId: Long, color: String) {
        viewModelScope.launch(dispatchProvider.io) {
            runCatching {
                val existing = vaultRepository.getTokenById(tokenId) ?: return@launch
                val updated = existing.copy(iconColor = color)
                vaultRepository.updateToken(updated)
            }
        }
    }

    fun setTokenNotes(tokenId: Long, notes: String) {
        viewModelScope.launch(dispatchProvider.io) {
            runCatching {
                val existing = vaultRepository.getTokenById(tokenId) ?: return@launch
                val updated = existing.copy(tokenNotes = notes)
                vaultRepository.updateToken(updated)
            }
        }
    }

    fun deleteSelectedTokens(tokenIds: List<Long>) {
        viewModelScope.launch(dispatchProvider.io) {
            tokenIds.forEach { id ->
                runCatching { vaultRepository.removeTokenById(id) }
            }
        }
    }

    fun importTokensFromUri(uriContent: String) {
        viewModelScope.launch(dispatchProvider.io) {
            try {
                val result = vaultRepository.parseOtpUri(uriContent)
                if (result is auth.vault.data.importer.AuthenticatorImporter.ImportResult.Success) {
                    vaultRepository.importTokens(result.tokens)
                }
            } catch (e: Exception) {
            }
        }
    }
}
