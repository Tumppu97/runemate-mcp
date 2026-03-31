plugins {
    kotlin("jvm") version "2.1.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.freefair.lombok") version "6.3.0"
}

apply<com.runemate.mcp.gradle.DocsPlugin>()
apply<com.runemate.mcp.gradle.ApiPlugin>()

group = "com.runemate"
version = "1.3.0"

repositories {
    maven("https://gitlab.com/api/v4/projects/10471880/packages/maven") //client
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.modelcontextprotocol:kotlin-sdk-server:0.8.4")

    val bom = platform("com.runemate:runemate-client-bom:4.17.7.0")
    implementation(bom)
    implementation("com.google.guava:guava")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")

    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3") // route SLF4J (from MCP SDK) to Log4j2

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
}

application {
    mainClass.set("com.runemate.mcp.MainKt")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

kotlin {
    jvmToolchain(17)
}
