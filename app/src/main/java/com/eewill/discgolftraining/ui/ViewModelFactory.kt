package com.eewill.discgolftraining.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eewill.discgolftraining.DiscGolfApp
import com.eewill.discgolftraining.data.ApproachRoundRepository
import com.eewill.discgolftraining.data.DiscRepository
import com.eewill.discgolftraining.data.RoundRepository

fun Context.repository(): RoundRepository =
    (applicationContext as DiscGolfApp).repository

fun Context.discRepository(): DiscRepository =
    (applicationContext as DiscGolfApp).discRepository

fun Context.approachRoundRepository(): ApproachRoundRepository =
    (applicationContext as DiscGolfApp).approachRoundRepository

inline fun <reified VM : ViewModel> simpleFactory(
    crossinline create: (CreationExtras) -> VM,
) = viewModelFactory {
    initializer { create(this) }
}
