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

package org.moddinginquisition.web.backend

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.javalin.Javalin
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.kohsuke.github.GitHubBuilder
import org.mariadb.jdbc.MariaDbDataSource
import org.moddinginquisition.web.backend.auth.AuthResolver
import org.moddinginquisition.web.backend.db.Database
import org.moddinginquisition.web.backend.endpoints.BrokenModsEndpoint

import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Slf4j
@CompileStatic
class WebsiteBackend {

    private static final ThreadGroup GROUP = new ThreadGroup('InquisitorWebsite')
    private static final ScheduledExecutorService REFRESHER = Executors.newScheduledThreadPool(2, {
        final thread = new Thread(GROUP, it, 'Refresher')
        thread.daemon = true
        return thread
    })

    public static Database database

    static void main(String[] args) throws Exception {
        final conf = Configuration.read(Path.of('config.json'))
        final app = Javalin.create().start(conf.port)
        app.get('/') {
            result('WIP')
        }

        final url = "jdbc:mariadb://${conf.database.url}/${conf.database.name}"
        final dataSource = new MariaDbDataSource()
        dataSource.setUrl url
        dataSource.setUser conf.database.user
        if (conf.database.password)
            dataSource.setPassword conf.database.password

        final var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db")
                .load()
        flyway.migrate()

        database = new Database(Jdbi.create(dataSource))

        REFRESHER.schedule new AuthResolver(new GitHubBuilder()
            .withOAuthToken(conf.gitHub.apiToken)
            .build(), conf.gitHub), 1, TimeUnit.HOURS

        BrokenModsEndpoint.setup app, database
    }

}
