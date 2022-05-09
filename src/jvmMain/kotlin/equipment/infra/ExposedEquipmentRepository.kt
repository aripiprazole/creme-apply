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

package creme.apply.equipment.infra

import creme.apply.equipment.domain.Equipment
import creme.apply.equipment.domain.EquipmentRepository
import creme.apply.recipe.domain.Recipe
import creme.apply.shared.domain.EntityNotFoundException
import creme.apply.tool.domain.ToolRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ExposedEquipmentRepository(private val toolRepository: ToolRepository) : EquipmentRepository {
  override suspend fun findEquipment(id: String): Equipment? = newSuspendedTransaction {
    EquipmentTable
      .select { EquipmentTable.id eq UUID.fromString(id) }
      .map { it.toEquipment(toolRepository) }
      .firstOrNull()
  }

  override suspend fun getEquipmentsByRecipe(recipe: Recipe): Set<Equipment> {
    TODO("Not yet implemented")
  }
}

private suspend fun ResultRow.toEquipment(toolRepository: ToolRepository): Equipment {
  val toolId = this[EquipmentTable.toolId].value.toString()

  return Equipment(
    id = this[EquipmentTable.id].value.toString(),
    quantity = this[EquipmentTable.quantity],
    tool = toolRepository.findTool(toolId) ?: throw EntityNotFoundException(toolId)
  )
}
