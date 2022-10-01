package com.studiia.user

import com.studiia.Controller
import com.studiia.id
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.*

class UserController(override val di: DI) : Controller() {
	private val userService: UserService by instance()
	override fun Route.registerRoutes() {
		route("/users") {
			get("/{id}") {
				val id = call.id()
				call.respond(userService.find(id))
			}

		}
	}
}
