package auth.vault.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.vault.data.repository.VaultRepository
import auth.vault.data.security.BiometricAuthenticator
import auth.vault.util.DispatchProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockScreenUiState(
    val showBiometricOption: Boolean = false,
    val biometricAvailable: Boolean = false,
    val showError: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class LockScreenViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val dispatchProvider: DispatchProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockScreenUiState())
    val uiState: StateFlow<LockScreenUiState> = _uiState.asStateFlow()

    init {
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch(dispatchProvider.io) {
            try {
                val biometricEnabled = vaultRepository.isBiometricEnabled().first()
                val biometricAvailable = biometricAuthenticator.canAuthenticate()
                _uiState.value = _uiState.value.copy(
                    showBiometricOption = biometricEnabled && biometricAvailable,
                    biometricAvailable = biometricAvailable
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showBiometricOption = false,
                    biometricAvailable = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(showError = false, errorMessage = "")
    }
}
