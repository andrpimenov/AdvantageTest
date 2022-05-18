package com.advantege.test.matchers

import com.advantege.test.entity.Todo
import org.hamcrest.StringDescription

class TodoAssert(val actual: Todo?) {
    protected val errors = ArrayList<String>()

    fun equalsTo(expected: Todo) {
        checkNotNull(actual)

        if (expected.id != actual.id)
            errors.add("id:{Expected: <${expected.id}>, but <${actual.id}>")
        if (expected.text != actual.text)
            errors.add("id:{Expected: <${expected.id}>, but <${actual.id}>")
        if (expected.completed != actual.completed)
            errors.add("id:{Expected: <${expected.completed}>, but <${actual.completed}>")

        if (errors.isNotEmpty()) {
            val description = StringDescription()
            description.appendText("Todo check failed!")
                .appendText(errors.joinToString { "\n" })
        }
    }
}

fun assertTodo(actual: Todo?) = TodoAssert(actual)