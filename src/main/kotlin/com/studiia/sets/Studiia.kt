package com.studiia.sets

import com.studiia.Hexa
import com.studiia.ModuleProducer
import com.typesafe.config.Config
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import kotlin.time.Duration

@Serializable
data class Studiia(
	@SerialName("_id")
	val id: Hexa,
	var name: String,
	var description: String,
	var createdAt: Instant,
	var creator: Hexa,
	var folder: Hexa?,
	val cards: MutableList<Flashcard>,
	val userData: MutableSet<UserData>,
) {

	companion object : ModuleProducer {
		override fun produceModule(config: Config): DI.Module = DI.Module("Studiia") {
			bind<StudiiaService>() with singleton { StudiiaService(di) }

		}

	}

	@Serializable
	data class Flashcard(
		var index: Int,
		var frontText: String,
		var backText: String,
		val openEndedPossibleAnswers: MutableList<String>
	) {
		@Serializable
		data class Create(
			var frontText: String,
			var backText: String,
		) {
			fun toFlashcard(index: Int) = Flashcard(index, frontText, backText, mutableListOf())
		}
	}

	@Serializable
	data class UserData(
		val user: Hexa,
		var review: Review?,
		val testResults: MutableList<TestResult>,
		var matchingPersonalBest: Double
	) {
		@Serializable
		data class Review(
			var rating: Int,
			var comment: String?,
			var timestamp: Instant,
			var reviewer: Hexa,
		) {
			@Serializable
			data class Create(
				var rating: Int,
				var comment: String? = null,
			) {
				fun toReview(reviewer: Hexa) = Review(
					rating = rating,
					comment = comment,
					timestamp = Clock.System.now(),
					reviewer = reviewer,
				)
			}
		}

		@Serializable
		sealed class QuestionResult {
			abstract val card: Int
			abstract val timestamp: Instant
			abstract val approximateAnswerTime: Duration
			abstract val correct: Boolean

			@Serializable
			sealed class Create {
				abstract val card: Int
				abstract val approximateAnswerTime: Duration
			}
		}

		@SerialName("multiple_choice")
		@Serializable
		data class MultipleChoiceCardResult(
			override val card: Int,
			override val timestamp: Instant,
			@Contextual
			override val approximateAnswerTime: Duration,
			override val correct: Boolean,
			val question: String,
			val answer: Int,
			val possibleAnswers: List<String>,
		) : QuestionResult() {
			@SerialName("multiple_choice")
			@Serializable
			data class Create(
				override val card: Int,
				@Contextual
				override val approximateAnswerTime: Duration,
				val question: String,
				val answer: Int,
				val possibleAnswers: List<String>,
			) : QuestionResult.Create()
		}

		@SerialName("open_answer")
		@Serializable
		data class OpenAnswerCardResult(
			override val card: Int,
			override val timestamp: Instant,
			@Contextual
			override val approximateAnswerTime: Duration,
			override val correct: Boolean,
			val question: String,
			val answer: String,
			val expectedAnswer: String,
		) : QuestionResult() {

			@SerialName("open_answer")
			@Serializable
			data class Create(
				override val card: Int,
				@Contextual
				override val approximateAnswerTime: Duration,
				val question: String,
				val answer: String
			) : QuestionResult.Create()

		}

		@Serializable
		@SerialName("matching")
		data class MatchingCardResult(
			override val card: Int = -1, // card # is not relevant for matching
			override val timestamp: Instant,
			@Contextual
			override val approximateAnswerTime: Duration,
			override val correct: Boolean,
			val leftAnswers: List<String>,
			val rightAnswers: List<String>,
			val correctMatching: Map<Int, Int>,
			val userMatching: Map<Int, Int>,
			val cards: List<Int>,
		) : QuestionResult() {

			@Serializable
			@SerialName("matching")
			data class Create(
				override val card: Int = -1,
				@Contextual
				override val approximateAnswerTime: Duration,
				val leftAnswers: List<String>,
				val rightAnswers: List<String>,
				val userMatching: Map<Int, Int>,
				val cards: List<Int>,
			) : QuestionResult.Create()

		}

		@Serializable
		data class TestResult(
			val id: Hexa,
			@Contextual
			val duration: Duration,
			val timeStart: Instant,
			val timeStop: Instant,
			val results: List<QuestionResult>,
			val accuracy: Double
		)

		@Serializable
		data class TestResultCreate(
			val timeStart: Instant,
			val timeStop: Instant,
			val results: List<QuestionResult>
		)
	}

	@Serializable
	data class NonOwnerResponse(
		val id: Hexa,
		val name: String,
		val description: String,
		val createdAt: Instant,
		val creator: Hexa,
		val folder: Hexa?,
		val cards: List<Flashcard>,
		val averageReviews: Double
	)

	fun toNonOwnerResponse(averageReviews: Double): NonOwnerResponse {
		return NonOwnerResponse(
			id = id,
			name = name,
			description = description,
			createdAt = createdAt,
			creator = creator,
			folder = folder,
			cards = cards,
			averageReviews = averageReviews
		)
	}

	@Serializable
	data class Creation(
		val id: Hexa,
		val name: String,
		val description: String,
		val folder: Hexa?
	) {
		fun toStudiia(creator: Hexa) = Studiia(
			id = id,
			name = name,
			description = description,
			createdAt = Clock.System.now(),
			creator = creator,
			folder = folder,
			cards = mutableListOf(),
			userData = mutableSetOf(),
		)
	}

	@Serializable
	data class OpenEndedQuestionResponseInaccuracy(
		val question: String,
		val answer: String,
		val expectedAnswer: String,
		val comment: String?,
		val user: Hexa,
		val timestamp: Instant
	) {
		@Serializable
		data class Create(
			val question: String,
			val answer: String,
			val expectedAnswer: String,
			val comment: String?,
		) {
			fun toOpenEndedQuestionResponseInaccuracy(user: Hexa) = OpenEndedQuestionResponseInaccuracy(
				question = question,
				answer = answer,
				expectedAnswer = expectedAnswer,
				comment = comment,
				user = user,
				timestamp = Clock.System.now()
			)
		}
	}
}