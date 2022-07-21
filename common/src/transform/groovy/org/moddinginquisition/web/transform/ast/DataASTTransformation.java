package org.moddinginquisition.web.transform.ast;

import groovy.transform.CompileStatic;
import org.apache.groovy.ast.tools.AnnotatedNodeUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class DataASTTransformation extends AbstractASTTransformation {
    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        final ClassNode clazz = (ClassNode) nodes[1];
        clazz.getFields().forEach(it -> {
            if (Modifier.isStatic(it.getModifiers())) return;
            final var stms = new ArrayList<Statement>();
            stms.add(new ReturnStatement(new VariableExpression(it)));

            final Statement getStatement = new BlockStatement(stms, new VariableScope());

            final MethodNode getNode = new MethodNode("get" + StringGroovyMethods.capitalize(it.getName()),
                    Modifier.PUBLIC,
                    it.getType(),
                    new Parameter[0],
                    new ClassNode[0],
                    getStatement
            );
            AnnotatedNodeUtils.markAsGenerated(clazz, getNode);
            clazz.addMethod(getNode);
        });
    }
}
