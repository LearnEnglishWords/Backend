package com.learnenglish.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import com.learnenglish.services.UserService
import com.learnenglish.models.Role
import com.learnenglish.models.User
import javax.validation.Valid

@Validated
@Controller("/users")
@Secured(SecurityRule.IS_ANONYMOUS)
class UserController(private val userService: UserService) {

    @Post("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun save(@Body @Valid user: User): HttpResponse<String> {
        if (userService.usernameExists(user.username)) {
            return HttpResponse.badRequest("Username '${user.username}' already taken.")
        }
        val result = userService.save(user)
        userService.addRole(Role(userId = user.id, name = "USER"))
        return if (result != null) {
            HttpResponse.created("created $result")
        } else HttpResponse.badRequest("unable to create user!")
    }
}