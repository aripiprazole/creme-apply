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

import creme.apply.food.domain.FoodRepository
import creme.apply.ingredient.domain.Ingredient
import creme.apply.ingredient.domain.IngredientRepository
import creme.apply.recipe.domain.Recipe
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ExposedIngredientRepository(private val foodRepository: FoodRepository) : IngredientRepository {
  override suspend fun findIngredient(id: String): Ingredient? = newSuspendedTransaction {
    IngredientTable
      .select { IngredientTable.id eq UUID.fromString(id) }
      .map { it.toIngredient(foodRepository) }
      .firstOrNull()
  }

  override suspend fun getIngredientsByRecipe(recipe: Recipe): Set<Ingredient> {
    TODO("Not yet implemented")
  }
}

private suspend fun ResultRow.toIngredient(foodRepository: FoodRepository): Ingredient {
  val foodId = this[IngredientTable.foodId].value.toString()

  return Ingredient(
    id = this[IngredientTable.id].value.toString(),
    quantity = this[IngredientTable.quantity],
    unit = this[IngredientTable.unit],
    // NOTE: does not have an entity not found error, because the database foreign key make it
    // impossible to get a null here
    food = foodRepository.findFood(foodId)!!,
  )
}
