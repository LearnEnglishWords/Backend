package com.learnenglish.services

import com.learnenglish.config.DbConfig
import com.learnenglish.models.Student
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import javax.inject.Singleton

interface StudentDao {
    @SqlUpdate("insert into students (first_name, initials,last_name,birth_date) values(:firstName,:initials,:lastName,:birthDate)")
    @GetGeneratedKeys
    fun save(@BindBean student: Student): Long

    @SqlQuery("select * from students")
    @RegisterBeanMapper(Student::class)
    fun findAll(): List<Student>

    @SqlQuery("select * from students where id=:id")
    fun findById(@Bind("id") id: Long): Student?

    @SqlUpdate("delete from students where id=:id")
    fun remove(@Bind("id") id: Long)
}

@Singleton
class StudentService {
    private val db = DbConfig.getInstance()

    fun save(student: Student): Long? {
        return try {
            db.onDemand<StudentDao>().save(student)
        } catch (e: Exception) {
            null
        }
    }

    fun findAll(): List<Student> {
        return db.onDemand<StudentDao>().findAll()
    }

    fun findById(id: Long): Student? {
        return try {
            db.onDemand<StudentDao>().findById(id)
        } catch (e: Exception) {
            null
        }
    }

    fun delete(id: Long): Boolean {
        return try {
            db.onDemand<StudentDao>().remove(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}