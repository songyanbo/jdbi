/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.sqlobject.config;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.core.rewriter.StatementRewriter;

/**
 * Use the specified {@link StatementRewriter} class to rewrite SQL for the annotated SQL object class or method. The
 * given {@link StatementRewriter} class must have a public constructor with any of the following signatures:
 * <ul>
 * <li>RewriterClass() // no arguments</li>
 * <li>RewriterClass(Class)</li>
 * <li>RewriterClass(Class,Method)</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@ConfiguringAnnotation(UseStatementRewriter.Impl.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface UseStatementRewriter
{
    /**
     * Specify the StatementRewriter class to use.
     * @return the StatementRewriter class to use.
     */
    Class<? extends StatementRewriter> value();

    class Impl implements Configurer
    {
        @Override
        public void configureForMethod(ConfigRegistry registry, Annotation annotation, Class<?> sqlObjectType, Method method)
        {
            UseStatementRewriter anno = (UseStatementRewriter) annotation;
            try {
                final StatementRewriter rw = instantiate(anno.value(), sqlObjectType, method);
                registry.get(SqlStatements.class).setStatementRewriter(rw);
            }
            catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void configureForType(ConfigRegistry registry, Annotation annotation, Class<?> sqlObjectType)
        {
            UseStatementRewriter anno = (UseStatementRewriter) annotation;
            try {
                final StatementRewriter rw = instantiate(anno.value(), sqlObjectType, null);
                registry.get(SqlStatements.class).setStatementRewriter(rw);
            }
            catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private StatementRewriter instantiate(Class<? extends StatementRewriter> value,
                                              Class<?> sqlObjectType,
                                              Method m) throws Exception
        {
            try {
                Constructor<? extends StatementRewriter> no_arg = value.getConstructor();
                return no_arg.newInstance();
            }
            catch (NoSuchMethodException e) {
                try {
                    Constructor<? extends StatementRewriter> class_arg = value.getConstructor(Class.class);
                    return class_arg.newInstance(sqlObjectType);
                }
                catch (NoSuchMethodException e1) {
                    if (m != null) {
                        Constructor<? extends StatementRewriter> c_m_arg = value.getConstructor(Class.class,
                                                                                                Method.class);
                        return c_m_arg.newInstance(sqlObjectType, m);
                    }
                    throw new IllegalStateException("Unable to instantiate, no viable constructor " + value.getName());
                }
            }
        }
    }
}
