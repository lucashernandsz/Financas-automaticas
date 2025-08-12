package com.nate.autofinance.utils

import com.nate.autofinance.R

object Categories {
    val Income = Category("Renda", R.drawable.ic_cat_income)
    val Home = Category("Casa", R.drawable.ic_cat_home)
    val Food = Category("Comida", R.drawable.ic_cat_food)
    val Care = Category("Cuidados", R.drawable.ic_cat_care)
    val Education = Category("Educação", R.drawable.ic_cat_education)
    val Investment = Category("Investimento", R.drawable.ic_cat_investment)
    val Leisure = Category("Lazer", R.drawable.ic_cat_leisure)
    val Market = Category("Mercado", R.drawable.ic_cat_market)
    val Subscriptions = Category("Assinaturas", R.drawable.ic_cat_subscriptions)
    val Health = Category("Saúde", R.drawable.ic_cat_health)
    val Transport = Category("Transporte", R.drawable.ic_cat_transport)
    val Others = Category("Outros", R.drawable.ic_cat_others)

    val fixedCategories = listOf(
        Income,
        Subscriptions,
        Home,
        Food,
        Care,
        Education,
        Investment,
        Leisure,
        Market,
        Others,
        Health,
        Transport
    )

    fun find(name: String): Category {
        return fixedCategories.find { it.name.equals(name, ignoreCase = true) } ?: Others
    }

    fun getIcon(name: String): Int {
        return find(name).iconResId
    }
}
