package org.moddinginquisition.web.backend.endpoints

import com.google.gson.JsonSyntaxException
import groovy.transform.CompileStatic
import io.javalin.Javalin
import io.javalin.http.HttpCode
import io.javalin.http.util.NaiveRateLimit
import org.jdbi.v3.core.result.ResultIterable
import org.moddinginquisition.web.backend.auth.AuthResolver
import org.moddinginquisition.web.backend.db.Database
import org.moddinginquisition.web.backend.db.types.BrokenMod
import org.moddinginquisition.web.common.util.ErrorResponder
import org.moddinginquisition.web.common.util.Role
import org.moddinginquisition.web.common.util.McRateLimit

import java.util.concurrent.TimeUnit

@CompileStatic
class BrokenModsEndpoint {

    static void setup(Javalin app, Database db) {
        final brokenModsDao = db.get(BrokenMod)

        app.get('/broken_mods') {
            if (!AuthResolver.isJanitor(delegate)) {
                NaiveRateLimit.requestPerTimeUnit(delegate, 5, TimeUnit.MINUTES)
                McRateLimit.requestPerTimeUnit(delegate, 5, TimeUnit.MINUTES)
            }

            result(brokenModsDao.select()
                    .and(BrokenMod.&getMinecraft_version, queryParam('minecraft_version'))
                    .and(BrokenMod.&getMod_id, queryParam('mod_id'))
                    .execute(ResultIterable::list)
                    .toResponseJson())
        }

        app.delete('/broken_mods', Role.JANITOR) {
            final id = queryParam('id')?.isLong() ? queryParam('id') as Long : -1
            final found = brokenModsDao.select()
                    .and(BrokenMod.&getId, id)
                    .execute(ResultIterable::findFirst)
            if (found) {
                try (final handle = db.jdbi.open()) {
                    handle.commit()
                    final updated = brokenModsDao.delete()
                            .and(BrokenMod.&getId, id)
                            .execute()
                    if (updated == 1) {
                        status(HttpCode.OK)
                        result(found.get().toResponseJson())
                    } else {
                        handle.rollback()
                        status(HttpCode.INTERNAL_SERVER_ERROR)
                        result(ErrorResponder.UNEXPECTED_MODIFICATION.withMessage("Deleted $updated rows, more than 1 expected"))
                    }
                }
            } else {
                status(HttpCode.BAD_REQUEST)
                result(ErrorResponder.INVALID_ID.withMessage("Invalid ID (${queryParam('id')}) provided"))
            }
        }

        app.post('/broken_mods', Role.JANITOR) {
            try {
                final mod = BrokenMod.fromJson(body())
                for (property in mod.getMetaClass().getProperties()) {
                    if (property.name == 'class' || property.name == 'id') continue
                    if (property.getProperty(mod) === null) {
                        status(HttpCode.BAD_REQUEST)
                        result(ErrorResponder.MISSING_FIELD.withMessage("Missing required field: ${property.name}"))
                        return
                    }
                }
                final id = brokenModsDao.insertReturning(mod, BrokenMod.&getId)
                status(HttpCode.OK)
                result("""{"id": $id}""")
            } catch (JsonSyntaxException e) {
                status(HttpCode.BAD_REQUEST)
                result(ErrorResponder.INVALID_SYNTAX.withMessage(e.getMessage()))
            }
        }
    }

}
