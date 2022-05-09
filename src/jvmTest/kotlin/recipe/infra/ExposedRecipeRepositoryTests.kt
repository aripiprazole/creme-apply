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

package creme.apply.recipe.infra

import creme.apply.equipment.domain.Equipment
import creme.apply.equipment.domain.EquipmentRepository
import creme.apply.food.domain.Food
import creme.apply.food.infra.FoodTable
import creme.apply.ingredient.domain.Ingredient
import creme.apply.ingredient.domain.IngredientRepository
import creme.apply.ingredient.infra.IngredientTable
import creme.apply.paging.domain.Paginated
import creme.apply.withTestDB
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

const val RISOTO_DESCRIPTION = "O risotto é um prato típico italiano em que se " +
  "fritam levemente as cebolas e o arbório, ou o arroz" +
  " em manteiga,  e se vai gradualmente deitando fundo" +
  " de carne ou legumes e outros ingredientes, até o" +
  " arroz estar cozido e não poder absorver mais líquido."

class ExposedRecipeRepositoryTests {
  @Test
  fun `test should return a empty set with when a ingredient is not used in any recipes`(): Unit =
    withTestDB {
      val ingredients = setOf<Ingredient>()
      val equipments = setOf<Equipment>()

      val ingredientRepository = mockk<IngredientRepository> {
        coEvery { getIngredientsByRecipe(any()) } returns ingredients
      }
      val equipmentRepository = mockk<EquipmentRepository> {
        coEvery { getEquipmentsByRecipe(any()) } returns equipments
      }

      val ingredient = Ingredient(
        id = UUID.randomUUID().toString(),
        food = Food(UUID.randomUUID().toString(), "Alho poró", "..."),
        quantity = 1,
        unit = "colher de sopa",
      )

      val recipeRepository = ExposedRecipeRepository(ingredientRepository, equipmentRepository)

      val recipes = runBlocking { recipeRepository.getRecipesByIngredient(ingredient) }

      assertEquals(0, recipes.size)
    }

  @Test
  fun `test should return a set with all recipes with an ingredient`(): Unit = withTestDB {
    val ingredients = setOf<Ingredient>()
    val equipments = setOf<Equipment>()

    val ingredientRepository = mockk<IngredientRepository> {
      coEvery { getIngredientsByRecipe(any()) } returns ingredients
    }
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

    val recipeRepository = ExposedRecipeRepository(ingredientRepository, equipmentRepository)

    val recipes = runBlocking { recipeRepository.getRecipesByIngredient(ingredient) }

    assertEquals(1, recipes.size)

    val recipe = recipes.first()

    assertEquals(recipeEntityId.value.toString(), recipe.id)
    assertEquals("Risoto", recipe.name)
    assertEquals("...", recipe.hero)
    assertEquals(RISOTO_DESCRIPTION, recipe.description)
    assertEquals(ingredients, recipe.ingredients)
    assertEquals(equipments, recipe.equipments)
  }

  @Test
  fun `test should return null when database does not contains it`(): Unit = withTestDB {
    val ingredientRepository = mockk<IngredientRepository>()
    val equipmentRepository = mockk<EquipmentRepository>()

    val recipeRepository = ExposedRecipeRepository(ingredientRepository, equipmentRepository)

    val recipe = runBlocking { recipeRepository.findRecipe(UUID.randomUUID().toString()) }

    assertNull(recipe)
  }

  @Test
  fun `test should return a recipe when database contains it`(): Unit = withTestDB {
    val ingredients = setOf<Ingredient>()
    val equipments = setOf<Equipment>()

    val ingredientRepository = mockk<IngredientRepository> {
      coEvery { getIngredientsByRecipe(any()) } returns ingredients
    }
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

    val recipeRepository = ExposedRecipeRepository(ingredientRepository, equipmentRepository)

    val recipe = runBlocking { recipeRepository.findRecipe(recipeEntityId.value.toString()) }

    assertNotNull(recipe)
    assertEquals(recipeEntityId.value.toString(), recipe.id)
    assertEquals("Risoto", recipe.name)
    assertEquals("...", recipe.hero)
    assertEquals(RISOTO_DESCRIPTION, recipe.description)
    assertEquals(ingredients, recipe.ingredients)
    assertEquals(equipments, recipe.equipments)
  }

  @Test
  fun `test should return a recipe paginated`(): Unit = withTestDB {
    val ingredients = setOf<Ingredient>()
    val equipments = setOf<Equipment>()

    val ingredientRepository = mockk<IngredientRepository> {
      coEvery { getIngredientsByRecipe(any()) } returns ingredients
    }
    val equipmentRepository = mockk<EquipmentRepository> {
      coEvery { getEquipmentsByRecipe(any()) } returns equipments
    }

    val recipeRepository = ExposedRecipeRepository(ingredientRepository, equipmentRepository)

    val recipeEntityId = transaction {
      RecipeTable.insertAndGetId {
        it[name] = "Risoto"
        it[hero] = "..."
        it[description] = RISOTO_DESCRIPTION
      }
    }

    val paginated = runBlocking { recipeRepository.getRecipes(0) }

    assertEquals(Paginated.PAGE_SIZE, paginated.size)
    assertEquals(1, paginated.values.size)
    assertEquals(1, paginated.totalPages)

    val recipe = paginated.values.first()

    assertEquals(recipeEntityId.value.toString(), recipe.id)
    assertEquals("Risoto", recipe.name)
    assertEquals("...", recipe.hero)
    assertEquals(RISOTO_DESCRIPTION, recipe.description)
    assertEquals(ingredients, recipe.ingredients)
    assertEquals(equipments, recipe.equipments)
  }
}
