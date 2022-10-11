package com.studiia.folder

import com.studiia.Hexa
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Folder(
	@SerialName("_id")
	val id: Hexa,
	var name: String,
	var description: String,
	var creator: Hexa,
	var parent: Hexa?,
	val createdAt: Instant,
	val studiias: Set<Hexa>
) {
	@Serializable
	data class Create(
		val name: String,
		val description: String,
		val parent: Hexa?
	)

	@Serializable
	data class PutUpdate(
		val name: String,
		val description: String,
		val parent: Hexa?
	) {
		fun apply(folder: Folder) {
			folder.name = name
			folder.description = description
			folder.parent = parent
		}
	}

	@Serializable
	data class PatchUpdate(
		val name: String?,
		val description: String?,
		val parent: Hexa?
	) {
		fun apply(folder: Folder) {
			name?.let { folder.name = it }
			description?.let { folder.description = it }
			parent?.let { folder.parent = it }
		}
	}
}