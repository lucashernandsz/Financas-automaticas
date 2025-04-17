package com.nate.autofinance.utils

import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

fun LocalDate.toDate(): Date =
    Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
