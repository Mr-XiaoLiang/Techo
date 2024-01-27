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
        permissions: Array<String>,
        @StringRes rationale: Int,
        anyOne: Boolean = true
    ): PermissionLauncherManager {
        if (permissions.isEmpty()) {
            return this
        }
        pendingRequester.addLast(
            PermissionRequester(
                permissions = permissions,
                rationaleMessage = rationale,
                who = who,
                anyOne = anyOne
            )
        )
        return this
    }

    fun findLauncher(who: Any, permissions: Array<String>): PermissionLauncher? {
        return launcherMap.get(who).find(permissions)
    }

    fun registerLauncher() {
        while (pendingRequester.isNotEmpty()) {
            val requester = pendingRequester.removeFirst()
            val store = launcherMap.get(requester.who)

            if (requester.permissions.size == 1) {
                registerSingleLauncher(store, requester.permissions[0], requester)
            } else if (requester.permissions.size > 1) {
                store.put(
                    requester.permissions,
                    if (requester.anyOne) {
                        PermissionLauncher.MultipleByOr(
                            activity,
                            requester.rationaleMessage,
                            requester.permissions
                        )
                    } else {
                        PermissionLauncher.MultipleByAnd(
                            activity,
                            requester.rationaleMessage,
                            requester.permissions
                        )
                    }
                )
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
        private val launcherList = ArrayList<LauncherMultipleInfo>()

        fun find(permission: String): PermissionLauncher? {
            if (permission.isBlank()) {
                return null
            }
            return launcherMap[permission]
        }

        fun find(permissions: Array<String>): PermissionLauncher? {
            val count = permissions.size
            if (count < 1) {
                return null
            }
            if (count == 1) {
                return find(permissions[0])
            }
            val permissionsSet = HashSet<String>()
            permissionsSet.addAll(permissions)
            for (info in launcherList) {
                if (arrayEquals(permissionsSet, info.permissions)) {
                    return info.launcher
                }
            }
            return null
        }

        private fun arrayEquals(array1: Set<String>, array2: Set<String>): Boolean {
            if (array1.size != array2.size) {
                // 长度不一样，就直接忽略
                return false
            }
            for (str in array1) {
                if (!array2.contains(str)) {
                    return false
                }
            }
            return true
        }

        fun put(permission: String, launcher: PermissionLauncher) {
            if (permission.isBlank()) {
                return
            }
            launcherMap[permission] = launcher
        }

        fun put(permissions: Array<String>, launcher: PermissionLauncher) {
            if (permissions.isEmpty()) {
                return
            }
            if (permissions.size == 1) {
                put(permissions[0], launcher)
                return
            }
            val permissionsSet = HashSet<String>()
            permissionsSet.addAll(permissions)
            if (permissionsSet.size == 1) {
                val array = permissionsSet.toTypedArray()
                put(array[0], launcher)
                return
            }
            launcherList.add(LauncherMultipleInfo(permissionsSet, launcher))
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

    private class LauncherMultipleInfo(
        val permissions: Set<String>,
        val launcher: PermissionLauncher
    )

    class PermissionRequester(
        @StringRes
        val rationaleMessage: Int,
        val who: Any,
        val permissions: Array<String>,
        val anyOne: Boolean
    )

}