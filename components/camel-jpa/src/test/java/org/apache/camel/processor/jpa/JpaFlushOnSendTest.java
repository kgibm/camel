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
package org.apache.camel.processor.jpa;

import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.examples.SendEmail;
import org.junit.jupiter.api.Test;

import static org.apache.camel.test.junit5.TestSupport.assertIsInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpaFlushOnSendTest extends AbstractJpaTest {
    protected static final String SELECT_ALL_STRING = "select x from " + SendEmail.class.getName() + " x";

    @Test
    public void testRouteJpa() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        template.sendBody("direct:start", new SendEmail("someone@somewhere.org"));

        assertMockEndpointsSatisfied();
        assertEntityInDB();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start").to("jpa://" + SendEmail.class.getName() + "?flushOnSend=true").to("mock:result");
            }
        };
    }

    private void assertEntityInDB() {
        List<?> list = entityManager.createQuery(SELECT_ALL_STRING).getResultList();
        assertEquals(1, list.size());

        assertIsInstanceOf(SendEmail.class, list.get(0));
    }

    @Override
    protected String routeXml() {
        return "org/apache/camel/processor/jpa/springJpaRouteTest.xml";
    }

    @Override
    protected String selectAllString() {
        return SELECT_ALL_STRING;
    }
}
