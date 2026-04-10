package auth.vault.util

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalExceptionHandler @Inject constructor(
    private val dispatchProvider: DispatchProvider
) : Thread.UncaughtExceptionHandler {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    fun init() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Timber.e(e, "Uncaught exception on thread: ${t.name}")
        defaultHandler?.uncaughtException(t, e)
    }
}
