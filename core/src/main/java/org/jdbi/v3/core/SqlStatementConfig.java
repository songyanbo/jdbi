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
package org.jdbi.v3.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jdbi.v3.core.rewriter.ColonPrefixStatementRewriter;
import org.jdbi.v3.core.rewriter.StatementRewriter;

public final class SqlStatementConfig implements JdbiConfig<SqlStatementConfig> {

    private final Map<String, Object> attributes;
    private volatile StatementRewriter statementRewriter;
    private volatile TimingCollector timingCollector;

    public SqlStatementConfig() {
        attributes = new ConcurrentHashMap<>();
        statementRewriter = new ColonPrefixStatementRewriter();
        timingCollector = TimingCollector.NOP_TIMING_COLLECTOR;
    }

    private SqlStatementConfig(SqlStatementConfig that) {
        this.attributes = new ConcurrentHashMap<>(that.attributes);
        this.statementRewriter = that.statementRewriter;
        this.timingCollector = that.timingCollector;
    }

    /**
     * The attributes which will be applied to all {@link SqlStatement SQL statements} created by JDBI.
     * @return the defined attributes.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Define an attribute for {@link StatementContext} for statements executed by JDBI.
     *
     * @param key   The key for the attribute
     * @param value the value for the attribute
     * @return this
     */
    public SqlStatementConfig putAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    /**
     * Obtain the value of an attribute
     *
     * @param key The name of the attribute
     *
     * @return the value of the attribute
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Defines attributes for each key/value pair in the Map.
     *
     * @param values map of attributes to define.
     * @return this
     */
    public SqlStatementConfig putAttributes(final Map<String, ?> values)
    {
        if (values != null) {
            attributes.putAll(values);
        }
        return this;
    }

    public StatementRewriter getStatementRewriter() {
        return statementRewriter;
    }

    /**
     * Sets the {@link StatementRewriter} used to transform SQL for all {@link SqlStatement SQL satements} executed by
     * JDBI. The default statement rewriter handles named parameter interpolation.
     *
     * @param rewriter the new statement rewriter.
     * @return this
     */
    public SqlStatementConfig setStatementRewriter(StatementRewriter rewriter) {
        this.statementRewriter = rewriter;
        return this;
    }

    public TimingCollector getTimingCollector() {
        return timingCollector;
    }

    /**
     * Sets the {@link TimingCollector} used to collect timing about the {@link SqlStatement SQL statements} executed
     * by JDBI. The default collector does nothing.
     *
     * @param timingCollector the new timing collector
     * @return this
     */
    public SqlStatementConfig setTimingCollector(TimingCollector timingCollector) {
        this.timingCollector = timingCollector == null ? TimingCollector.NOP_TIMING_COLLECTOR : timingCollector;
        return this;
    }

    @Override
    public SqlStatementConfig createChild() {
        return new SqlStatementConfig(this);
    }
}
