package com.learnenglish.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Value
import io.micronaut.context.env.Environment
import org.jdbi.v3.core.Jdbi
import javax.inject.Singleton
import javax.sql.DataSource

@Singleton
class DbConfig {
    companion object {
        fun getInstance(): Jdbi {
            val jdbi = Jdbi.create(DbConfig().dataSource())
            jdbi.installPlugins()
            return jdbi
        }
    }

    @Factory
    @Replaces(DataSource::class)
    fun dataSource(): DataSource {
        val mysqlAddress = System.getenv("MYSQL_ADDRESS")
        val mysqlPort = System.getenv("MYSQL_PORT")
        val mysqlDatabase = System.getenv("MYSQL_DATABASE")

        var config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://${mysqlAddress}:${mysqlPort}/${mysqlDatabase}"
        config.username = System.getenv("MYSQL_USER")
        config.password = System.getenv("MYSQL_PASSWORD")
        return HikariDataSource(config)
    }
}