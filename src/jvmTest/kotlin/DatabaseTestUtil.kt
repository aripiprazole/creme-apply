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

import creme.apply.equipment.infra.EquipmentTable
import creme.apply.food.infra.FoodTable
import creme.apply.ingredient.infra.IngredientTable
import creme.apply.recipe.infra.RecipeTable
import creme.apply.tool.infra.ToolTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun withTestDB(block: () -> Unit) {
  val databasesFolder = File("databases")
  val databaseFile = databasesFolder.resolve("database")

  databasesFolder.listFiles()
    .orEmpty()
    .filter { it.name.endsWith(".mv.db") }
    .forEach { it.delete() }

  Database.connect(
    url = "jdbc:h2:file:${databaseFile.absolutePath};MODE=postgresql;DATABASE_TO_UPPER=false",
    driver = "org.h2.Driver",
    user = "root",
    password = "",
  )
  transaction {
    SchemaUtils.create(ToolTable, FoodTable, EquipmentTable, RecipeTable, IngredientTable)
  }

  block()
}
