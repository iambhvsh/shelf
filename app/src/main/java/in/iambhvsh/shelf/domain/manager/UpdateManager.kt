package `in`.iambhvsh.shelf.domain.manager

interface UpdateManager {
    suspend fun checkForUpdates(force: Boolean = false): Boolean
    fun downloadAndInstallUpdate()
}
