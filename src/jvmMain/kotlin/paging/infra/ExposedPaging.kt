/*
 * CREME application
 * Copyright (C) 2022  Gabrielle Guimarães de Oliveira
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package creme.apply.paging.infra

import creme.apply.paging.domain.Paginated
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import kotlin.math.ceil

fun Query.paginated(page: Int, size: Int = Paginated.PAGE_SIZE): Query {
  return limit(size, ((page - 1) * size).toLong())
}

fun Query.countTotalPages(size: Int = Paginated.PAGE_SIZE): Int {
  return ceil((count() / size).toDouble()).toInt() + 1
}

inline fun <R> Query.mapToPage(size: Int = Paginated.PAGE_SIZE, f: (ResultRow) -> R): Paginated<R> {
  val totalPages = countTotalPages()

  return Paginated(map(f).toSet(), size, totalPages)
}
