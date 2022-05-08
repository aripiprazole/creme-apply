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

import creme.apply.food.infra.FoodTable
import creme.apply.recipe.infra.RecipeTable
import org.jetbrains.exposed.dao.id.UUIDTable

object IngredientTable : UUIDTable("ingredient") {
  val quantity = integer("quantity")
  val unit = varchar("unit", 25)
  val foodId = reference("food_id", FoodTable)
  val recipeId = reference("recipe_id", RecipeTable)
}
