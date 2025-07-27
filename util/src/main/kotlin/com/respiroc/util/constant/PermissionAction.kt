package com.respiroc.util.constant

enum class PermissionAction(val level: Int) {
    WRITE(3),
    SUBMIT(2),
    READ(1);

    companion object {
        fun fromCode(code: String): PermissionAction? = entries.find { it.name.equals(code, ignoreCase = true) }
    }

    fun includes(other: PermissionAction): Boolean = this.level >= other.level
}
