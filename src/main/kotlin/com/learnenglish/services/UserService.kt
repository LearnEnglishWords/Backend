package com.learnenglish.services

import io.micronaut.security.authentication.UserDetails
import com.learnenglish.config.DbConfig
import com.learnenglish.config.SecurityHelper
import com.learnenglish.models.Role
import com.learnenglish.models.User
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import javax.inject.Singleton

interface UserDao {
    @SqlUpdate("insert into users (username, password) values(:username, :password)")
    @GetGeneratedKeys
    fun save(@BindBean user: User): Long?

    @SqlQuery("select * from users where username=:username")
    fun getUserByUsername(@Bind("username") username: String): User?

    @SqlQuery("select * from roles where userId=:userId")
    fun getRolesByUserId(@Bind("userId") userId: Long): List<Role>

    @SqlQuery("insert into roles (userId, name) values(:userId, :name)")
    fun saveRole(@BindBean role: Role): Long?
}

@Singleton
class UserService(private val securityHelper: SecurityHelper) {
    private val db = DbConfig.getInstance()

    fun getUserByUsername(username: String): User? {
        return try {
            db.onDemand<UserDao>().getUserByUsername(username)
        } catch (x: Exception) {
            null
        }
    }

    fun save(user: User): Long? {
        return try {
            user.password = securityHelper.encodePwd(user.password)
            db.onDemand<UserDao>().save(user)
        } catch (x: Exception) {
            println(x.message)
            null
        }
    }

    fun addRole(role: Role): Long? {
        return try {
            db.onDemand<UserDao>().saveRole(role)
        } catch (x: Exception) {
            println(x.message)
            null
        }
    }

    private fun isUserValid(username: String, password: String): Boolean {
        val user = getUserByUsername(username)
        return (user != null && securityHelper.passwordValid(password, user.password))
    }

    fun authenticate(username: String, password: String): UserDetails? {
        try {
            val user = getUserByUsername(username)
            if (user != null && isUserValid(username, password)) {
                val roles = getUserRoles(user.id!!).map { role -> role.name }
                return UserDetails(user.username, java.util.ArrayList(roles))
            }
        } catch (x: Exception) {
            println(x.message)
        }
        return null
    }

    fun usernameExists(username: String): Boolean {
        return getUserByUsername(username) != null
    }

    private fun getUserRoles(userId: Long): List<Role> {
        return db.onDemand<UserDao>().getRolesByUserId(userId)
    }
}