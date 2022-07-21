package org.moddinginquisition.web.backend.db.types

import groovy.transform.CompileStatic
import org.moddinginquisition.web.backend.db.ForTable
import org.moddinginquisition.web.backend.db.IgnoreInTransaction
import org.moddinginquisition.web.transform.Data

@Data
@CompileStatic
@ForTable('broken_mods')
class BrokenMod {
    @IgnoreInTransaction
    long id

    String mod_id
    String minecraft_version
    String affected_versions
    String reason
    String fixed_version
    String fixed_download_url
}