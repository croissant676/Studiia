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
	var owner: Hexa,
	var folder: Hexa?,
	val cards: MutableList<Flashcard>,
	val userData: MutableSet<UserData>,
) {

	companion object : ModuleProducer {
		override fun produceModule(config: Config): DI.Module = DI.Module("Studiia") {
			bind<StudiiaService>() with singleton { StudiiaService(di) }
			bind<StudiiaController>() with singleton { StudiiaController(di) }
		}
	}

	@Serializable
	data class PatchUpdate(
		val name: String? = null,
		val description: String? = null,
		val folder: Hexa? = null
	) {
		fun apply(studiia: Studiia) {
			name?.let { studiia.name = it }
			description?.let { studiia.description = it }
			folder?.let { studiia.folder = it }
		}
	}

	@Serializable
	data class PutUpdate(
		val name: String,
		val description: String,
		val folder: Hexa?
	) {
		fun apply(studiia: Studiia) {
			studiia.name = name
			studiia.description = description
			studiia.folder = folder
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

		@Serializable
		data class PatchUpdate(
			var frontText: String? = null,
			var backText: String? = null,
		) {
			fun update(flashcard: Flashcard) {
				frontText?.let { flashcard.frontText = it }
				backText?.let { flashcard.backText = it }
			}
		}

		@Serializable
		data class PutUpdate(
			var frontText: String,
			var backText: String,
		) {
			fun update(flashcard: Flashcard) {
				flashcard.frontText = frontText
				flashcard.backText = backText
			}
		}
	}

	@Serializable
	data class UserData(
		val user: Hexa,
		var review: Review?,
		val testResults: MutableList<TestResult>,
		var matchingPersonalBest: Double?,
		var stage: Stage,
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
			abstract val timestamp: Instant
			abstract val approximateAnswerTime: Duration?
			abstract val correct: Boolean

			val multipleChoiceCardResult: MultipleChoiceCardResult?
				get() = this as? MultipleChoiceCardResult
			val openEndedCardResult: OpenAnswerCardResult?
				get() = this as? OpenAnswerCardResult
			val matchingCardResult: MatchingCardResult?
				get() = this as? MatchingCardResult

			@Serializable
			sealed class Create {
				abstract val approximateAnswerTime: Duration?
			}
		}

		@SerialName("multiple_choice")
		@Serializable
		data class MultipleChoiceCardResult(
			override val timestamp: Instant,
			@Contextual
			override val approximateAnswerTime: Duration?,
			override val correct: Boolean,
			val card: Int,
			val question: String,
			val answer: Int,
			val possibleAnswers: List<String>,
		) : QuestionResult() {
			@SerialName("multiple_choice")
			@Serializable
			data class Create(
				@Contextual
				override val approximateAnswerTime: Duration?,
				val card: Int,
				val question: String,
				val answer: Int,
				val possibleAnswers: List<String>,
			) : QuestionResult.Create()
		}

		@SerialName("open_answer")
		@Serializable
		data class OpenAnswerCardResult(
			override val timestamp: Instant,
			@Contextual
			override val approximateAnswerTime: Duration,
			override val correct: Boolean,
			val card: Int,
			val question: String,
			val answer: String,
			val expectedAnswer: String,
		) : QuestionResult() {

			@SerialName("open_answer")
			@Serializable
			data class Create(
				@Contextual
				override val approximateAnswerTime: Duration,
				val card: Int,
				val question: String,
				val answer: String
			) : QuestionResult.Create()

		}

		@Serializable
		@SerialName("matching")
		data class MatchingCardResult(
			override val timestamp: Instant,
			@Contextual
			override val approximateAnswerTime: Duration,
			override val correct: Boolean,
			val leftAnswers: List<String>,
			val rightAnswers: List<String>,
			val correctMatching: Map<Char, Char>,
			val userMatching: Map<Char, Char>,
			val cards: List<Int>,
		) : QuestionResult() {

			@Serializable
			@SerialName("matching")
			data class Create(
				@Contextual
				override val approximateAnswerTime: Duration?,
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
			val duration: Duration?,
			val timeStart: Instant,
			val timeStop: Instant,
			val results: List<QuestionResult>,
			val accuracy: Double
		) {

			@Serializable
			data class Create(
				val timeStart: Instant,
				val timeStop: Instant,
				val results: List<QuestionResult.Create>
			)
		}

		@Serializable
		data class OwnerCopy(
			val user: Hexa,
			val review: Review?,
			val stage: Stage,
			val matchingPersonalBest: Double?
		)

		fun toOwnerCopy() = OwnerCopy(
			user = user,
			review = review,
			stage = stage,
			matchingPersonalBest = matchingPersonalBest
		)

		@Serializable
		enum class Stage {
			Learning,
			Reviewing,
			Mastered;
		}
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
			creator = owner,
			folder = folder,
			cards = cards,
			averageReviews = averageReviews
		)
	}

	@Serializable
	data class OwnerResponse(
		val id: Hexa,
		val name: String,
		val description: String,
		val createdAt: Instant,
		val creator: Hexa,
		val folder: Hexa?,
		val cards: List<Flashcard>,
		val reviews: List<UserData.Review>,
		val users: List<Hexa>
	)

	fun toOwnerResponse(reviews: List<UserData.Review>, users: List<Hexa>): OwnerResponse {
		return OwnerResponse(
			id = id,
			name = name,
			description = description,
			createdAt = createdAt,
			creator = owner,
			folder = folder,
			cards = cards,
			reviews = reviews,
			users = users
		)
	}

	@Serializable
	data class Create(
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
			owner = creator,
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