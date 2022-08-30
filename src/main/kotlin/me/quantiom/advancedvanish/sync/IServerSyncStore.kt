package me.quantiom.advancedvanish.sync

import java.util.*

interface IServerSyncStore {
    fun setup(): Boolean
    fun close()

    fun get(key: UUID): Boolean
    fun setAsync(key: UUID, value: Boolean)
}