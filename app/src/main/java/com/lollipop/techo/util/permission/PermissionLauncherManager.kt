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
            when (requester) {
                is PermissionRequester.Single -> {
                    registerSingleLauncher(store, requester.permission, requester)
                }

                is PermissionRequester.Multiple -> {
                    if (requester.permissions.size == 1) {
                        registerSingleLauncher(store, requester.permissions[0], requester)
                    } else if (requester.permissions.size > 1) {
                        val key = StringBuilder()
                        for (permission in requester.permissions) {
                            key.append(permission)
                        }
                        // TODO 这个Key怎么保证拼接的顺序和读取的时候顺序一致？Map怎么定位呢？
                    }
                }
            }
        }
    }

    private fun registerSingleLauncher(
        store: LauncherStore,
        permission: String,
        requester: PermissionRequester
    ) {
        store.put(
            permission,
            PermissionLauncher.Single(
                activity,
                requester.rationaleMessage,
                permission
            )
        )
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

    sealed class PermissionRequester(
        @StringRes
        val rationaleMessage: Int,
        val who: Any
    ) {

        class Single(
            @StringRes
            rationaleMessage: Int,
            who: Any,
            val permission: String,
        ) : PermissionRequester(rationaleMessage, who)

        class Multiple(
            @StringRes
            rationaleMessage: Int,
            who: Any,
            val permissions: Array<String>,
            val anyOne: Boolean
        ) : PermissionRequester(rationaleMessage, who)

    }

}