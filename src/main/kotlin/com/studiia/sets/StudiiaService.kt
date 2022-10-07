package com.studiia.sets

import com.studiia.Hexa
import com.studiia.user.UserService
import io.ktor.server.plugins.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.litote.kmongo.coroutine.CoroutineCollection

class StudiiaService(override val di: DI) : DIAware {
	private val studiias by instance<CoroutineCollection<Studiia>>()
	private val userService by instance<UserService>()

	suspend fun find(id: Hexa): Studiia = studiias.findOneById(id)
		?: throw NotFoundException("Studiia with id $id not found")

	suspend fun create(create: Studiia.Create): Studiia.OwnerResponse {
		val hexa = Hexa()
		val studiia = create.toStudiia(hexa)
		studiias.insertOne(studiia)
		return ownerResponse(studiia)
	}

	suspend fun updatePut(id: Hexa, update: Studiia.PutUpdate): Studiia.OwnerResponse {
		val studiia = find(id)
		update.apply(studiia)
		studiias.updateOneById(id, studiia)
		return ownerResponse(studiia)
	}

	suspend fun updatePatch(id: Hexa, update: Studiia.PatchUpdate): Studiia.OwnerResponse {
		val studiia = find(id)
		update.apply(studiia)
		studiias.updateOneById(id, studiia)
		return ownerResponse(studiia)
	}

	suspend fun delete(id: Hexa): Studiia {
		val studiia = find(id)
		studiias.deleteOneById(id)
		return studiia
	}

	fun getReviews(studiia: Studiia): List<Studiia.UserData.Review> =
		studiia.userData.mapNotNull { it.review }

	fun ownerResponse(studiia: Studiia): Studiia.OwnerResponse =
		studiia.toOwnerResponse(getReviews(studiia), studiia.userData.map { it.user })

	suspend fun addReview(id: Hexa, user: Hexa, review: Studiia.UserData.Review.Create): Any {

	}

}