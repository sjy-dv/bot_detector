fun isBot(userAgent: String): Boolean {
    val botRegex = Regex("bot|crawl|slurp|spider", RegexOption.IGNORE_CASE)
    return botRegex.containsMatchIn(userAgent)
}
///////

import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

suspend fun botDetectionMiddleware(context: PipelineContext<Unit, ApplicationCall>) {
    val userAgent = context.call.request.headers["User-Agent"]
    if (userAgent != null && isBot(userAgent)) {
        context.call.respond(HttpStatusCode.Forbidden, "Access denied")
    } else {
        context.proceed()
    }
}

//////////////
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
            intercept(ApplicationCallPipeline.Call) {
                botDetectionMiddleware(this)
            }
            get("/") {
                call.respondText("Hello, world!")
            }
        }
    }.start(wait = true)
}