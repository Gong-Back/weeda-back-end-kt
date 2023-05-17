package gongback.pureureum.domain.dashboardbulletinboard

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "dashboard_bulletin_board_comment",
    indexes = [Index(name = "idx_dashboard_bulletin_board_user_id", columnList = "user_id")]
)
class DashboardBulletinBoardComment(
    @Column(nullable = false, name = "user_id")
    val userId: Long,

    @Column(length = 200, nullable = false)
    val content: String,

    @Column(nullable = false)
    val depth: Int,

    @Column(nullable = false)
    val parentCommentId: Long
) : BaseEntity()
