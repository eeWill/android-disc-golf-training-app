package com.eewill.discgolftraining

import android.app.Application
import com.eewill.discgolftraining.data.AppDatabase
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.DiscRepository
import com.eewill.discgolftraining.data.PuttingRoundRepository
import com.eewill.discgolftraining.data.RoundRepository

class DiscGolfApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.build(this) }
    val repository: RoundRepository by lazy { RoundRepository(database.roundDao()) }
    val discRepository: DiscRepository by lazy { DiscRepository(database.discDao()) }
    val approachRoundRepository: ApproachRoundRepository by lazy {
        ApproachRoundRepository(database.approachRoundDao())
    }
    val puttingRoundRepository: PuttingRoundRepository by lazy {
        PuttingRoundRepository(database.puttingRoundDao())
    }
}
