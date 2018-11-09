package main.jdbc

import java.util.*
import javax.sql.DataSource
import kotlin.collections.LinkedHashMap

class SqlTemplate(val dataSource: DataSource) {

    fun first(sql: String, vararg params: Any): Optional<Map<String, Any>>{
        dataSource.getConnection().use { conn ->
            val stmt = conn.prepareStatement(sql)
            for ((i, v) in params.withIndex()){
                stmt.setObject(i+1, v)
            }
            val rs = stmt.executeQuery()

            return if (rs.next()){
                val map = LinkedHashMap<String, Any>()
                val rsm = rs.metaData
                val columnCount = rsm.columnCount

                for(i in 1..columnCount){
                    map[rsm.getColumnName(i)] = rs.getObject(i)
                }
                Optional.of(map)
            } else {
                Optional.empty()
            }
        }
    }

    fun list(sql: String, vararg params: Any?): List<Map<String, Any>>{
        val result = ArrayList<Map<String, Any>>()

        dataSource.getConnection().use{ conn ->
            val stmt = conn.prepareStatement(sql)
            for (i in 1..params.size){
                stmt.setObject(i, params[i-1])
            }
            val rs = stmt.executeQuery()
            val rsm = rs.metaData
            val columnCount = rsm.columnCount

            while(rs.next()){
                val map = LinkedHashMap<String, Any>()
                for(i in 1..columnCount){
                    map.put(rsm.getColumnName(i), rs.getObject(i))
                }
                result.add(map)
            }
        }
        return result
    }
}
