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
import creme.apply.ingredient.domain.Ingredient
import creme.apply.ingredient.domain.IngredientRepository
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
  class FindRecipeTests {
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
  }

  class GetRecipesTests {
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
}
