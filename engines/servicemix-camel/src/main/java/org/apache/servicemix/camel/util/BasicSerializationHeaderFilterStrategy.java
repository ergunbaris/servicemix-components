/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.camel.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.spi.HeaderFilterStrategy;

/**
 * {@link org.apache.camel.spi.HeaderFilterStrategy} that filters out non-serializable values:
 * <ul>
 * <li>classes that don't implement {@link java.io.Serializable}</li>
 * <li>{@link java.util.Collection}s</li>
 * <li>{@link java.util.Map}s</li>
 * </ul>
 */
public class BasicSerializationHeaderFilterStrategy implements HeaderFilterStrategy {

    public boolean applyFilterToCamelHeaders(String s, Object o, Exchange exchange) {
        return doApplyFilter(o);
    }

    public boolean applyFilterToExternalHeaders(String s, Object o, Exchange exchange) {
        return doApplyFilter(o);
    }

    private boolean doApplyFilter(Object o) {
        return !(o instanceof Serializable) || (o instanceof Map) || (o instanceof Collection);
    }
}
