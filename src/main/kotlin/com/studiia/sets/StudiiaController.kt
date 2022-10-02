package com.studiia.sets

import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class StudiiaController(override val di: DI) : DIAware {
	private val service: StudiiaService by instance()

}