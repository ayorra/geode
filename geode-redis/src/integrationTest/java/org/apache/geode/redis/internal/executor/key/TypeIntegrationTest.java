/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.redis.internal.executor.key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

import org.apache.geode.redis.GeodeRedisServerRule;
import org.apache.geode.test.awaitility.GeodeAwaitility;

public class TypeIntegrationTest {

  public static Jedis jedis;
  public static int REDIS_CLIENT_TIMEOUT =
      Math.toIntExact(GeodeAwaitility.getTimeout().toMillis());

  @ClassRule
  public static GeodeRedisServerRule server = new GeodeRedisServerRule();

  @BeforeClass
  public static void setUp() {
    jedis = new Jedis("localhost", server.getPort(), REDIS_CLIENT_TIMEOUT);
  }

  @After
  public void flushAll() {
    jedis.flushAll();
  }

  @AfterClass
  public static void tearDown() {
    jedis.close();
  }

  @Test
  public void shouldReturnNone_givenKeyDoesNotExist() {
    assertThat(jedis.type("doesNotExist")).isEqualTo("none");
  }

  @Test
  public void shouldThrowError_givenTooFewArgs() {
    assertThatThrownBy(() -> jedis.sendCommand(Protocol.Command.TYPE))
        .hasMessageContaining("wrong number of arguments");
  }

  @Test
  public void shouldThrowError_givenTooManyArgs() {
    assertThatThrownBy(() -> jedis.sendCommand(Protocol.Command.TYPE, "orange", "blue"))
        .hasMessageContaining("wrong number of arguments");
  }

  @Test
  public void shouldReturnCorrectType_givenStringKey() {
    jedis.set("orange", "crush");

    assertThat(jedis.type("orange")).isEqualTo("string");
  }

  @Test
  public void shouldReturnCorrectType_givenSetKey() {
    jedis.sadd("orange", "crush");

    assertThat(jedis.type("orange")).isEqualTo("set");
  }

  @Test
  public void shouldReturnCorrectType_givenHashKey() {
    jedis.hset("rem", "songs", "orange crush");

    assertThat(jedis.type("rem")).isEqualTo("hash");
  }
}
