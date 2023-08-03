package com.dicoding.courseschedule.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.dicoding.courseschedule.util.QueryType
import com.dicoding.courseschedule.util.QueryUtil
import com.dicoding.courseschedule.util.QueryUtil.sortedQuery
import com.dicoding.courseschedule.util.SortType
import com.dicoding.courseschedule.util.executeThread
import java.util.*

//TODO 4 : Implement repository with appropriate dao
class DataRepository(private val dao: CourseDao) {
    fun getNearestSchedule(queryType: QueryType): LiveData<Course?> {
        val nearQuery: SupportSQLiteQuery = QueryUtil.nearestQuery(type = queryType)
        return dao.getNearestSchedule(nearQuery)
    }

    fun getAllCourse(sortType: SortType): LiveData<PagedList<Course>> {
        val query: SupportSQLiteQuery = when (sortType) {
            SortType.TIME -> SimpleSQLiteQuery("SELECT * FROM course ORDER BY startTime ASC")
            SortType.COURSE_NAME -> SimpleSQLiteQuery("SELECT * FROM course ORDER BY courseName ASC")
            SortType.LECTURER -> SimpleSQLiteQuery("SELECT * FROM course ORDER BY lecturer ASC")
        }
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(PAGE_SIZE)
            .build()
        return LivePagedListBuilder(dao.getAll(query), config).build()
    }

    fun getCourse(id: Int): LiveData<Course> {
        return dao.getCourse(id)
    }

    fun getTodaySchedule(): List<Course> {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val adjustedDayOfWeek = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
        return dao.getTodaySchedule(adjustedDayOfWeek)
    }


    fun insert(course: Course) = executeThread {
        dao.insert(course)
    }

    fun delete(course: Course) = executeThread {
        dao.delete(course)
    }

    companion object {
        @Volatile
        private var instance: DataRepository? = null
        private const val PAGE_SIZE = 10

        fun getInstance(context: Context): DataRepository? {
            return instance ?: synchronized(DataRepository::class.java) {
                if (instance == null) {
                    val database = CourseDatabase.getInstance(context)
                    instance = DataRepository(database.courseDao())
                }
                return instance
            }
        }
    }
}