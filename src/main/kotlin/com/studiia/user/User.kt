package com.studiia.user

import com.studiia.Hexa
import com.studiia.ModuleProducer
import com.studiia.encode
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
data class User(
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

	// don't expose password
	@Serializable
	data class CompleteResponse(
		val id: Hexa,
		val username: String,
		val profilePicture: String?,
		val topLevelSets: Set<Hexa>,
		val folders: Set<Hexa>,
		val email: String,
		val firstName: String,
		val middleName: String?,
		val lastName: String,
		val birthday: LocalDate,
		val createdAt: Instant,
		val verified: Boolean,
		val privateMode: Boolean
	)

	fun toCompleteResponse() = CompleteResponse(
		id = id,
		username = username,
		profilePicture = profilePicture,
		topLevelSets = topLevelSets,
		folders = folders,
		email = email,
		firstName = firstName,
		middleName = middleName,
		lastName = lastName,
		birthday = birthday,
		createdAt = createdAt,
		verified = verified,
		privateMode = privateMode
	)

	@Serializable
	data class Signup(
		val email: String,
		val username: String,
		val password: String,
		val firstName: String,
		val middleName: String? = null,
		val lastName: String,
		val birthday: LocalDate
	) {
		suspend fun toUser(hexa: Hexa) = User(
			id = hexa,
			email = email,
			username = username,
			password = password.encode(),
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
	data class Verification(
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

	@Serializable
	data class PatchUpdate(
		var email: String? = null,
		var username: String? = null,
		var password: String? = null,
		var firstName: String? = null,
		var middleName: String? = null,
		var lastName: String? = null,
		var birthday: LocalDate? = null,
		var profilePicture: String? = null,
	)

	suspend fun applyPatch(patchUpdate: PatchUpdate) {
		patchUpdate.email?.let { email = it }
		patchUpdate.username?.let { username = it }
		patchUpdate.password?.let { password = it.encode() }
		patchUpdate.firstName?.let { firstName = it }
		patchUpdate.middleName?.let { middleName = it }
		patchUpdate.lastName?.let { lastName = it }
		patchUpdate.birthday?.let { birthday = it }
		patchUpdate.profilePicture?.let { profilePicture = it }
	}

	@Serializable
	data class PutUpdate(
		var email: String,
		var username: String,
		var password: String,
		var firstName: String,
		var middleName: String?,
		var lastName: String,
		var birthday: LocalDate,
		var profilePicture: String?
	)

	suspend fun applyPut(putUpdate: PutUpdate) {
		email = putUpdate.email
		username = putUpdate.username
		password = putUpdate.password.encode()
		firstName = putUpdate.firstName
		middleName = putUpdate.middleName
		lastName = putUpdate.lastName
		birthday = putUpdate.birthday
		profilePicture = putUpdate.profilePicture
	}

	@Serializable
	data class DeleteResponse(
		val id: Hexa,
		val username: String,
		val deletedSets: Set<Hexa>,
		val deletedFolders: Set<Hexa>
	)

	fun toDeleteResponse() = DeleteResponse(
		id = id,
		username = username,
		deletedSets = topLevelSets,
		deletedFolders = folders
	)

}