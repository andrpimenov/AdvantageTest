package com.advantege.test

import com.advantege.test.client.TodoAppClient
import com.advantege.test.configuration.TestConfig
import com.advantege.test.entity.Todo
import io.restassured.response.ValidatableResponse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlinx.coroutines.*
import org.junit.jupiter.api.Disabled
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
class PerformanceIT {
    val todoClient = TodoAppClient()
    val config = TestConfig()

    @BeforeAll
    fun before() {
        deleteAllTodos()
    }

    private fun deleteAllTodos() {
        val todos = todoClient.getTodos()

        todos.forEach {
            todoClient.deleteTodo(
                config.user, config.password, it.id!!
            )
        }
    }

    @Test
    fun `performance - sequential requests`() {
        val timestamps = ArrayList<Long>()

        (1..10000).forEachIndexed { i, it ->
            val todo = Todo(IdGenerator.getId(), "test", true)

            val currentTimeMills = measureTimeMillis {
                todoClient.createTodo(todo)
            }

            timestamps.add(currentTimeMills)
        }

        println("Average time: " + timestamps.average())
        println("Min time: " + timestamps.minByOrNull { it })
        println("Max time: " + timestamps.maxByOrNull { it })
        println("Number of requests more then 10ms: " + timestamps.filter { it > 10 }.size)
        println("Number of requests more then 20ms: " + timestamps.filter { it > 20 }.size)
        println("Number of requests more then 50ms: " + timestamps.filter { it > 50 }.size)
        println("Number of requests more then 100ms: " + timestamps.filter { it > 100 }.size)
    }

    @Test
    fun `performance - parallel requests`() {
        val timestamps = ArrayList<Long>()
        val numberOfErrors = AtomicInteger(0)

        (1..200).forEach {
            runBlocking {
                val tasks = (1..20)
                    .map {
                        async(Dispatchers.IO) {
                            var response: ValidatableResponse
                            val currentTimeMills = measureTimeMillis {
                                response = todoClient.createTodo(Todo(IdGenerator.getId(), "test", true), null)
                            }
                            if (response.extract().statusCode() != 201)
                                numberOfErrors.incrementAndGet()

                            timestamps.add(currentTimeMills)
                        }
                    }

                tasks.awaitAll()
            }
        }

        println("Number of errors: $numberOfErrors")
        println("Average time: " + timestamps.average())
        println("Min time: " + timestamps.minByOrNull { it })
        println("Max time: " + timestamps.maxByOrNull { it })
        println("Number of requests more then 10ms: " + timestamps.filter { it > 10 }.size)
        println("Number of requests more then 20ms: " + timestamps.filter { it > 20 }.size)
        println("Number of requests more then 50ms: " + timestamps.filter { it > 50 }.size)
        println("Number of requests more then 100ms: " + timestamps.filter { it > 100 }.size)
    }
}