plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
        }
    }
}
