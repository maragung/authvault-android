package auth.vault.di

import android.content.Context
import androidx.room.Room
import auth.vault.data.backup.VaultBackupManager
import auth.vault.data.importer.AuthenticatorImporter
import auth.vault.data.local.dao.TokenDao
import auth.vault.data.local.database.VaultDatabase
import auth.vault.data.notifications.VaultNotificationManager
import auth.vault.data.network.VaultNetworkClient
import auth.vault.data.repository.CloudSyncRepository
import auth.vault.data.repository.VaultRepository
import auth.vault.data.security.BiometricAuthenticator
import auth.vault.data.security.EncryptionKeyProvider
import auth.vault.data.settings.repository.SettingsRepository
import auth.vault.domain.usecase.PasswordHasher
import auth.vault.domain.usecase.TotpGenerator
import auth.vault.domain.usecase.VaultLockTimeout
import auth.vault.lifecycle.AppLifecycleObserver
import auth.vault.util.ClipboardManagerUtil
import auth.vault.util.DispatchProvider
import auth.vault.util.GlobalExceptionHandler
import auth.vault.util.HapticFeedbackUtil
import auth.vault.util.TimeSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDispatchProvider(): DispatchProvider = DispatchProvider()

    @Provides
    @Singleton
    fun provideTimeSource(): TimeSource = TimeSource()

    @Provides
    @Singleton
    fun provideTotpGenerator(): TotpGenerator = TotpGenerator()

    @Provides
    @Singleton
    fun providePasswordHasher(): PasswordHasher = PasswordHasher()

    @Provides
    @Singleton
    fun provideHapticFeedbackUtil(@ApplicationContext context: Context): HapticFeedbackUtil =
        HapticFeedbackUtil(context)

    @Provides
    @Singleton
    fun provideClipboardManager(
        @ApplicationContext context: Context,
        hapticFeedback: HapticFeedbackUtil
    ): ClipboardManagerUtil = ClipboardManagerUtil(context, hapticFeedback)

    @Provides
    @Singleton
    fun provideEncryptionKeyProvider(@ApplicationContext context: Context): EncryptionKeyProvider =
        EncryptionKeyProvider(context)

    @Provides
    @Singleton
    fun provideBiometricAuthenticator(@ApplicationContext context: Context): BiometricAuthenticator =
        BiometricAuthenticator(context)

    @Provides
    @Singleton
    fun provideAuthenticatorImporter(
        @ApplicationContext context: Context,
        timeSource: TimeSource
    ): AuthenticatorImporter = AuthenticatorImporter(context, timeSource)

    @Provides
    @Singleton
    fun provideAppLifecycleObserver(
        @ApplicationContext context: Context,
        vaultRepository: VaultRepository,
        hapticFeedback: HapticFeedbackUtil
    ): AppLifecycleObserver = AppLifecycleObserver(context, vaultRepository, hapticFeedback)

    @Provides
    @Singleton
    fun provideVaultNetworkClient(): VaultNetworkClient = VaultNetworkClient()

    @Provides
    @Singleton
    fun provideCloudSyncRepository(networkClient: VaultNetworkClient): CloudSyncRepository =
        CloudSyncRepository(networkClient)

    @Provides
    @Singleton
    fun provideVaultBackupManager(@ApplicationContext context: Context): VaultBackupManager =
        VaultBackupManager(context)

    @Provides
    @Singleton
    fun provideVaultNotificationManager(@ApplicationContext context: Context): VaultNotificationManager =
        VaultNotificationManager(context)

    @Provides
    @Singleton
    fun provideGlobalExceptionHandler(dispatchProvider: DispatchProvider): GlobalExceptionHandler =
        GlobalExceptionHandler(dispatchProvider)

    @Provides
    @Singleton
    fun provideVaultDatabase(
        @ApplicationContext context: Context,
        encryptionKeyProvider: EncryptionKeyProvider
    ): VaultDatabase {
        val passphrase = encryptionKeyProvider.getDatabasePassphrase()
        return VaultDatabase.getInstance(context, passphrase)
    }

    @Provides
    @Singleton
    fun provideTokenDao(database: VaultDatabase): TokenDao = database.tokenDao()

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context,
        dispatchProvider: DispatchProvider,
        encryptionKeyProvider: EncryptionKeyProvider
    ): SettingsRepository = SettingsRepository(context, dispatchProvider, encryptionKeyProvider)

    @Provides
    @Singleton
    fun provideVaultRepository(
        tokenDao: TokenDao,
        settingsRepository: SettingsRepository,
        importer: AuthenticatorImporter
    ): VaultRepository = VaultRepository(tokenDao, settingsRepository, importer)

    @Provides
    @Singleton
    fun provideVaultLockTimeout(
        vaultRepository: VaultRepository,
        timeSource: TimeSource
    ): VaultLockTimeout = VaultLockTimeout(vaultRepository, timeSource)
}
