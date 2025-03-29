package ca.rmen.android.poetassistant


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
/**
 * Add this annotation to functions to exclude from jacoco coverage metrics.
 *
 * https://github.com/jacoco/jacoco/wiki/FilteringOptions#annotation-based-filtering
 */
annotation class ExcludeFromJacocoGeneratedReport