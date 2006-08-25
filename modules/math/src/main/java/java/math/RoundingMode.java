/*
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.math;

/**
 * @ar.org.fitc.spec_ref
 * @author Intel Middleware Product Division
 * @author Instituto Tecnologico de Cordoba
 */
public enum RoundingMode {

    /** @ar.org.fitc.spec_ref */
    UP(BigDecimal.ROUND_UP),

    /** @ar.org.fitc.spec_ref */
    DOWN(BigDecimal.ROUND_DOWN),

    /** @ar.org.fitc.spec_ref */
    CEILING(BigDecimal.ROUND_CEILING),

    /** @ar.org.fitc.spec_ref */
    FLOOR(BigDecimal.ROUND_FLOOR),

    /** @ar.org.fitc.spec_ref */
    HALF_UP(BigDecimal.ROUND_HALF_UP),

    /** @ar.org.fitc.spec_ref */
    HALF_DOWN(BigDecimal.ROUND_HALF_DOWN),

    /** @ar.org.fitc.spec_ref */
    HALF_EVEN(BigDecimal.ROUND_HALF_EVEN),

    /** @ar.org.fitc.spec_ref */
    UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);

    /** The old constant of <code>BigDecimal</code>. */
    protected final int bigDecimalRM;

    /** It sets the old contant. */
    RoundingMode(int rm) {
        bigDecimalRM = rm;
    }

    /** @ar.org.fitc.spec_ref */
    public static RoundingMode valueOf(int rM) {
        switch (rM) {
            case BigDecimal.ROUND_CEILING:
                return CEILING;
            case BigDecimal.ROUND_DOWN:
                return DOWN;
            case BigDecimal.ROUND_FLOOR:
                return FLOOR;
            case BigDecimal.ROUND_HALF_DOWN:
                return HALF_DOWN;
            case BigDecimal.ROUND_HALF_EVEN:
                return HALF_EVEN;
            case BigDecimal.ROUND_HALF_UP:
                return HALF_UP;
            case BigDecimal.ROUND_UNNECESSARY:
                return UNNECESSARY;
            case BigDecimal.ROUND_UP:
                return UP;
            default:
                throw new IllegalArgumentException("Invalid rounding mode");
        }
    }
}
