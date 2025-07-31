package com.respiroc.user.domain.repository

import com.respiroc.user.domain.model.Role
import com.respiroc.util.repository.CustomJpaRepository
import org.apache.el.stream.Optional
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : CustomJpaRepository<Role, Long> {

    fun findByCode(code: String): Role?

}
