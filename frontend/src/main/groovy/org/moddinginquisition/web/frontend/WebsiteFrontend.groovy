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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.rendering.vue.JavalinVue
import io.javalin.plugin.rendering.vue.VueComponent

import java.nio.file.Path

@Slf4j
@CompileStatic
class WebsiteFrontend {

    static void main(String[] args) throws Exception {
        JavalinVue.rootDirectory {
            it.classpathPath('/vue', WebsiteFrontend)
        }

        final conf = Configuration.read(Path.of('config.json'))
        final app = Javalin.create() {
            it.enableWebjars()
            it.addStaticFiles('/public', Location.CLASSPATH)
        }.start(conf.port)
        app.get('/', vue('index'))
        app.get('/members', vue('members'))

        app.error(404, 'html', vue('not-found'))

        // TODO should probably be part of the public API
        final clientApiEndpoint = '/client_api/'
        app.get("$clientApiEndpoint/get_org_members") {
            try {
                final is = URI.create("https://api.github.com/orgs/${conf.organization}/members?page=1&per_page=100").toURL().newInputStream()
                result(is)
            } catch (IOException e) {
                result("{'err': 'Unable to load org members!'}")
                log.error('Unable to load requested org members!', e)
            }
        }
    }

    static VueComponent vue(String name) {
        new VueComponent(name)
    }

}
