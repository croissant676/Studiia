package com.studiia.sets

import com.studiia.Controller
import com.studiia.Hexa
import com.studiia.id
import com.studiia.user.User
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class StudiiaController(override val di: DI) : Controller() {
	private val service: StudiiaService by instance()
	override fun Route.registerRoutes() {
		route("/api/studiias") {
			authenticate {
				route("/{id}") {
					get {
						val id = call.id()
						call.respond(service.find(id))
					}
					put {
						val id = call.id()
						val studiia = call.receive<Studiia.PutUpdate>()
						call.respond(service.updatePut(id, studiia))
					}
					patch {
						val id = call.id()
						val studiia = call.receive<Studiia.PatchUpdate>()
						call.respond(service.updatePatch(id, studiia))
					}
					delete {
						val id = call.id()
						call.respond(service.delete(id))
					}
					route("/reviews") {
						get {
							val id = call.id()
							val user = call.principal<User>()
							call.respond(service.getReviews(id, user!!.id))
						}
						post {
							val id = call.id()
							val user = call.principal<User>()
							val review = call.receive<Studiia.UserData.Review.Create>()
							call.respond(service.addReview(id, user!!.id, review))
						}
					}
				}
				post {
					val studiiaSignup = call.receive<Studiia.Create>()
					call.respond(service.create(studiiaSignup))
				}
			}
		}
	}
}