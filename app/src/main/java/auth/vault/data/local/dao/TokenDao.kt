package auth.vault.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import auth.vault.data.local.entity.AuthTokenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenDao {

    @Query("SELECT * FROM auth_tokens ORDER BY is_pinned DESC, last_accessed DESC")
    fun getAllTokens(): Flow<List<AuthTokenEntity>>

    @Query("SELECT * FROM auth_tokens WHERE token_category = :category ORDER BY is_pinned DESC, last_accessed DESC")
    fun getTokensByCategory(category: String): Flow<List<AuthTokenEntity>>

    @Query("SELECT * FROM auth_tokens WHERE token_id = :tokenId")
    suspend fun getTokenById(tokenId: Long): AuthTokenEntity?

    @Query("SELECT COUNT(*) FROM auth_tokens")
    fun getTokenCount(): Flow<Int>

    @Query("SELECT DISTINCT token_tags FROM auth_tokens WHERE token_tags != ''")
    fun getAllTags(): Flow<List<String>>

    @Query("SELECT * FROM auth_tokens WHERE token_tags LIKE '%' || :tag || '%'")
    fun getTokensByTag(tag: String): Flow<List<AuthTokenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: AuthTokenEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokens(tokens: List<AuthTokenEntity>): List<Long>

    @Update
    suspend fun updateToken(token: AuthTokenEntity)

    @Delete
    suspend fun deleteToken(token: AuthTokenEntity)

    @Query("DELETE FROM auth_tokens WHERE token_id = :tokenId")
    suspend fun deleteTokenById(tokenId: Long)

    @Query("UPDATE auth_tokens SET last_accessed = :timestamp, usage_count = usage_count + 1 WHERE token_id = :tokenId")
    suspend fun recordTokenAccess(tokenId: Long, timestamp: Long)

    @Query("UPDATE auth_tokens SET is_pinned = :pinned WHERE token_id = :tokenId")
    suspend fun togglePinToken(tokenId: Long, pinned: Boolean)

    @Query("UPDATE auth_tokens SET previous_code_2 = previous_code_1, previous_code_1 = :code, last_generated = :timestamp WHERE token_id = :tokenId")
    suspend fun updateTokenHistory(tokenId: Long, code: String, timestamp: Long)

    @Query("DELETE FROM auth_tokens")
    suspend fun clearAllTokens()

    @Query("UPDATE auth_tokens SET secret_key = :secret, algorithm = :algo, digit_count = :digits, time_step = :timeStep, is_steam = :isSteam WHERE token_id = :tokenId")
    suspend fun updateTokenDetails(tokenId: Long, secret: String, algo: String, digits: Int, timeStep: Long, isSteam: Boolean)
}
