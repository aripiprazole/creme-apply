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

import creme.apply.recipe.infra.RISOTO_DESCRIPTION
import creme.apply.recipe.infra.RecipeTable
import creme.apply.shared.domain.EntityNotFoundException
import creme.apply.tool.domain.Tool
import creme.apply.tool.domain.ToolRepository
import creme.apply.tool.infra.ToolTable
import creme.apply.withTestDB
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExposedEquipmentRepositoryTests {
  class FindEquipmentTests {
    @Test
    fun `test should return null when database does not contains it`(): Unit = withTestDB {
      val toolRepository = mockk<ToolRepository>()
      val equipmentRepository = ExposedEquipmentRepository(toolRepository)

      val equipment = runBlocking {
        equipmentRepository.findEquipment(UUID.randomUUID().toString())
      }

      assertNull(equipment)
    }

    @Test
    fun `test should return an equipment when database contains it`(): Unit = withTestDB {
      val recipeEntityId = transaction {
        RecipeTable.insertAndGetId {
          it[name] = "Risoto"
          it[hero] = "..."
          it[description] = RISOTO_DESCRIPTION
        }
      }

      val toolEntityId = transaction {
        ToolTable.insertAndGetId {
          it[name] = "Faca"
          it[hero] = "..."
        }
      }

      val equipmentEntityId = transaction {
        EquipmentTable.insertAndGetId {
          it[quantity] = 1
          it[toolId] = toolEntityId
          it[recipeId] = recipeEntityId
        }
      }

      val tool = Tool(toolEntityId.value.toString(), "Faca", "... ")

      val toolRepository = mockk<ToolRepository> {
        coEvery { findTool(tool.id) } returns tool
      }
      val equipmentRepository = ExposedEquipmentRepository(toolRepository)

      val equipment = runBlocking {
        equipmentRepository.findEquipment(equipmentEntityId.value.toString())
      }

      assertNotNull(equipment)

      assertEquals(equipmentEntityId.value.toString(), equipment.id)
      assertEquals(1, equipment.quantity)
      assertEquals(tool, equipment.tool)
    }

    @Test
    fun `test should throw an error when tool repository does contains the tool`(): Unit =
      withTestDB {
        val recipeEntityId = transaction {
          RecipeTable.insertAndGetId {
            it[name] = "Risoto"
            it[hero] = "..."
            it[description] = RISOTO_DESCRIPTION
          }
        }

        val toolEntityId = transaction {
          ToolTable.insertAndGetId {
            it[name] = "Faca"
            it[hero] = "..."
          }
        }

        val equipmentEntityId = transaction {
          EquipmentTable.insertAndGetId {
            it[quantity] = 1
            it[toolId] = toolEntityId
            it[recipeId] = recipeEntityId
          }
        }

        val toolRepository = mockk<ToolRepository> {
          coEvery { findTool(toolEntityId.value.toString()) } returns null
        }
        val equipmentRepository = ExposedEquipmentRepository(toolRepository)

        assertThrows<EntityNotFoundException>("Requested entity with id $toolEntityId was not found.") {
          runBlocking {
            equipmentRepository.findEquipment(equipmentEntityId.value.toString())
          }
        }
      }
  }
}
