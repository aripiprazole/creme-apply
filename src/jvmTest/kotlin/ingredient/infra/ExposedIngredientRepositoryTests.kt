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

package creme.apply.ingredient.infra

import creme.apply.food.domain.Food
import creme.apply.food.domain.FoodRepository
import creme.apply.food.infra.FoodTable
import creme.apply.recipe.infra.RISOTO_DESCRIPTION
import creme.apply.recipe.infra.RecipeTable
import creme.apply.shared.domain.EntityNotFoundException
import creme.apply.withTestDB
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExposedIngredientRepositoryTests {
  class FindIngredientTests {
    @Test
    fun `test should return null when database does not contains it`(): Unit = withTestDB {
      val foodRepository = mockk<FoodRepository>()
      val ingredientRepository = ExposedIngredientRepository(foodRepository)

      val ingredient = runBlocking {
        ingredientRepository.findIngredient(UUID.randomUUID().toString())
      }

      assertNull(ingredient)
    }

    @Test
    fun `test should return an ingredient when database contains it`(): Unit = withTestDB {
      val recipeEntityId = transaction {
        RecipeTable.insertAndGetId {
          it[name] = "Risoto"
          it[hero] = "..."
          it[description] = RISOTO_DESCRIPTION
        }
      }

      val foodEntityId = transaction {
        FoodTable.insertAndGetId {
          it[name] = "Alho poró"
          it[hero] = "..."
        }
      }

      val ingredientEntityId = transaction {
        IngredientTable.insertAndGetId {
          it[quantity] = 1
          it[unit] = "colher de sopa"
          it[foodId] = foodEntityId
          it[recipeId] = recipeEntityId
        }
      }

      val food = Food(foodEntityId.value.toString(), "Alho poró", "... ")

      val foodRepository = mockk<FoodRepository> {
        coEvery { findFood(food.id) } returns food
      }
      val ingredientRepository = ExposedIngredientRepository(foodRepository)

      val ingredient = runBlocking {
        ingredientRepository.findIngredient(ingredientEntityId.value.toString())
      }

      assertNotNull(ingredient)

      assertEquals(1, ingredient.quantity)
      assertEquals("colher de sopa", ingredient.unit)
      assertEquals(ingredientEntityId.value.toString(), ingredient.id)
      assertEquals(food, ingredient.food)
    }

    @Test
    fun `test should throw an error when food repository does contains the food`(): Unit =
      withTestDB {
        val recipeEntityId = transaction {
          RecipeTable.insertAndGetId {
            it[name] = "Risoto"
            it[hero] = "..."
            it[description] = RISOTO_DESCRIPTION
          }
        }

        val foodEntityId = transaction {
          FoodTable.insertAndGetId {
            it[name] = "Alho poró"
            it[hero] = "..."
          }
        }

        val ingredientEntityId = transaction {
          IngredientTable.insertAndGetId {
            it[quantity] = 1
            it[unit] = "colher de sopa"
            it[foodId] = foodEntityId
            it[recipeId] = recipeEntityId
          }
        }

        val foodRepository = mockk<FoodRepository> {
          coEvery { findFood(foodEntityId.value.toString()) } returns null
        }
        val ingredientRepository = ExposedIngredientRepository(foodRepository)

        assertThrows<EntityNotFoundException>("Requested entity with id $foodEntityId was not found.") {
          runBlocking { ingredientRepository.findIngredient(ingredientEntityId.value.toString()) }
        }
      }
  }
}
