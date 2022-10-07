package com.studiia.user

import com.studiia.Controller
import com.studiia.id
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.*

class UserController(override val di: DI) : Controller() {
	private val service: UserService by instance()
	override fun Route.registerRoutes() {
		route("/api/users") {
			route("/{id}") {
				get {
					val id = call.id()
					call.respond(service.find(id))
				}
				put {
					val id = call.id()
					val user = call.receive<User.PutUpdate>()
					call.respond(service.updatePut(id, user).toCompleteResponse())
				}
				patch {
					val id = call.id()
					val user = call.receive<User.PatchUpdate>()
					call.respond(service.updatePatch(id, user).toCompleteResponse())
				}
				delete {
					val id = call.id()
					call.respond(service.delete(id).toDeleteResponse())
				}
			}
			post {
				val userSignup = call.receive<User.Signup>()
				call.respond(service.create(userSignup))
			}
		}
		route("/verify") {
			route("/{token}") {
				get {
					val token = call.parameters["token"] ?: throw MissingRequestParameterException("token")
					service.finishVerification(token)
					call.respondRedirect("/dashboard")
				}
			}
		}
	}
}
