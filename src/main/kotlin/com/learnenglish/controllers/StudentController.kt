package com.learnenglish.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import com.learnenglish.services.StudentService
import com.learnenglish.models.Student
import javax.validation.Valid

@Validated
@Controller("/student")
@Secured(SecurityRule.IS_AUTHENTICATED)
class StudentController(private val studentService: StudentService) {

    @Get("/list")
    @Produces(MediaType.APPLICATION_JSON)
    fun students(): HttpResponse<List<Student>> {
        return HttpResponse.ok(studentService.findAll())
    }

    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun student(id: Long): HttpResponse<Student> {
        val student = studentService.findById(id)
        return if (student != null) {
            HttpResponse.ok(student)
        } else HttpResponse.notFound()
    }

    @Post("/")
    @Consumes(MediaType.APPLICATION_JSON)
    fun save(@Body @Valid student: Student): HttpResponse<String> {
        val result = studentService.save(student)
        return if (result != null) {
            HttpResponse.created("created $result")
        } else HttpResponse.badRequest()
    }

    @Delete("/{id}")
    fun remove(id: Long): HttpResponse<Any> {
        val student = studentService.findById(id) ?: return HttpResponse.notFound()

        studentService.delete(student.id!!)
        return HttpResponse.ok()
    }
}