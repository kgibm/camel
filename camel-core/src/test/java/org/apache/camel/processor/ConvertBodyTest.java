/**
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
package org.apache.camel.processor;

import java.util.Date;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

public class ConvertBodyTest extends ContextTestSupport {

    public void testConvertToInteger() throws Exception {
        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedBodiesReceived(11);

        template.sendBody("direct:start", "11");

        assertMockEndpointsSatisfied();
    }

    public void testConvertFailed() throws Exception {
        MockEndpoint dead = getMockEndpoint("mock:dead");
        dead.expectedMessageCount(1);
        getMockEndpoint("mock:result").expectedMessageCount(0);

        try {
            template.sendBody("direct:invalid", "11");
            fail("Should have thrown an exception");
        } catch (RuntimeCamelException e) {
            assertTrue(e.getCause() instanceof InvalidPayloadException);
        }

        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                errorHandler(deadLetterChannel("mock:dead").disableRedelivery());

                from("direct:start").convertBodyTo(Integer.class).to("mock:result");

                from("direct:invalid").convertBodyTo(Date.class).to("mock:result");
            }
        };
    }

}
