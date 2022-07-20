package org.moddinginquisition.web.backend.db.types


import groovy.transform.CompileStatic

@CompileStatic
class BrokenMod {
    String mod_id
    String affected_versions
    String reason
    String fixed_version
    String fixed_download_url
}
