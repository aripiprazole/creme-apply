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

import creme.apply.equipment.domain.Equipment
import creme.apply.equipment.domain.EquipmentRepository
import creme.apply.food.domain.Food
import creme.apply.food.domain.FoodRepository
import creme.apply.food.infra.FoodTable
import creme.apply.ingredient.domain.Ingredient
import creme.apply.recipe.infra.RISOTO_DESCRIPTION
import creme.apply.recipe.infra.RecipeTable
import creme.apply.shared.domain.EntityNotFoundException
import creme.apply.withTestDB
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
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
  class GetRecipesByIngredientTests {
    @Test
    fun `test should return a empty set with when a ingredient is not used in any recipes`(): Unit =
      withTestDB {
        val ingredients = setOf<Ingredient>()
        val equipments = setOf<Equipment>()

        val foodRepository = mockk<FoodRepository>()

        val equipmentRepository = mockk<EquipmentRepository> {
          coEvery { getEquipmentsByRecipe(any()) } returns equipments
        }

        val ingredient = Ingredient(
          id = UUID.randomUUID().toString(),
          food = Food(UUID.randomUUID().toString(), "Alho poró", "..."),
          quantity = 1,
          unit = "colher de sopa",
        )

        val ingredientRepository = ExposedIngredientRepository(foodRepository, equipmentRepository)
          .also { mockkObject(it) }
          .apply {
            coEvery { getIngredientsByRecipe(any()) } returns ingredients
            coEvery { findIngredient(ingredient.id) } returns ingredient
          }

        val recipes = runBlocking { ingredientRepository.getRecipesByIngredient(ingredient) }

        assertEquals(0, recipes.size)
      }

    @Test
    fun `test should return a set with all recipes with an ingredient`(): Unit = withTestDB {
      val ingredients = setOf<Ingredient>()
      val equipments = setOf<Equipment>()

      val foodRepository = mockk<FoodRepository>()

      val equipmentRepository = mockk<EquipmentRepository> {
        coEvery { getEquipmentsByRecipe(any()) } returns equipments
      }

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

      val ingredient = Ingredient(
        id = ingredientEntityId.value.toString(),
        food = Food(foodEntityId.value.toString(), "Alho poró", "..."),
        quantity = 1,
        unit = "colher de sopa",
      )

      val ingredientRepository = ExposedIngredientRepository(foodRepository, equipmentRepository)
        .also { mockkObject(it) }
        .apply {
          coEvery { getIngredientsByRecipe(any()) } returns ingredients
        }

      val recipes = runBlocking { ingredientRepository.getRecipesByIngredient(ingredient) }

      assertEquals(1, recipes.size)

      val recipe = recipes.first()

      assertEquals(recipeEntityId.value.toString(), recipe.id)
      assertEquals("Risoto", recipe.name)
      assertEquals("...", recipe.hero)
      assertEquals(RISOTO_DESCRIPTION, recipe.description)
      assertEquals(ingredients, recipe.ingredients)
      assertEquals(equipments, recipe.equipments)
    }
  }

  class FindIngredientTests {
    @Test
    fun `test should return null when database does not contains it`(): Unit = withTestDB {
      val foodRepository = mockk<FoodRepository>()
      val equpipmentRepository = mockk<EquipmentRepository>()
      val ingredientRepository = ExposedIngredientRepository(foodRepository, equpipmentRepository)

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

      val equipmentRepository = mockk<EquipmentRepository>()
      val foodRepository = mockk<FoodRepository> {
        coEvery { findFood(food.id) } returns food
      }
      val ingredientRepository = ExposedIngredientRepository(foodRepository, equipmentRepository)

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

        val equipmentRepository = mockk<EquipmentRepository>()
        val foodRepository = mockk<FoodRepository> {
          coEvery { findFood(foodEntityId.value.toString()) } returns null
        }
        val ingredientRepository = ExposedIngredientRepository(foodRepository, equipmentRepository)

        assertThrows<EntityNotFoundException>("Requested entity with id $foodEntityId was not found.") {
          runBlocking { ingredientRepository.findIngredient(ingredientEntityId.value.toString()) }
        }
      }
  }
}
