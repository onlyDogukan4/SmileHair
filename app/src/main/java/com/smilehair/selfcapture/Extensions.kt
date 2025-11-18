package com.smilehair.selfcapture

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Extension: ML Kit Task'larını Coroutine'ler için await edilebilir hale getirir
suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result -> cont.resume(result) }
        addOnFailureListener { e -> cont.resumeWithException(e) }
    }
}
