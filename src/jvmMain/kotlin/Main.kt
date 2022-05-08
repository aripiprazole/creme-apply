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

package creme.apply

import creme.apply.ingredient.infra.IngredientRepositoryImpl
import creme.apply.ingredient.web.ingredientRoutes
import creme.apply.recipe.infra.RecipeRepositoryImpl
import creme.apply.recipe.web.recipeRoutes
import creme.apply.shared.domain.EntityNotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing

fun main() {
  embeddedServer(Netty, port = 8000) {
    val recipeRepository = RecipeRepositoryImpl()
    val ingredientRepository = IngredientRepositoryImpl()

    install(StatusPages) {
      exception { call: ApplicationCall, cause: EntityNotFoundException ->
        call.respond(HttpStatusCode.NotFound, cause.toJson())
      }
    }

    routing {
      recipeRoutes(recipeRepository)
      ingredientRoutes(ingredientRepository)
    }
  }.start(wait = true)
}
