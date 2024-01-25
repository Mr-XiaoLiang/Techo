package com.lollipop.techo.util.permission

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import java.util.LinkedList

class PermissionLauncherManager(
    private val activity: AppCompatActivity
) {

    private val launcherMap = StoreManager()

    private val pendingRequester = LinkedList<PermissionRequester>()

    fun register(
        who: Any,
        permission: String,
        @StringRes rationale: Int
    ): PermissionLauncherManager {
        pendingRequester.addLast(PermissionRequester(permission, rationale, who))
        return this
    }

    fun findLauncher(who: Any, permission: String): PermissionLauncher? {
        return launcherMap.get(who).find(permission)
    }

    fun registerLauncher() {
        while (pendingRequester.isNotEmpty()) {
            val requester = pendingRequester.removeFirst()
            val store = launcherMap.get(requester.who)
            store.put(
                requester.permission,
                PermissionLauncher.create(
                    activity,
                    requester.rationaleMessage,
                    requester.permission
                )
            )
        }
    }

    private class LauncherStore {
        private val launcherMap = HashMap<String, PermissionLauncher>()

        fun find(permission: String): PermissionLauncher? {
            return launcherMap[permission]
        }

        fun put(permission: String, launcher: PermissionLauncher) {
            launcherMap[permission] = launcher
        }

    }

    private class StoreManager {
        private val storeMap = HashMap<Any, LauncherStore>()

        fun get(who: Any): LauncherStore {
            synchronized(storeMap) {
                val store = storeMap[who]
                if (store != null) {
                    return store
                }
                val newStore = LauncherStore()
                storeMap[who] = newStore
                return newStore
            }
        }

    }

    data class PermissionRequester(
        val permission: String,
        @StringRes
        val rationaleMessage: Int,
        val who: Any
    )

}