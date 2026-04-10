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
import timber.log.Timber

class VaultAutofillService : AutofillService() {

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
            val demoCode = "000000"
            val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            presentation.setTextViewText(android.R.id.text1, "AuthVault OTP: $demoCode")

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
