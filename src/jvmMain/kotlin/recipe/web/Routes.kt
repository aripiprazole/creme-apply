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

package creme.apply.recipe.web

import creme.apply.paging.Paginated
import creme.apply.recipe.domain.Recipe
import creme.apply.recipe.domain.RecipeRepository
import creme.apply.recipe.handlers.GetRecipeInput
import creme.apply.recipe.handlers.GetRecipesHandler
import creme.apply.shared.domain.Handler
import creme.apply.shared.domain.HandlerDsl
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.util.getValue

fun Route.recipeRoutes(recipeRepository: RecipeRepository) {
  route("recipes") {
    getRecipes(GetRecipesHandler(recipeRepository))
  }
}

@HandlerDsl
fun Route.getRecipes(handler: Handler<GetRecipeInput, Paginated<Recipe>>) {
  get {
    val page: Int by call.request.queryParameters

    call.respond(handler.handle(GetRecipeInput(page)))
  }
}
