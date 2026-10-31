package com.ascdev.checkmo.testing

import com.ascdev.checkmo.domain.model.Workplace
import com.ascdev.checkmo.domain.repository.WorkplaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWorkplaceRepository : WorkplaceRepository {

    val workplace = MutableStateFlow<Workplace?>(null)
    var failSaves = false
    var savedCompanyId: String? = null
        private set

    override fun observeWorkplace(companyId: String): Flow<Workplace?> = workplace

    override suspend fun saveWorkplace(companyId: String, workplace: Workplace): Result<Unit> {
        if (failSaves) return Result.failure(IllegalStateException("offline"))
        savedCompanyId = companyId
        this.workplace.value = workplace
        return Result.success(Unit)
    }
}
