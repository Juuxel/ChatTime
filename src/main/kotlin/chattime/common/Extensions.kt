/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.common

import java.util.*

fun Random.nextInt(origin: Int, bound: Int): Int
    = ints(1, origin, bound).findFirst().asInt

fun Random.nextInt(range: IntRange): Int
    = nextInt(range.start, range.endInclusive + 1)
