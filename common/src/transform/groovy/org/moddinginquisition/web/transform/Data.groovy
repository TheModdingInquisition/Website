package org.moddinginquisition.web.transform

import groovy.transform.CompileStatic
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Annotation used to mark 'data' classes. Will generate getters for all instance fields, at transformation phase.
 */
@Documented
@CompileStatic
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass('org.moddinginquisition.web.transform.ast.DataASTTransformation')
@interface Data {
}
