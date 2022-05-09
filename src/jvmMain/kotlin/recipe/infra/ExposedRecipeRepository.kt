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
import creme.apply.ingredient.infra.IngredientTable
import creme.apply.paging.domain.Paginated
import creme.apply.paging.infra.mapToPage
import creme.apply.paging.infra.paginated
import creme.apply.recipe.domain.Recipe
import creme.apply.recipe.domain.RecipeRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ExposedRecipeRepository(
  private val ingredientRepository: IngredientRepository,
  private val equipmentRepository: EquipmentRepository,
) : RecipeRepository {
  override suspend fun getRecipes(page: Int): Paginated<Recipe> = newSuspendedTransaction {
    RecipeTable
      .selectAll()
      .paginated(page)
      .mapToPage { it.toRecipe(ingredientRepository, equipmentRepository) }
  }

  override suspend fun getRecipesByIngredient(ingredient: Ingredient): Set<Recipe> =
    newSuspendedTransaction {
      (RecipeTable innerJoin IngredientTable)
        .select { RecipeTable.id eq IngredientTable.recipeId }
        .map { it.toRecipe(ingredientRepository, equipmentRepository) }
        .toSet()
    }

  override suspend fun findRecipe(id: String): Recipe? = newSuspendedTransaction {
    RecipeTable
      .select { RecipeTable.id eq UUID.fromString(id) }
      .map { it.toRecipe(ingredientRepository, equipmentRepository) }
      .firstOrNull()
  }
}

private suspend fun ResultRow.toRecipe(
  ingredientRepository: IngredientRepository,
  equipmentRepository: EquipmentRepository,
): Recipe {
  val ingredients = mutableSetOf<Ingredient>()
  val equipments = mutableSetOf<Equipment>()

  val recipe = Recipe(
    id = this[RecipeTable.id].value.toString(),
    name = this[RecipeTable.name],
    hero = this[RecipeTable.hero],
    description = this[RecipeTable.description],
    ingredients = ingredients,
    equipments = equipments,
  )

  ingredients.addAll(ingredientRepository.getIngredientsByRecipe(recipe))
  equipments.addAll(equipmentRepository.getEquipmentsByRecipe(recipe))

  return recipe
}
