/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.distsql.handler.aware;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.type.DistSQLConnectionContext;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Optional;

/**
 * DistSQL executor aware setter.
 */
@RequiredArgsConstructor
public final class DistSQLExecutorAwareSetter {
    
    private final ContextManager contextManager;
    
    private final ShardingSphereDatabase database;
    
    private final DistSQLConnectionContext connectionContext;
    
    private final Object executor;
    
    /**
     * Set aware context.
     */
    @SuppressWarnings("rawtypes")
    public void set() {
        if (executor instanceof DistSQLExecutorDatabaseAware) {
            ShardingSpherePreconditions.checkNotNull(database, NoDatabaseSelectedException::new);
            ((DistSQLExecutorDatabaseAware) executor).setDatabase(database);
        }
        if (executor instanceof DistSQLExecutorRuleAware) {
            setRule((DistSQLExecutorRuleAware) executor);
        }
        if (executor instanceof DistSQLExecutorConnectionContextAware) {
            ((DistSQLExecutorConnectionContextAware) executor).setConnectionContext(connectionContext);
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setRule(final DistSQLExecutorRuleAware executor) throws UnsupportedSQLOperationException {
        Optional<ShardingSphereRule> rule = findRule(executor.getRuleClass());
        ShardingSpherePreconditions.checkState(rule.isPresent(), () -> new UnsupportedSQLOperationException(String.format("The current database has no `%s` rules", executor.getRuleClass())));
        executor.setRule(rule.get());
    }
    
    private Optional<ShardingSphereRule> findRule(final Class<ShardingSphereRule> ruleClass) {
        Optional<ShardingSphereRule> globalRule = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(ruleClass);
        return globalRule.isPresent() ? globalRule : database.getRuleMetaData().findSingleRule(ruleClass);
    }
}
