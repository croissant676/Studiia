package com.studiia.folder

import com.studiia.Hexa
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Folder(
	@SerialName("_id")
	val id: Hexa,

)