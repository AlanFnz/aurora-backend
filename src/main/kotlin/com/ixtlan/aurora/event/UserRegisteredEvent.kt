package com.ixtlan.aurora.event

import com.ixtlan.aurora.entity.User
import org.springframework.context.ApplicationEvent

class UserRegisteredEvent(
    source: Any,
    val user: User
) : ApplicationEvent(source)
