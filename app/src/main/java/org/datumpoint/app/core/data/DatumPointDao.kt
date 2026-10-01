package org.datumpoint.app.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DatumPointDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM survey_points WHERE projectId = :projectId ORDER BY sequence ASC")
    fun observePoints(projectId: Long): Flow<List<SurveyPointEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProject(project: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPoint(point: SurveyPointEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ObservationSessionEntity): Long

    @Query("DELETE FROM survey_points WHERE id = :pointId")
    suspend fun deletePoint(pointId: Long)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: Long)
}
