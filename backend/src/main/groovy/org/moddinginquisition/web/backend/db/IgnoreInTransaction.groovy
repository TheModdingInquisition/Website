package org.moddinginquisition.web.backend.db

import groovy.transform.CompileStatic

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@CompileStatic
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface IgnoreInTransaction {

}