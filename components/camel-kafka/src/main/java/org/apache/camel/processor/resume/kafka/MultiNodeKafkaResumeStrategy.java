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

package org.apache.camel.processor.resume.kafka;

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.camel.resume.ResumeAdapter;
import org.apache.camel.resume.cache.ResumeCache;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resume strategy that publishes offsets to a Kafka topic. This resume strategy is suitable for multi node
 * integrations. This is suitable, for instance, when using clusters with the master component.
 *
 * @param <K> the type of key
 * @param <V> the type of the value
 */
public class MultiNodeKafkaResumeStrategy<K, V> extends SingleNodeKafkaResumeStrategy<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(MultiNodeKafkaResumeStrategy.class);
    private final ExecutorService executorService;

    /**
     * Create a new instance of this class
     * 
     * @param bootstrapServers the address of the Kafka broker
     * @param topic            the topic where to publish the offsets
     * @param resumeCache      a cache instance where to store the offsets locally for faster access
     * @param resumeAdapter    the component-specific resume adapter
     */
    public MultiNodeKafkaResumeStrategy(String bootstrapServers, String topic, ResumeCache<K, V> resumeCache,
                                        ResumeAdapter resumeAdapter) {
        // just in case users don't want to provide their own worker thread pool
        this(bootstrapServers, topic, resumeCache, resumeAdapter, Executors.newSingleThreadExecutor());
    }

    /**
     * Builds an instance of this class
     *
     * @param bootstrapServers
     * @param topic            the topic where to publish the offsets
     * @param resumeCache      a cache instance where to store the offsets locally for faster access
     * @param resumeAdapter    the component-specific resume adapter
     * @param executorService  an executor service that will run a separate thread for periodically refreshing the
     *                         offsets
     */

    public MultiNodeKafkaResumeStrategy(String bootstrapServers, String topic, ResumeCache<K, V> resumeCache,
                                        ResumeAdapter resumeAdapter, ExecutorService executorService) {
        super(bootstrapServers, topic, resumeCache, resumeAdapter);

        // We need to keep refreshing the cache
        this.executorService = executorService;
        executorService.submit(() -> refresh());
    }

    /**
     * Builds an instance of this class
     *
     * @param topic          the topic where to publish the offsets
     * @param resumeCache    a cache instance where to store the offsets locally for faster access
     * @param resumeAdapter  the component-specific resume adapter
     * @param producerConfig the set of properties to be used by the Kafka producer within this class
     * @param consumerConfig the set of properties to be used by the Kafka consumer within this class
     */
    public MultiNodeKafkaResumeStrategy(String topic, ResumeCache<K, V> resumeCache, ResumeAdapter resumeAdapter,
                                        Properties producerConfig, Properties consumerConfig) {
        this(topic, resumeCache, resumeAdapter, producerConfig, consumerConfig, Executors.newSingleThreadExecutor());
    }

    /**
     * Builds an instance of this class
     *
     * @param topic           the topic where to publish the offsets
     * @param resumeCache     a cache instance where to store the offsets locally for faster access
     * @param resumeAdapter   the component-specific resume adapter
     * @param producerConfig  the set of properties to be used by the Kafka producer within this class
     * @param consumerConfig  the set of properties to be used by the Kafka consumer within this class
     * @param executorService an executor service that will run a separate thread for periodically refreshing the
     *                        offsets
     */

    public MultiNodeKafkaResumeStrategy(String topic, ResumeCache<K, V> resumeCache, ResumeAdapter resumeAdapter,
                                        Properties producerConfig, Properties consumerConfig, ExecutorService executorService) {
        super(topic, resumeCache, resumeAdapter, producerConfig, consumerConfig);

        this.executorService = executorService;
        executorService.submit(() -> refresh());
    }

    /**
     * Launch a thread to refresh the offsets periodically
     */
    protected void refresh() {
        LOG.trace("Creating a offset cache refresher");
        try {
            Properties prop = (Properties) getConsumerConfig().clone();
            prop.setProperty(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());

            Consumer<K, V> consumer = new KafkaConsumer<>(prop);

            consumer.subscribe(Collections.singletonList(getTopic()));

            while (true) {
                var records = consumer.poll(getPollDuration());
                if (records.isEmpty()) {
                    continue;
                }

                for (var record : records) {
                    V value = record.value();

                    LOG.trace("Read from Kafka: {}", value);
                    getResumeCache().add(record.key(), record.value());
                }
            }
        } catch (Exception e) {
            LOG.error("Error while refreshing the local cache: {}", e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        try {
            executorService.shutdown();
        } finally {
            super.stop();
        }
    }
}
