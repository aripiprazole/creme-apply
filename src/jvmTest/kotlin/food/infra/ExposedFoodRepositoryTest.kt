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

import creme.apply.withTestDB
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExposedFoodRepositoryTest {
  class FindFoodTests {
    @Test
    fun `test should return null when database does not contains it`(): Unit = withTestDB {
      val foodRepository = ExposedFoodRepository()

      val food = runBlocking { foodRepository.findFood(UUID.randomUUID().toString()) }

      assertNull(food)
    }

    @Test
    fun `test should return a food when database contains it`(): Unit = withTestDB {
      val foodRepository = ExposedFoodRepository()

      val foodEntityId = transaction {
        FoodTable.insertAndGetId {
          it[name] = "Alho poró"
          it[hero] = "..."
        }
      }

      val food = runBlocking { foodRepository.findFood(foodEntityId.value.toString()) }

      assertNotNull(food)
      assertEquals(foodEntityId.value.toString(), food.id)
      assertEquals("Alho poró", food.name)
      assertEquals("...", food.hero)
    }
  }
}
