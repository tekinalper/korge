package korlibs.modules

import korlibs.*
import org.gradle.api.*
import org.gradle.plugins.signing.*

fun Project.configureSigning() { //= doOncePerProject("configureSigningOnce") {
//fun Project.configureSigning() {
    //println("configureSigning: $this")
	val signingSecretKeyRingFile = System.getenv("ORG_GRADLE_PROJECT_signingSecretKeyRingFile") ?: project.findProperty("signing.secretKeyRingFile")?.toString()

	// gpg --armor --export-secret-keys foobar@example.com | awk 'NR == 1 { print "signing.signingKey=" } 1' ORS='\\n'
	val signingKey = System.getenv("ORG_GRADLE_PROJECT_signingKey") ?: project.findProperty("signing.signingKey")?.toString()
	val signingPassword = System.getenv("ORG_GRADLE_PROJECT_signingPassword") ?: project.findProperty("signing.password")?.toString()

	if (signingSecretKeyRingFile == null && signingKey == null) {
        doOnce("signingWarningLogged") {
            logger.info("WARNING! Signing not configured due to missing properties/environment variables like signing.keyId or ORG_GRADLE_PROJECT_signingKey. This is required for deploying to Maven Central. Check README for details")
        }
	} else {
        plugins.apply("signing")

        afterEvaluate {
            //println("configuring signing for $this")
            signing.apply {
                // This might be duplicated for korge-gradle-plugin? : Signing plugin detected. Will automatically sign the published artifacts.
                try {
                    sign(publishing.publications)
                } catch (e: GradleException) {
                }
                if (signingKey != null) {
                    useInMemoryPgpKeys(signingKey, signingPassword)
                }
                project.gradle.taskGraph.whenReady {

                }
            }
        }
    }
}

val Project.signing get() = extensions.getByType<SigningExtension>()
