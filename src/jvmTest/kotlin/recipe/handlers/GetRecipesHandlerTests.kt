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

package creme.apply.recipe.handlers

import creme.apply.paging.domain.Paginated
import creme.apply.recipe.domain.Recipe
import creme.apply.recipe.domain.RecipeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class GetRecipesHandlerTests {
  @Test
  fun `test should return a recipe paginated`() {
    val paginated = Paginated<Recipe>(values = setOf())

    val recipeRepository = mockk<RecipeRepository> {
      coEvery { getRecipes(any()) } returns paginated
    }
    val handler = GetRecipesHandler(recipeRepository)

    runBlocking { handler.handle(GetRecipesInput(0)) }

    coVerify(exactly = 1) { recipeRepository.getRecipes(0) }
  }
}
