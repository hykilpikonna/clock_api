package org.hydev.clock_api.entity

import com.sun.istack.NotNull
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

/**
 * TODO: Write a description for this class!
 *
 * @author HyDEV Team (https://github.com/HyDevelop)
 * @author Hykilpikonna (https://github.com/hykilpikonna)
 * @author Vanilla (https://github.com/VergeDX)
 * @since 2021-01-24 10:18
 */
@Entity(name = "family")
data class Family(
    @Id
    @GeneratedValue
    var fid: Long = 0,

    @NotNull
    var name: String = "",

    @NotNull
    var members: String = "",

    @NotNull
    var pinHash: String = "",
)

interface FamilyRepo: JpaRepository<Family, Long>
