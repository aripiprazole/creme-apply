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

@file:Suppress("UnstableApiUsage")

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
  kotlin("multiplatform") version "1.6.21"
  kotlin("plugin.serialization") version "1.6.21"
  id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
  id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

group = "com.gabrielleeg1"
version = "1.0.0"

repositories {
  mavenCentral()
}

configure<KtlintExtension> {
  android.set(false)
  additionalEditorconfigFile.set(rootProject.file(".editorconfig"))
}

configure<DetektExtension> {
  buildUponDefaultConfig = true
  allRules = false

  config = files("${rootProject.projectDir}/config/detekt.yml")
  baseline = file("${rootProject.projectDir}/config/baseline.xml")
}

configure<KotlinMultiplatformExtension> {
  jvm {
    withJava()

    compilations.all {
      kotlinOptions.jvmTarget = "1.8"
    }

    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
      testLogging.showStandardStreams = true
      testLogging.exceptionFormat = TestExceptionFormat.FULL
    }
  }

  sourceSets {
    val commonMain by getting
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.exposed.core)
        implementation(libs.exposed.jdbc)

        implementation(libs.ktor.server.core)
        implementation(libs.ktor.server.netty)
        implementation(libs.ktor.server.statusPages)
        implementation(libs.ktor.server.defaultHeaders)
        implementation(libs.ktor.server.contentNegotiation)

        implementation(libs.ktor.serialization.kotlinxJson)

        implementation(libs.log4j2.slf4j.impl)
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(kotlin("test-junit5"))

        implementation(libs.h2)
        implementation(libs.mockk)

        implementation(libs.exposed.core)

        implementation(libs.log4j2.slf4j.impl)
      }
    }
  }
}
