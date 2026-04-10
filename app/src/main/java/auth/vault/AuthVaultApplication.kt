package auth.vault

import android.app.Application
import android.view.WindowManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject
import auth.vault.util.GlobalExceptionHandler
import auth.vault.lifecycle.AppLifecycleObserver
import auth.vault.data.repository.VaultRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class AuthVaultApplication : Application() {

    @Inject
    lateinit var crashHandler: GlobalExceptionHandler

    @Inject
    lateinit var lifecycleObserver: AppLifecycleObserver

    @Inject
    lateinit var vaultRepository: VaultRepository

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        crashHandler.init()
    }
}

class ReleaseTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= 3) {
            super.log(priority, tag, message, t)
        }
    }
}
