package com.studiia.user

import com.studiia.Hexa
import com.studiia.coroutineScope
import com.studiia.from
import com.studiia.secureRandom
import io.ktor.server.plugins.*
import io.ktor.util.*
import kotlinx.coroutines.launch
import net.axay.simplekotlinmail.delivery.send
import net.axay.simplekotlinmail.email.emailBuilder
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.setTo

class UserService(override val di: DI) : DIAware {
	private val database: CoroutineCollection<User> by instance()
	private val verificationDatabase: CoroutineCollection<User.Verification> by instance()

	suspend fun find(id: Hexa) = database.findOneById(id)
		?: throw NotFoundException("User with id $id not found")

	suspend fun findVerification(token: String) = verificationDatabase.findOneById(token)
		?: throw NotFoundException("Verification with token $token not found. It may have been used or expired.")

	suspend fun create(signup: User.Signup): User {
		val hexa = Hexa()
		val user = signup.toUser(hexa)
		database.insertOne(user)
		coroutineScope.launch {
			createVerification(user)
		}
		return user
	}

	suspend fun createVerification(user: User): User.Verification {
		val token = secureRandom.nextBytes(24).encodeBase64()
		val verification = user.toVerification(token)
		verificationDatabase.insertOne(verification)
		sendEmail(verification)
		return verification
	}

	suspend fun sendEmail(verification: User.Verification) {
		emailBuilder {
			from()
			to(verification.email)
			withSubject("Verify your email")
			withPlainText("Verify your email by clicking this link: http://localhost:8080/verify/${verification.token}")
		}.send()
	}

	suspend fun finishVerification(token: String) {
		val verification = findVerification(token)
		verificationDatabase.deleteOneById(token)
		database.updateOneById(verification.user, User::verified setTo true)
	}

	suspend fun delete(id: Hexa): User {
		val user = find(id)
		database.deleteOneById(id)
		return user
	}

	suspend fun updatePatch(id: Hexa, patch: User.PatchUpdate): User {
		val user = find(id)
		user.applyPatch(patch)
		database.updateOneById(id, user)
		return user
	}

	suspend fun updatePut(id: Hexa, put: User.PutUpdate): User {
		val user = find(id)
		user.applyPut(put)
		database.updateOneById(id, user)
		return user
	}

}
