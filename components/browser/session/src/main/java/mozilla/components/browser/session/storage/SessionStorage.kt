/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.browser.session.storage

import android.content.Context
import android.support.annotation.CheckResult
import android.support.annotation.VisibleForTesting
import android.support.annotation.WorkerThread
import android.util.AtomicFile
import mozilla.components.browser.session.SessionManager
import mozilla.components.concept.engine.Engine
import org.json.JSONException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val STORE_FILE_NAME_FORMAT = "mozilla_components_session_storage_%s.json"

private val sessionFileLock = Any()

/**
 * Session storage for persisting the state of a [SessionManager] to disk (browser and engine session states).
 */
class SessionStorage(
    private val context: Context,
    private val engine: Engine
) : AutoSave.Storage {
    private val serializer = SnapshotSerializer()

    /**
     * Reads the saved state from disk. Returns null if no state was found on disk or if reading the file failed.
     */
    fun restore(): SessionManager.Snapshot? {
        return readSnapshotFromDisk(getFileForEngine(context, engine), serializer, engine)
    }

    /**
     * Clears the state saved on disk.
     */
    fun clear() {
        removeSnapshotFromDisk(context, engine)
    }

    /**
     * Saves the given state to disk.
     */
    @WorkerThread
    override fun save(snapshot: SessionManager.Snapshot): Boolean {
        if (snapshot.isEmpty()) {
            clear()
            return true
        }

        return saveSnapshotToDisk(getFileForEngine(context, engine), serializer, snapshot)
    }

    /**
     * Starts configuring automatic saving of the state.
     */
    @CheckResult
    fun autoSave(
        sessionManager: SessionManager,
        interval: Long = AutoSave.DEFAULT_INTERVAL_MILLISECONDS,
        unit: TimeUnit = TimeUnit.MILLISECONDS
    ): AutoSave {
        return AutoSave(sessionManager, this, unit.toMillis(interval))
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Suppress("ReturnCount")
internal fun readSnapshotFromDisk(
    file: AtomicFile,
    serializer: SnapshotSerializer,
    engine: Engine
): SessionManager.Snapshot? {
    synchronized(sessionFileLock) {

        try {
            file.openRead().use {
                val json = it.bufferedReader().use { reader -> reader.readText() }

                val snapshot = serializer.fromJSON(engine, json)

                if (snapshot.isEmpty()) {
                    return null
                }

                return snapshot
            }
        } catch (_: IOException) {
            return null
        } catch (_: JSONException) {
            return null
        }
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun saveSnapshotToDisk(
    file: AtomicFile,
    serializer: SnapshotSerializer,
    snapshot: SessionManager.Snapshot
): Boolean {
    require(snapshot.sessions.isNotEmpty()) {
        "SessionsSnapshot must not be empty"
    }
    requireNotNull(snapshot.sessions.getOrNull(snapshot.selectedSessionIndex)) {
        "SessionSnapshot's selected index must be in bounds"
    }

    synchronized(sessionFileLock) {
        var outputStream: FileOutputStream? = null

        return try {
            val json = serializer.toJSON(snapshot)

            outputStream = file.startWrite()
            outputStream.write(json.toByteArray())
            file.finishWrite(outputStream)
            true
        } catch (_: IOException) {
            file.failWrite(outputStream)
            false
        } catch (_: JSONException) {
            file.failWrite(outputStream)
            false
        }
    }
}

private fun removeSnapshotFromDisk(context: Context, engine: Engine) {
    synchronized(sessionFileLock) {
        getFileForEngine(context, engine).delete()
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun getFileForEngine(context: Context, engine: Engine): AtomicFile {
    return AtomicFile(File(context.filesDir, String.format(STORE_FILE_NAME_FORMAT, engine.name()).toLowerCase()))
}
