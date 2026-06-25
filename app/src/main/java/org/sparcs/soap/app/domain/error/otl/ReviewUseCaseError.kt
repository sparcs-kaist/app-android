package org.sparcs.soap.app.domain.error.otl

import androidx.annotation.StringRes
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.error.ErrorSource
import org.sparcs.soap.app.domain.error.SourcedError
import java.io.Serializable

sealed class ReviewUseCaseError : Exception(), SourcedError, Serializable {
    override val source: ErrorSource = ErrorSource.UseCase

    data class Unknown(val underlying: Throwable?) : ReviewUseCaseError()

    @get:StringRes
    val messageRes: Int
        get() = when (this) {
            is Unknown -> R.string.error_unknown_try_again
        }
}