plugins {
    kotlin("jvm") version "1.8.20"
    application
}

group = "decisiontree"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.jnagmp:jnagmp:3.0.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.72")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

application {
    applicationName = "DecisionTree"
    mainClass.set("decisiontree.main.MPCTrainingKt")
    applicationDefaultJvmArgs = listOf("-Djava.lang.Integer.IntegerCache.high=256", "-Djava.util.concurrent.ForkJoinPool.common.parallelism=16", "-Xmx40G", "-Xms10G")
}
val startScriptsSingleThreaded by tasks.register("startScriptsSingleThreaded", CreateStartScripts::class) {
    applicationName = "DecisionTreeSingleThreaded"
    mainClass.set("decisiontree.main.MPCTrainingKt")
    defaultJvmOpts = listOf("-Djava.lang.Integer.IntegerCache.high=256", "-Djava.util.concurrent.ForkJoinPool.common.parallelism=1", "-Xmx40G", "-Xms10G")
    classpath = tasks.startScripts.get().classpath
    outputDir = tasks.startScripts.get().outputDir
}
tasks.startScripts.get().dependsOn(startScriptsSingleThreaded)
val startScriptsOreBenchmark by tasks.register("startScriptsOreBenchmark", CreateStartScripts::class) {
    applicationName = "OreBenchmark"
    mainClass.set("decisiontree.main.OreBenchmarkKt")
    classpath = tasks.startScripts.get().classpath
    outputDir = tasks.startScripts.get().outputDir
}
tasks.startScripts.get().dependsOn(startScriptsOreBenchmark)
