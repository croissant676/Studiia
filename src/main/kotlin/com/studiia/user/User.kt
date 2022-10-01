package com.studiia.user

import com.studiia.Hexa
import com.studiia.ModuleProducer
import com.studiia.getCollection
import com.typesafe.config.Config
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.time.Duration.Companion.days

@Serializable
class User(
	@SerialName("_id")
	val id: Hexa,
	var email: String,
	var username: String,
	var password: String,
	var firstName: String,
	var middleName: String?,
	var lastName: String,
	var birthday: LocalDate,
	var profilePicture: String?,
	var createdAt: Instant,
	var topLevelSets: MutableSet<Hexa>,
	var folders: MutableSet<Hexa>,
	var verified: Boolean,
	var privateMode: Boolean,
) {
	companion object : ModuleProducer {
		val VerificationDuration = 1.days
		override fun produceModule(config: Config): DI.Module = DI.Module("user") {
			bind<UserService>() with singleton { UserService(di) }
			bind<UserController>() with singleton { UserController(di) }
			bind<CoroutineCollection<User>>() with singleton { getCollection() }
			bind<CoroutineCollection<Verification>>() with singleton { getCollection() }
		}
	}

	@Serializable
	data class PrivateResponse(
		val id: Hexa,
		val username: String,
		val profilePicture: String?,
		val topLevelSets: Set<Hexa>,
		val folders: Set<Hexa>,
	)

	fun toPrivateResponse() = PrivateResponse(
		id = id,
		username = username,
		profilePicture = profilePicture,
		topLevelSets = topLevelSets,
		folders = folders,
	)

	@Serializable
	data class PublicResponse(
		val id: Hexa,
		val username: String,
		val profilePicture: String?,
		val topLevelSets: Set<Hexa>,
		val folders: Set<Hexa>,
		val email: String,
		val firstName: String,
		val middleName: String?,
		val lastName: String,
		val createdAt: Instant,
	)

	fun toPublicResponse() = PublicResponse(
		id = id,
		username = username,
		profilePicture = profilePicture,
		topLevelSets = topLevelSets,
		folders = folders,
		email = email,
		firstName = firstName,
		middleName = middleName,
		lastName = lastName,
		createdAt = createdAt,
	)

	@Serializable
	data class Signup(
		val email: String,
		val username: String,
		val password: String,
		val firstName: String,
		val middleName: String?,
		val lastName: String,
		val birthday: LocalDate
	) {
		fun toUser(hexa: Hexa) = User(
			id = hexa,
			email = email,
			username = username,
			password = password,
			firstName = firstName,
			middleName = middleName,
			lastName = lastName,
			birthday = birthday,
			profilePicture = null,
			createdAt = Clock.System.now(),
			topLevelSets = mutableSetOf(),
			folders = mutableSetOf(),
			verified = false,
			privateMode = false,
		)
	}

	@Serializable
	class Verification(
		@SerialName("_id")
		val token: String,
		val email: String,
		val expirationTimestamp: Instant,
		val user: Hexa
	)

	fun toVerification(token: String) = Verification(
		token = token,
		email = email,
		expirationTimestamp = Clock.System.now() + VerificationDuration,
		user = id,
	)

}