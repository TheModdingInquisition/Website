//file:noinspection DuplicatedCode
package org.moddinginquisition.web.backend.endpoints

import com.google.gson.JsonObject
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

            final Boolean isFixed = queryParam('is_fixed') ? Boolean.parseBoolean(queryParam('is_fixed')) : null

            result(brokenModsDao.select()
                    .and(BrokenMod.&getMinecraft_version, queryParam('minecraft_version'))
                    .and(BrokenMod.&getMod_id, queryParam('mod_id'))
                    .and(BrokenMod.&getIs_fixed, isFixed)
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

        final requiredProperties = [
                'mod_id', 'minecraft_version', 'affected_versions', 'reason', 'is_fixed'
        ]

        app.post('/broken_mods', Role.JANITOR) {
            try {
                final mod = BrokenMod.fromJson(body())
                for (property in requiredProperties) {
                    if (mod.getProperty(property) === null) {
                        status(HttpCode.BAD_REQUEST)
                        result(ErrorResponder.MISSING_FIELD.withMessage("Missing required field: ${property}"))
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

        app.patch('/broken_mods', Role.JANITOR) {
            final id = queryParam('id')?.isLong() ? queryParam('id') as Long : -1
            final found = brokenModsDao.select()
                    .and(BrokenMod.&getId, id)
                    .execute(ResultIterable::findFirst)
            final json = JsonObject.fromJson(body())
            if (found) {
                try (final handle = db.jdbi.open()) {
                    handle.commit()
                    final updated = brokenModsDao.update()
                            .where(BrokenMod.&getId, id)
                            .set(BrokenMod.&getIs_fixed, json.getBoolean('is_fixed'))
                            .set(BrokenMod.&getFixed_version, json.get('fixed_version')?.getAsString())
                            .set(BrokenMod.&getFixed_download_url, json.get('fixed_download_url')?.getAsString())
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
    }

}
