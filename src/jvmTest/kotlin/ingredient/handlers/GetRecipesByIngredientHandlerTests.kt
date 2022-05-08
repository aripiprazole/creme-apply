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

package creme.apply.ingredient.handlers

import creme.apply.food.domain.Food
import creme.apply.ingredient.domain.Ingredient
import creme.apply.ingredient.domain.IngredientRepository
import creme.apply.recipe.domain.Recipe
import creme.apply.shared.domain.EntityNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class GetRecipesByIngredientHandlerTests {
  @Test
  fun `test should return a recipe set when repository return a ingredient`() {
    val ingredient = Ingredient(
      id = UUID.randomUUID().toString(),
      food = Food(UUID.randomUUID().toString(), "Alho poró", "..."),
      quantity = 1,
      unit = "colher de sopa",
    )

    val recipeSet = setOf<Recipe>()

    val ingredientRepository = mockk<IngredientRepository> {
      coEvery { findIngredient(ingredient.id) } returns ingredient
      coEvery { getRecipesByIngredient(ingredient) } returns recipeSet
    }
    val handler = GetRecipesByIngredientHandler(ingredientRepository)

    runBlocking { handler.handle(GetRecipesByIngredientInput(ingredient.id)) }

    coVerify(exactly = 1) { ingredientRepository.findIngredient(ingredient.id) }
    coVerify(exactly = 1) { ingredientRepository.getRecipesByIngredient(ingredient) }
  }

  @Test
  fun `test should throw an error when repository return a null ingredient`() {
    val ingredient = Ingredient(
      id = UUID.randomUUID().toString(),
      food = Food(UUID.randomUUID().toString(), "Alho poró", "..."),
      quantity = 1,
      unit = "colher de sopa",
    )

    val ingredientRepository = mockk<IngredientRepository> {
      coEvery { findIngredient(any()) } returns null
    }
    val handler = GetRecipesByIngredientHandler(ingredientRepository)

    assertThrows<EntityNotFoundException>("Requested entity with id ${ingredient.id} was not found.") {
      runBlocking { handler.handle(GetRecipesByIngredientInput(ingredient.id)) }
    }

    coVerify(exactly = 1) { ingredientRepository.findIngredient(any()) }
    coVerify(exactly = 0) { ingredientRepository.getRecipesByIngredient(ingredient) }
  }
}
