package com.studiia

import com.typesafe.config.Config
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.kodein.di.*
import org.kodein.type.jvmType
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase

abstract class Controller : DIAware {
	val application: Application by instance()
	abstract fun Route.registerRoutes()
	internal class RouterConsumer internal constructor(private val application: Application) :
		(DI, DI.Key<*, *, *>, List<DIDefinition<*, *, *>>) -> Unit {
		override fun invoke(di: DI, key: DI.Key<*, *, *>, definitions: List<DIDefinition<*, *, *>>) {
			val bindingClass = key.type.jvmType as? Class<*>?
			if (bindingClass != null && Controller::class.java.isAssignableFrom(bindingClass)) {
				val delegatedController by di.Instance(key.type)
				val controller = delegatedController as Controller
				application.routing {
					controller.apply { registerRoutes() }
				}
			}
		}
	}
}

inline fun <reified T : Any> DirectDI.getCollection(): CoroutineCollection<T> = instance<CoroutineDatabase>().getCollection()

interface ModuleProducer {
	fun produceModule(config: Config): DI.Module
}