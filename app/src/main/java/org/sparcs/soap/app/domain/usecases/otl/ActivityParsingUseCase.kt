package org.sparcs.soap.app.domain.usecases.otl

import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.helpers.ActivityDescriptionScanner
import org.sparcs.soap.app.domain.models.otl.ActivityParsingError
import org.sparcs.soap.app.domain.models.otl.ActivityParsingException
import org.sparcs.soap.app.domain.models.otl.ParsedActivity
import timber.log.Timber
import javax.inject.Inject

interface ActivityParsingUseCaseProtocol {
    suspend fun isAvailable(): Boolean
    suspend fun parse(description: String, today: DayType): ParsedActivity
}

class ActivityParsingUseCase @Inject constructor() : ActivityParsingUseCaseProtocol {
    override suspend fun isAvailable(): Boolean {
        val model = Generation.getClient()
        return try {
            model.checkStatus() == FeatureStatus.AVAILABLE
        } catch (error: GenAiException) {
            Timber.w("Activity parsing availability failed: code=%d", error.errorCode)
            false
        } finally {
            model.close()
        }
    }

    override suspend fun parse(description: String, today: DayType): ParsedActivity {
        val request = description.trim().take(MAXIMUM_DESCRIPTION_LENGTH)
        if (request.isEmpty()) throw ActivityParsingException(ActivityParsingError.NOTHING_FOUND)
        val model = Generation.getClient()
        val response = try {
            if (model.checkStatus() != FeatureStatus.AVAILABLE) {
                throw ActivityParsingException(ActivityParsingError.UNAVAILABLE)
            }
            val prompt = generateContentRequest(TextPart("$INSTRUCTIONS\n\nDescription: $request")) {
                temperature = 0f
                maxOutputTokens = 256
            }
            model.generateContent(prompt).candidates.firstOrNull()?.text.orEmpty()
        } catch (error: GenAiException) {
            Timber.w("Activity parsing failed: code=%d", error.errorCode)
            throw ActivityParsingException(error.parsingError)
        } finally {
            model.close()
        }
        return ActivityResponseParser.parse(response, request, today)
    }

    private val GenAiException.parsingError: ActivityParsingError
        get() = when (errorCode) {
            GenAiException.ErrorCode.REQUEST_PROCESSING_ERROR,
            GenAiException.ErrorCode.RESPONSE_GENERATION_ERROR,
            GenAiException.ErrorCode.RESPONSE_PROCESSING_ERROR,
                -> ActivityParsingError.UNSAFE_REQUEST

            GenAiException.ErrorCode.NOT_AVAILABLE,
            GenAiException.ErrorCode.NOT_SUPPORTED,
            GenAiException.ErrorCode.AICORE_INCOMPATIBLE,
            GenAiException.ErrorCode.NEEDS_SYSTEM_UPDATE,
                -> ActivityParsingError.UNAVAILABLE

            else -> ActivityParsingError.FAILED
        }

    private companion object {
        const val MAXIMUM_DESCRIPTION_LENGTH = 200
        val INSTRUCTIONS = """
            You fill in a form for a weekly activity on a university student's timetable, from a description they type.
            Answer with only a JSON object with these keys: "title", "location", "days", "startHour", "startMinute", "endHour", "endMinute", "durationMinutes".
            Give the title as a short phrase in the words they used, leaving out the day, the time, the place and words that only say it repeats, such as every or weekly.
            Give the location only if they say where it happens: the name of a club, team or event is not a place.
            Give "days" as English day names such as "Tuesday", only the days they name: all seven for every day, Monday to Friday for weekdays.
            Give times on a 24-hour clock: afternoon and evening times are 12 or more, so 7 in the evening is 19.
            Give an end time only if they give one, and a length in minutes only if they give one instead.
            Use null for anything they don't say rather than guessing.
            Keep the title and the location in the language they wrote in.
        """.trimIndent()
    }
}

internal object ActivityResponseParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Serializable
    private data class Response(
        val title: String? = null,
        val location: String? = null,
        val days: List<String> = emptyList(),
        val startHour: Int? = null,
        val startMinute: Int? = null,
        val endHour: Int? = null,
        val endMinute: Int? = null,
        val durationMinutes: Int? = null,
    )

    fun parse(response: String, description: String, today: DayType): ParsedActivity {
        val generated = response.substringAfter('{', "").substringBeforeLast('}', "")
            .takeIf { it.isNotBlank() }
            ?.let { runCatching { json.decodeFromString<Response>("{$it}") }.getOrNull() }
            ?: Response()
        val modelReading = ParsedActivity.fromClock(
            title = generated.title,
            location = generated.location,
            startHour = generated.startHour,
            startMinute = generated.startMinute,
            endHour = generated.endHour,
            endMinute = generated.endMinute,
            durationMinutes = generated.durationMinutes,
        )
        val scannedDays = ActivityDescriptionScanner.days(description, today)
        val scannedTimes = ActivityDescriptionScanner.allTimes(description)
        val first = scannedTimes.firstOrNull()
        val parsed = modelReading.copy(
            days = scannedDays.ifEmpty { generated.days.mapNotNull(::dayType).distinct() },
            begin = first?.begin ?: modelReading.begin,
            end = if (first == null) modelReading.end else first.end ?: lengthEnd(first.begin, generated.durationMinutes),
            laterTimes = scannedTimes.drop(1).map {
                ParsedActivity.Time(it.begin, it.end ?: lengthEnd(it.begin, generated.durationMinutes))
            },
        )
        if (parsed.isEmpty) throw ActivityParsingException(ActivityParsingError.NOTHING_FOUND)
        return parsed
    }

    private fun lengthEnd(begin: Int, durationMinutes: Int?): Int? =
        durationMinutes?.takeIf { it > 0 }?.let { minOf(begin + it, 24 * 60) }

    private fun dayType(name: String): DayType? = when (name.trim().lowercase().take(3)) {
        "mon" -> DayType.MON
        "tue" -> DayType.TUE
        "wed" -> DayType.WED
        "thu" -> DayType.THU
        "fri" -> DayType.FRI
        "sat" -> DayType.SAT
        "sun" -> DayType.SUN
        else -> null
    }
}
