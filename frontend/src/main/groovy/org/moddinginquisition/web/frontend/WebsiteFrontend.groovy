/*
 * MIT License
 *
 * Copyright (c) 2022 Modding Inquisition
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.moddinginquisition.web.frontend

import com.google.gson.JsonObject
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HttpCode
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.rendering.vue.JavalinVue
import io.javalin.plugin.rendering.vue.VueComponent
import org.kohsuke.github.GitHubBuilder
import org.moddinginquisition.web.common.util.ErrorResponder
import org.moddinginquisition.web.common.util.Role

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.util.function.Consumer

@Slf4j
@CompileStatic
class WebsiteFrontend {
    private static final HttpClient CLIENT = HttpClient.newHttpClient()

    private static Configuration conf

    static void main(String[] args) throws Exception {
        JavalinVue.rootDirectory {
            it.classpathPath('/vue', WebsiteFrontend)
        }

        conf = Configuration.read(Path.of('config.json'))
        final app = Javalin.create() {
            it.enableWebjars()
            it.addStaticFiles('/public', Location.CLASSPATH)

            it.accessManager((handler, ctx, routeRoles) -> {
                if (routeRoles.contains(Role.JANITOR) && 'janitor' !in getRoles(ctx)) {
                    ctx.status(HttpCode.FORBIDDEN).result(ErrorResponder.FORBIDDEN)
                } else {
                    handler.handle(ctx)
                }
            })
        }.start(conf.port)
        app.get('/', vue('index'))
        app.get('/members', vue('members'))

        app.error(404, 'html', vue('not-found'))

        final clientApiEndpoint = '/client_api/'

        // TODO this entire cookie store for the avatar seems rather cursed, maybe figure out another way
        final Consumer<Context> dataResolver = (final Context ctx) -> {
            if ((!ctx.cookieStore('gh_user') || !ctx.cookieStore('gh_user_avatar')) && ctx.cookieStore('gh_token')) {
                final myself = new GitHubBuilder().withJwtToken(ctx.cookieStore('gh_token'))
                        .build().getMyself()

                ctx.cookieStore('gh_user', myself.getLogin())
                ctx.cookieStore('gh_user_avatar', myself.getAvatarUrl())
            }
        }
        app.get("$clientApiEndpoint/self_avatar") {
            dataResolver.accept(delegate)
            final String ck = cookieStore('gh_user_avatar')
            if (ck) {
                redirect(ck, HttpCode.TEMPORARY_REDIRECT.status)
            } else {
                redirect('/dist/icons/no_user.png', HttpCode.TEMPORARY_REDIRECT.status)
            }
        }
        app.get("$clientApiEndpoint/self_user") {
            dataResolver.accept(delegate)
            final String ck = cookieStore('gh_user')
            if (ck) {
                redirect("https://github.com/$ck", HttpCode.TEMPORARY_REDIRECT.status)
            } else {
                redirect("/login", HttpCode.TEMPORARY_REDIRECT.status)
            }
        }
        app.get("$clientApiEndpoint/is_logged_in") {
            //noinspection ChangeToOperator
            final String ck = cookieStore('gh_token')
            status(HttpCode.OK).result(ck ? 'true' : 'false')
        }

        // TODO login/logout should redirect to the last page
        app.get('/login_gh') {
            final code = queryParam('code')
            if (!code) {
                status(HttpCode.BAD_REQUEST).result('Do not access this endpoint manually.')
                return
            }
            final request = HttpRequest.newBuilder(URI.create("https://github.com/login/oauth/access_token?client_id=${conf.github.client_id}&client_secret=${conf.github.client_secret}&code=${code}"))
                .POST(HttpRequest.BodyPublishers.ofString(''))
                .header('Accept', 'application/json').build()
            final response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString())
            final var json = JsonObject.fromJson(response.body())
            // TODO maybe encrypt
            final token = json.get('access_token').getAsString()
            cookieStore('gh_token', token)
            redirect('/', HttpCode.TEMPORARY_REDIRECT.status)
        }
        app.get('/logout') {
            cookieStore('gh_token', '')
            cookieStore('gh_user', '')
            cookieStore('gh_user_avatar', '')
            redirect('/', HttpCode.TEMPORARY_REDIRECT.status)
        }

        app.get('/login') {
            redirect("https://github.com/login/oauth/authorize?client_id=${conf.github.client_id}&scope=read:user")
        }
    }

    private static List<String> getRoles(Context context) {
        final String token = context.cookieStore('gh_token')
        if (!conf.apiUrl || !token)
            return List.of()
        try {
            final request = HttpRequest.newBuilder(URI.create("${conf.apiUrl}/user_roles"))
                .GET()
                .header('Authorization', token)
                .build()
            final var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString())
            return List.fromJson(response.body())
        } catch (Exception ignored) {
            return List.of()
        }
    }

    static Handler vue(String name) {
        (Context ctx) -> {
            ctx.cookie('userRoles', getRoles(ctx).join(","))
            new VueComponent(name).handle(ctx)
        }
    }

}
