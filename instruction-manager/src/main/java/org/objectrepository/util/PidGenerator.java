/*
 * Copyright (c) 2010-2012 Social History Services
 *
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

package org.objectrepository.util;

import java.util.UUID;

/**
 * This class is responsible for the generation of a unique key for each handle. It must take care to never generate
 * two identical keys even under high concurrency. Therefore proven techniques like UUIDs are good candidates.
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class PidGenerator {

    public static UUID getPidUUID() {
        return UUID.randomUUID();
    }

    public static String getPid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Produces a pid with the na prefixed to it: [na]/[UUID]
     *
     * @param na
     * @return
     */
    public static String getPidWithNa(String na) {
        return na + "/" + getPid().toUpperCase();
    }
}
