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

package creme.apply.ingredient.web

import creme.apply.ingredient.domain.Ingredient
import creme.apply.ingredient.domain.IngredientRepository
import creme.apply.ingredient.handlers.FindIngredientHandler
import creme.apply.ingredient.handlers.FindIngredientInput
import creme.apply.ingredient.handlers.GetRecipesByIngredientHandler
import creme.apply.ingredient.handlers.GetRecipesByIngredientInput
import creme.apply.recipe.domain.Recipe
import creme.apply.recipe.domain.RecipeRepository
import creme.apply.shared.domain.Handler
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.util.getValue

fun Route.ingredientRoutes(
  ingredientRepository: IngredientRepository,
  recipeRepository: RecipeRepository,
) {
  route("ingredients") {
    route("{ingredientId}") {
      findIngredientRoute(FindIngredientHandler(ingredientRepository))

      route("recipes") {
        getRecipesByIngredientRoute(
          GetRecipesByIngredientHandler(ingredientRepository, recipeRepository,)
        )
      }
    }
  }
}

fun Route.findIngredientRoute(handler: Handler<FindIngredientInput, Ingredient>) {
  get {
    val ingredientId: String by call.parameters

    call.respond(handler.handle(FindIngredientInput(ingredientId)))
  }
}

fun Route.getRecipesByIngredientRoute(handler: Handler<GetRecipesByIngredientInput, Set<Recipe>>) {
  get {
    val ingredientId: String by call.parameters

    call.respond(handler.handle(GetRecipesByIngredientInput(ingredientId)))
  }
}
