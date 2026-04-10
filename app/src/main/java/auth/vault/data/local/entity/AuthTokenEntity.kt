package auth.vault.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "auth_tokens")
data class AuthTokenEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "token_id")
    val tokenId: Long = 0,

    @ColumnInfo(name = "service_label")
    val serviceLabel: String,

    @ColumnInfo(name = "account_name")
    val accountName: String,

    @ColumnInfo(name = "secret_key")
    val secretKey: String,

    @ColumnInfo(name = "algorithm")
    val algorithm: String = "SHA1",

    @ColumnInfo(name = "digit_count")
    val digitCount: Int = 6,

    @ColumnInfo(name = "time_step")
    val timeStep: Long = 30L,

    @ColumnInfo(name = "token_category")
    val tokenCategory: String = "totp",

    @ColumnInfo(name = "icon_color")
    val iconColor: String = "#FF9800",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,

    @ColumnInfo(name = "last_accessed")
    val lastAccessed: Long = 0L,

    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,

    @ColumnInfo(name = "token_tags")
    val tokenTags: String = "",

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "is_steam")
    val isSteam: Boolean = false,

    @ColumnInfo(name = "counter_value")
    val counterValue: Long = 0L,

    @ColumnInfo(name = "previous_code_1")
    val previousCode1: String = "",

    @ColumnInfo(name = "previous_code_2")
    val previousCode2: String = "",

    @ColumnInfo(name = "last_generated")
    val lastGenerated: Long = 0L,

    @ColumnInfo(name = "token_notes")
    val tokenNotes: String = ""
)
