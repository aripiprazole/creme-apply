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

package creme.apply.food.infra

import creme.apply.food.domain.Food
import creme.apply.food.domain.FoodRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class FoodRepositoryImpl : FoodRepository {
  override suspend fun findFood(id: String): Food? = newSuspendedTransaction {
    FoodTable
      .select { FoodTable.id eq UUID.fromString(id) }
      .map { it.toFood() }
      .firstOrNull()
  }

  private fun ResultRow.toFood(): Food {
    return Food(this[FoodTable.id].value.toString(), this[FoodTable.name], this[FoodTable.hero])
  }
}
