package ai.reslt.kmpready.core.security

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

/**
 * Tracks the resumed Activity (weakly) so shared code can show BiometricPrompt without
 * holding an Activity reference in DI. MainActivity must extend FragmentActivity.
 */
internal class CurrentActivityTracker(application: Application) : Application.ActivityLifecycleCallbacks {
    private var current: WeakReference<Activity>? = null

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    val fragmentActivity: FragmentActivity? get() = current?.get() as? FragmentActivity

    override fun onActivityResumed(activity: Activity) {
        current = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (current?.get() === activity) current = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
