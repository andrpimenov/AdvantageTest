package com.advantege.test

import com.advantege.test.client.TodoAppClient
import com.advantege.test.configuration.TestConfig
import com.advantege.test.entity.Todo
import com.advantege.test.matchers.assertTodo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TempIT {
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

    @BeforeEach
    fun init() {
        println("executed before each")
    }

    @Test
    fun `Create todo - sanity case`() {
        val todo = Todo(IdGenerator.getId(), "Some todo", true)

        todoClient.createTodo(todo)
        val actualTodo = todoClient.getTodos().firstOrNull { it.id == todo.id }
        assertTodo(actualTodo).equalsTo(todo)
    }

    @Test
    fun `Impossible to create todo with the same id`() {
        val todo = Todo(IdGenerator.getId(), "Some todo", true)
        todoClient.createTodo(todo)
        val todo2 = Todo(todo.id, "Another todo with the same id", true)
        todoClient.createTodo(todo2, 400)
    }

    @Test
    fun `Impossible to create todo without text`() {
        val todo = Todo(IdGenerator.getId(), null, true)
        todoClient.createTodo(todo, 400)
    }

    @Test
    fun `Impossible to create todo without completed flag`() {
        val todo = Todo(IdGenerator.getId(), "some text",null)
        todoClient.createTodo(todo, 400)
    }

    @Test
    fun `Impossible to create todo without id`() {
        val todo = Todo(null, "some text",true)
        todoClient.createTodo(todo, 400)
    }

    @Test
    fun `Get method - check limit param`() {
        val todos = listOf(
            Todo(IdGenerator.getId(), "some text", true),
            Todo(IdGenerator.getId(), "some text", true),
        )
        todos.forEach { todoClient.createTodo(it) }

        val response = todoClient.getTodos(null, 1)
        assertEquals(1, response.size)
    }

    @Test
    fun `Get method - check offset param`() {
        deleteAllTodos()

        val todos = listOf(
            Todo(IdGenerator.getId(), "some text", true),
            Todo(IdGenerator.getId(), "some text", true),
        )
        todos.forEach { todoClient.createTodo(it) }

        val response = todoClient.getTodos(1, null)
        assertEquals(1, response.size)
    }

    @Test
    fun `Get method - check both offest and limit params`() {
        deleteAllTodos()

        val todos = listOf(
            Todo(IdGenerator.getId(), "some text1", true),
            Todo(IdGenerator.getId(), "some text2", true),
            Todo(IdGenerator.getId(), "some text3", true),
            Todo(IdGenerator.getId(), "some text4", true),
        )
        todos.forEach { todoClient.createTodo(it) }

        val response1 = todoClient.getTodos(0, 2, null)
        val response2 = todoClient.getTodos(2, 2, null)
        assertEquals(2, response1.size)
        assertEquals(2, response2.size)
        assertTodo(todos.get(0)).equalsTo(response1.get(0))
        assertTodo(todos.get(1)).equalsTo(response1.get(1))
        assertTodo(todos.get(2)).equalsTo(response2.get(0))
        assertTodo(todos.get(3)).equalsTo(response2.get(1))
    }

    @Test
    fun `Update todo - text param`() {
        val todo = Todo(IdGenerator.getId(), "some text",true)
        todoClient.createTodo(todo)

        val updatedTodo = todo.copy(text = "new text")
        todoClient.updateTodo(todo.id!!, updatedTodo)

        assertTodo(
            todoClient.getTodos().firstOrNull{ it.id == updatedTodo.id }
        ).equalsTo(updatedTodo)
    }

    @Test
    fun `Update todo - completed param`() {
        val todo = Todo(IdGenerator.getId(), "some text",true)
        todoClient.createTodo(todo)

        val updatedTodo = todo.copy(completed = false)
        todoClient.updateTodo(todo.id!!, updatedTodo)

        assertTodo(
            todoClient.getTodos().firstOrNull{ it.id == updatedTodo.id }
        ).equalsTo(updatedTodo)
    }

    @Test
    fun `Update todo - id param`() {
        val todo = Todo(IdGenerator.getId(), "some text",true)
        todoClient.createTodo(todo)

        val updatedTodo = todo.copy(id = IdGenerator.getId())
        todoClient.updateTodo(todo.id!!, updatedTodo)

        assertTodo(
            todoClient.getTodos().firstOrNull{ it.id == updatedTodo.id }
        ).equalsTo(updatedTodo)
    }

    @Test
    fun `Update todo - without changes`() {
        val todo = Todo(IdGenerator.getId(), "some text",true)
        todoClient.createTodo(todo)

        todoClient.updateTodo(todo.id!!, todo)

        assertTodo(
            todoClient.getTodos().firstOrNull{ it.id == todo.id }
        ).equalsTo(todo)
    }

    @Test
    fun `Update todo - unknown id - error code is returned`() {
        val todo = Todo(IdGenerator.getId(), "some text",true)
        todoClient.createTodo(todo)

        todoClient.updateTodo(IdGenerator.getId(), todo, 404)
    }

    @Test
    fun `Delete todo`() {
        val todo = Todo(IdGenerator.getId(), "some text",true)
        todoClient.createTodo(todo)

        todoClient.deleteTodo(config.user, config.password, todo.id!!)
        val response = todoClient.getTodos()

        assertNull(response.firstOrNull{ it.id == todo.id })
    }

    @Test
    fun `Delete todo - unknown id - error code is returned`() {
        todoClient.deleteTodo(config.user, config.password, IdGenerator.getId(), 404)
    }

    @Test
    fun `Delete todo - without credentials - no permissions`() {
        val todo = Todo(IdGenerator.getId(), "some text",true)
        todoClient.deleteTodo(null, null, todo.id!!, 401)
    }

    @Test
    fun `Delete todo - wrong credentials - no permissions`() {
        val todo = Todo(IdGenerator.getId(), "some text",true)
        todoClient.deleteTodo("user", "pwd", todo.id!!, 401)
    }
}

object IdGenerator {
    private var id = 1L
    fun getId() = id++
}