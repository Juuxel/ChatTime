/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.common

import java.util.*

fun Random.nextInt(origin: Int, bound: Int): Int
    = ints(1, origin, bound).findFirst().asInt

fun Random.nextInt(range: IntRange): Int
    = nextInt(range.start, range.endInclusive + 1)
