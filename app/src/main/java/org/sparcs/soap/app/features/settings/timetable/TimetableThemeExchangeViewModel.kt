package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.usecases.TimetableThemeUseCase
import javax.inject.Inject

data class ThemeExchangeState(
    val loading: Boolean = false,
    val theme: TimetableTheme? = null,
    val code: String? = null,
    val error: Int? = null,
)

@HiltViewModel
class TimetableThemeExchangeViewModel @Inject constructor(
    private val useCase: TimetableThemeUseCase,
) : ViewModel() {
    var state by mutableStateOf(ThemeExchangeState())
        private set
    private var job: Job? = null

    fun reset() {
        job?.cancel()
        state = ThemeExchangeState()
    }

    fun share(theme: TimetableTheme) = request(theme, R.string.theme_share_error) {
        ThemeExchangeState(theme = theme, code = useCase.share(theme))
    }

    fun fetch(code: String) = request(null, R.string.theme_import_error) {
        ThemeExchangeState(theme = useCase.fetch(code))
    }

    private fun request(theme: TimetableTheme?, errorMessage: Int, action: suspend () -> ThemeExchangeState) {
        job?.cancel()
        state = ThemeExchangeState(loading = true, theme = theme)
        job = viewModelScope.launch {
            try {
                state = action()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                state = ThemeExchangeState(theme = theme, error =
                    if (theme == null && (error is NetworkError.NotFound ||
                        error is NetworkError.ServerError && error.code == 404)) R.string.theme_code_not_found
                    else errorMessage)
            }
        }
    }
}
