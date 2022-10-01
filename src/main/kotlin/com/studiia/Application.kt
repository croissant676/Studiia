package com.studiia

import com.studiia.user.User
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.axay.simplekotlinmail.delivery.MailerManager
import net.axay.simplekotlinmail.delivery.mailerBuilder
import org.kodein.di.DI
import org.kodein.di.DIDefinition
import org.kodein.di.bind
import org.kodein.di.instance
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.simplejavamail.api.email.EmailPopulatingBuilder
import org.slf4j.event.Level

val logger = KotlinLogging.logger {}

fun main() {
	val config = loadConfig()
	embeddedServer(Netty, port = config.getInt("ktor.deployment.port")) {
		val di = initDI(config)
		di.indexBeans(
			Controller.RouterConsumer(this@embeddedServer)
		)
		installPlugins()
	}.start(wait = true)
}

private fun loadConfig(): Config {
	val config = ConfigFactory.load()
	for ((key, value) in config.getConfig("ktor").entrySet()) {
		System.setProperty("io.ktor.$key", value.unwrapped().toString())
	}
	return config
}

fun Application.initDI(config: Config): DI = DI {
	bind<Config>() with instance(config)
	bind<Application>() with instance(this@initDI)
	bind<CoroutineDatabase>() with instance(createDatabase(config))
	val moduleProducers = listOf(User)
	for (moduleProducer in moduleProducers) {
		import(moduleProducer.produceModule(config))
	}
}

internal typealias DITreeConsumer = DI.(DI.Key<*, *, *>, List<DIDefinition<*, *, *>>) -> Unit

fun DI.indexBeans(vararg consumers: DITreeConsumer = arrayOf()): List<DITreeConsumer> {
	val consumerList = consumers.toList()
	for ((key, diDefinitions) in container.tree.bindings) {
		for (consumer in consumerList) {
			this@indexBeans.consumer(key, diDefinitions)
		}
	}
	return consumerList
}

fun Application.installPlugins() {
	install(ContentNegotiation) {
		json()
	}
	install(CallLogging) {
		level = Level.INFO
		logger = KotlinLogging.logger("call-logger")
	}
	install(CallId) {
		header(HttpHeaders.XRequestId)
		generate { secureRandom.nextBytes(16).encodeBase64() }
	}
	install(AutoHeadResponse)
	install(DoubleReceive)
	install(Compression)
	install(StatusPages) {
		exception<Throwable> { call, cause ->
			call.respondException(cause)
		}
	}
}

open class StatusException(val status: HttpStatusCode, message: String) : Exception(message)

private fun deduceProperHttpStatus(exception: Throwable): HttpStatusCode = when (exception) {
	is StatusException -> exception.status
	is NotFoundException -> HttpStatusCode.NotFound
	is UnsupportedMediaTypeException -> HttpStatusCode.UnsupportedMediaType
	is BadRequestException -> HttpStatusCode.BadRequest
	is IllegalArgumentException -> HttpStatusCode.BadRequest
	is ContentTransformationException -> HttpStatusCode.BadRequest
	else -> HttpStatusCode.InternalServerError
}

private suspend fun ApplicationCall.respondException(throwable: Throwable) {
	val status = deduceProperHttpStatus(throwable)
	val response = mapOf(
		"timestamp" to Clock.System.now().toString(),
		"status" to status.value,
		"error" to status.description,
		"message" to throwable.message,
		"path" to request.path()
	)
	respond(status, response)
}

fun createDatabase(config: Config): CoroutineDatabase {
	val string = try {
		config.getString("ktor.mongo.connectionString")
	} catch (e: Exception) {
		"mongodb://localhost:27017"
	}
	val client = KMongo.createClient(string)
	return client.getDatabase("studiia").coroutine
}

private var emailUsername: String? = null

fun registerMailer(config: Config) {
	if (emailUsername != null) return
	val host = config.getString("email.host")
	val port = config.getInt("email.port")
	val password = config.getString("email.password")
	emailUsername = config.getString("email.username")
	MailerManager.defaultMailer = mailerBuilder(host, port, emailUsername, password) {
		val propsConfig = config.getConfig("email.props")
			?: return@mailerBuilder
		val propsAsMap = propsConfig.entrySet().associate { it.key to propsConfig.getString(it.key) }
		for ((string, configuredValue) in propsAsMap) {
			properties["mail.$string"] = configuredValue
		}
		logger.debug { "Inserted properties $propsAsMap into mailer.properties" }
	}
}

fun EmailPopulatingBuilder.from() {
	emailUsername?.let { from(it) }
}