package com.advantege.test.client

import com.advantege.test.configuration.TestConfig
import com.advantege.test.entity.Todo
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.response.ValidatableResponse
import io.restassured.specification.RequestSpecification

class TodoAppClient {
    lateinit var spec: RequestSpecification
    val config = TestConfig()

    private fun initializeSpec(): RequestSpecification {
        return RequestSpecBuilder()
            .setBaseUri(config.endpoint)
            .setPort(config.port)
            .build()
    }

    fun getTodos(
        offset: Int? = null,
        limit: Int? = null,
        responseCode: Int? = null): List<Todo> {
        if (!::spec.isInitialized)
            spec = initializeSpec()

        val request = RestAssured.given().spec(spec)
        offset?.let { request.queryParam("offset", it) }
        limit?.let { request.queryParam("limit", it) }

        val response = request
            .get("/todos")
            .then()

        responseCode?.let {
            response.statusCode(it)
        }

        return response.extract().body().jsonPath().getList(".", Todo::class.java)
    }

    fun createTodo(todo: Todo, responseCode: Int? = 201): ValidatableResponse {
        if (!::spec.isInitialized)
            spec = initializeSpec()

        val response = RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .body(todo)
            .post("/todos")
            .then()

        responseCode?.let { response.statusCode(responseCode) }

        return response
    }

    fun updateTodo(id: Long, todo: Todo, responseCode: Int = 200): ValidatableResponse {
        if (!::spec.isInitialized)
            spec = initializeSpec()

        return RestAssured.given()
            .spec(spec)
            .log().all()
            .contentType(ContentType.JSON)
            .body(todo)
            .pathParam("id", id)
            .put("/todos/{id}")
            .then()
            .log().all()
            .statusCode(responseCode)
    }

    fun deleteTodo(user: String?, pwd: String?, id: Long, responseCode: Int = 204): ValidatableResponse {
        val specWithAuth = RequestSpecBuilder()
            .setBaseUri(config.endpoint)
            .setPort(config.port)
            .build()

        if (user != null && pwd != null)
            specWithAuth.auth().preemptive().basic(user, pwd)

        return RestAssured.given()
            .spec(specWithAuth)
            .pathParam("id", id)
            .delete("/todos/{id}")
            .then()
            .statusCode(responseCode)
    }
}