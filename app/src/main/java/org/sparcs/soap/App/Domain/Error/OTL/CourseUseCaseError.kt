package org.sparcs.soap.App.Domain.Error.OTL

import androidx.annotation.StringRes
import org.sparcs.soap.App.Domain.Error.ErrorSource
import org.sparcs.soap.App.Domain.Error.SourcedError
import org.sparcs.soap.R
import java.io.Serializable

sealed class CourseUseCaseError : Exception(), SourcedError, Serializable {
    override val source: ErrorSource = ErrorSource.UseCase

    data class Unknown(val underlying: Throwable?) : CourseUseCaseError()

    @get:StringRes
    val messageRes: Int
        get() = when (this) {
            is Unknown -> R.string.error_unknown_try_again
        }
}