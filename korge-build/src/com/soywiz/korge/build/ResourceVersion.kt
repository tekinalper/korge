package com.soywiz.korge.build

import com.soywiz.korio.crypto.AsyncHash
import com.soywiz.korio.crypto.hash
import com.soywiz.korio.serialization.json.Json
import com.soywiz.korio.util.toHexStringLower
import com.soywiz.korio.vfs.VfsFile

data class ResourceVersion(val name: String, val loaderVersion: Int, val sha1: String, val configSha1: String = "") {
	suspend fun writeMeta(metaFile: VfsFile) {
		metaFile.writeString(Json.encode(this))
	}

	companion object {
		suspend fun fromFile(file: VfsFile, loaderVersion: Int): ResourceVersion {
			val configFile = file.appendExtension("config")
			val hash = file.readBytes().hash(AsyncHash.SHA1).toHexStringLower()
			val configHash = if (configFile.exists()) configFile.readBytes().hash(AsyncHash.SHA1).toHexStringLower() else ""
			return ResourceVersion(file.basename, loaderVersion, hash, configHash)
		}

		suspend fun readMeta(metaFile: VfsFile): ResourceVersion {
			return Json.decodeToType<ResourceVersion>(metaFile.readString())
		}
	}
}
