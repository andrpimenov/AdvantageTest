package com.advantege.test.configuration

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class TestConfig {

    val config: Config

    init {
        val env = System.getProperty("env") ?: "local"

        config = ConfigFactory.load("$env.conf")
    }

    val endpoint: String by lazy {
        config.getString("todo.endpoint")
    }

    val port: Int by lazy {
        config.getInt("todo.port")
    }

    val user: String by lazy {
        config.getString("todo.user")
    }

    val password: String by lazy {
        config.getString("todo.password")
    }
}