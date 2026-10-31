package com.ascdev.checkmo.domain.repository

import com.ascdev.checkmo.domain.model.Workplace
import kotlinx.coroutines.flow.Flow

interface WorkplaceRepository {

    /** Workplace of [companyId], or `null` while the company has not configured one. */
    fun observeWorkplace(companyId: String): Flow<Workplace?>

    suspend fun saveWorkplace(companyId: String, workplace: Workplace): Result<Unit>
}
