package auth.vault.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import auth.vault.data.local.dao.TokenDao
import auth.vault.data.local.entity.AuthTokenEntity
import auth.vault.data.local.util.ByteArrayConverter
import auth.vault.data.local.util.InstantConverter
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

abstract class VaultDatabase : RoomDatabase() {

    abstract fun tokenDao(): TokenDao

    companion object {
        private const val DB_NAME = "authvault_db"

        @Volatile
        private var INSTANCE: VaultDatabase? = null

        fun getInstance(context: Context, passphrase: ByteArray): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context, passphrase)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context, passphrase: ByteArray): VaultDatabase {
            SQLiteDatabase.loadLibs(context)
            val factory = SupportFactory(passphrase)

            return Room.databaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .openHelperFactory(factory)
                .build()
        }
    }
}
