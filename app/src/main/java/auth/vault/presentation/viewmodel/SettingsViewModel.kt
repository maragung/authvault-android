package auth.vault.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.vault.data.repository.VaultRepository
import auth.vault.util.DispatchProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val timeoutMinutes: Int = 5,
    val biometricEnabled: Boolean = false,
    val themeMode: String = "system",
    val hideTokenContent: Boolean = false,
    val sortOrder: String = "last_accessed",
    val timeOffsetSeconds: Int = 0,
    val isSaving: Boolean = false,
    val autoLockBackground: Boolean = true,
    val screenshotPrevention: Boolean = true,
    val themeSchedule: String = "system",
    val hapticEnabled: Boolean = true,
    val showPreviousCodes: Boolean = false,
    val showUsageStats: Boolean = false,
    val currentVaultName: String = "Main Vault",
    val vaultNames: List<String> = listOf("Main Vault")
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val dispatchProvider: DispatchProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        vaultRepository.getTimeoutMinutes().onEach { minutes ->
            _uiState.value = _uiState.value.copy(timeoutMinutes = minutes)
        }.launchIn(viewModelScope)

        vaultRepository.isBiometricEnabled().onEach { enabled ->
            _uiState.value = _uiState.value.copy(biometricEnabled = enabled)
        }.launchIn(viewModelScope)

        vaultRepository.getThemeMode().onEach { mode ->
            _uiState.value = _uiState.value.copy(themeMode = mode)
        }.launchIn(viewModelScope)

        vaultRepository.shouldHideTokenContent().onEach { hide ->
            _uiState.value = _uiState.value.copy(hideTokenContent = hide)
        }.launchIn(viewModelScope)

        vaultRepository.getSortOrder().onEach { order ->
            _uiState.value = _uiState.value.copy(sortOrder = order)
        }.launchIn(viewModelScope)

        vaultRepository.getTimeOffsetSeconds().onEach { offset ->
            _uiState.value = _uiState.value.copy(timeOffsetSeconds = offset)
        }.launchIn(viewModelScope)

        vaultRepository.shouldAutoLockBackground().onEach { enabled ->
            _uiState.value = _uiState.value.copy(autoLockBackground = enabled)
        }.launchIn(viewModelScope)

        vaultRepository.shouldScreenshotPrevention().onEach { enabled ->
            _uiState.value = _uiState.value.copy(screenshotPrevention = enabled)
        }.launchIn(viewModelScope)

        vaultRepository.getThemeSchedule().onEach { schedule ->
            _uiState.value = _uiState.value.copy(themeSchedule = schedule)
        }.launchIn(viewModelScope)

        vaultRepository.isHapticEnabled().onEach { enabled ->
            _uiState.value = _uiState.value.copy(hapticEnabled = enabled)
        }.launchIn(viewModelScope)

        vaultRepository.shouldShowPreviousCodes().onEach { show ->
            _uiState.value = _uiState.value.copy(showPreviousCodes = show)
        }.launchIn(viewModelScope)

        vaultRepository.shouldShowUsageStats().onEach { show ->
            _uiState.value = _uiState.value.copy(showUsageStats = show)
        }.launchIn(viewModelScope)

        vaultRepository.getVaultNames().onEach { names ->
            _uiState.value = _uiState.value.copy(
                currentVaultName = names,
                vaultNames = names.split(",").map { it.trim() }
            )
        }.launchIn(viewModelScope)
    }

    fun updateTimeoutMinutes(minutes: Int) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setTimeoutMinutes(minutes.coerceIn(1, 120))
        }
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setBiometricEnabled(enabled)
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setThemeMode(mode)
        }
    }

    fun updateHideTokenContent(hide: Boolean) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setHideTokenContent(hide)
        }
    }

    fun updateSortOrder(order: String) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setSortOrder(order)
        }
    }

    fun updateTimeOffsetSeconds(offset: Int) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setTimeOffsetSeconds(offset)
        }
    }

    fun updateAutoLockBackground(enabled: Boolean) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setAutoLockBackground(enabled)
        }
    }

    fun updateScreenshotPrevention(enabled: Boolean) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setScreenshotPrevention(enabled)
        }
    }

    fun updateThemeSchedule(schedule: String) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setThemeSchedule(schedule)
        }
    }

    fun updateHapticEnabled(enabled: Boolean) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setHapticEnabled(enabled)
        }
    }

    fun updateShowPreviousCodes(show: Boolean) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setShowPreviousCodes(show)
        }
    }

    fun updateShowUsageStats(show: Boolean) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setShowUsageStats(show)
        }
    }

    fun updateVaultNames(names: String) {
        viewModelScope.launch(dispatchProvider.io) {
            vaultRepository.setVaultNames(names)
        }
    }
}
