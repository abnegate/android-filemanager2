package com.jakebarnby.filemanager.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jakebarnby.filemanager.data.FileDatabase
import java.lang.Exception
import javax.inject.Inject

abstract class FileTreeWalkerWorker<T>(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val ROOT_PATH_KEY = "root_path"
    }

    @Inject
    lateinit var db: FileDatabase

    override suspend fun doWork(): Result {
        val path = inputData.getString(ROOT_PATH_KEY)
            ?: return Result.failure()

        return try {
            readFileTree(getRootNode(path))
            Result.success()
        } catch (ex: Exception) {
            ex.printStackTrace()
            Result.failure()
        }
    }

    @Throws
    abstract suspend fun getRootNode(rootPath: String): T?

    @Throws
    abstract suspend fun readFileTree(rootNode: T?)
}