package org.datumpoint.app.core.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        SurveyPointEntity::class,
        ObservationSessionEntity::class,
        ReferenceSegmentEntity::class,
        ReferenceValueEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class DatumPointDatabase : RoomDatabase() {
    abstract fun dao(): DatumPointDao
}
