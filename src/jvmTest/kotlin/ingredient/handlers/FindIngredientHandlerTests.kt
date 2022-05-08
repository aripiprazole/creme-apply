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

package ingredient.handlers

import creme.apply.food.domain.Food
import creme.apply.ingredient.domain.Ingredient
import creme.apply.ingredient.domain.IngredientRepository
import creme.apply.ingredient.handlers.FindIngredientHandler
import creme.apply.ingredient.handlers.FindIngredientInput
import creme.apply.shared.domain.EntityNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class FindIngredientHandlerTests {
  @Test
  fun `test should return a ingredient when repository return ingredient`() {
    val ingredient = Ingredient(
      id = UUID.randomUUID().toString(),
      food = Food(UUID.randomUUID().toString(), "Alho poró", "..."),
      quantity = 1,
      unit = "colher de sopa",
    )

    val ingredientRepository = mockk<IngredientRepository> {
      coEvery { findIngredient(ingredient.id) } returns ingredient
    }
    val handler = FindIngredientHandler(ingredientRepository)

    runBlocking { handler.handle(FindIngredientInput(ingredient.id)) }

    coVerify(exactly = 1) { ingredientRepository.findIngredient(ingredient.id) }
  }

  @Test
  fun `test should throw an error when repository return null`() {
    val id = UUID.randomUUID().toString()
    val ingredientRepository = mockk<IngredientRepository> {
      coEvery { findIngredient(any()) } returns null
    }
    val handler = FindIngredientHandler(ingredientRepository)

    assertThrows<EntityNotFoundException>("Requested entity with id $id was not found.") {
      runBlocking { handler.handle(FindIngredientInput(id)) }
    }

    coVerify(exactly = 1) { ingredientRepository.findIngredient(any()) }
  }
}
