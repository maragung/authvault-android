package auth.vault.util

object VaultConstants {
    const val DEFAULT_TIMEOUT_MINUTES = 5
    const val MIN_TIMEOUT_MINUTES = 1
    const val MAX_TIMEOUT_MINUTES = 120
    const val STEP_TIMEOUT_MINUTES = 5
    const val DB_NAME = "authvault_db"
    const val DATASTORE_NAME = "authvault_prefs"
    const val TOTP_CODE_LENGTH = 6
    const val TOTP_PERIOD = 30L
    const val MASTER_KEY_ALIAS = "authvault_master_key"
}
