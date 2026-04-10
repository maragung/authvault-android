package auth.vault.autofill

import android.annotation.SuppressLint
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.SingletonComponent
import auth.vault.data.local.dao.TokenDao
import auth.vault.domain.usecase.TotpGenerator
import auth.vault.util.TimeSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AutofillServiceEntryPoint {
    fun tokenDao(): TokenDao
}

class VaultAutofillService : AutofillService() {

    private val totpGenerator = TotpGenerator()
    private val timeSource = TimeSource()

    @SuppressLint("NewApi")
    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            callback.onFailure(null)
            return
        }

        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                AutofillServiceEntryPoint::class.java
            )
            val tokenDao = entryPoint.tokenDao()
            val tokens = runBlocking { tokenDao.getAllTokens().first() }

            if (tokens.isEmpty()) {
                callback.onFailure(null)
                return
            }

            val adjustedTime = timeSource.currentSeconds()
            val firstToken = tokens.first()
            val code = totpGenerator.generateCode(
                firstToken.secretKey,
                adjustedTime,
                firstToken.digitCount,
                firstToken.algorithm
            )

            val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            presentation.setTextViewText(android.R.id.text1, "AuthVault OTP: $code")

            val response = FillResponse.Builder()
                .addDataset(
                    android.service.autofill.Dataset.Builder()
                        .build()
                )
                .build()

            callback.onSuccess(response)
        } catch (e: Exception) {
            Timber.e(e, "Autofill response failed")
            callback.onFailure(null)
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        callback.onSuccess()
    }
}
