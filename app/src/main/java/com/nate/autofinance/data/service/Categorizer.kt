package com.nate.autofinance.data.categorization

import com.nate.autofinance.utils.Categories

object Categorizer {

    private val rules = mapOf(
        "IFD*" to Categories.Food,
        "BIG BOCA" to Categories.Food,
        "COVABRA SUPERMERCADOS" to Categories.Market,
        "SUPERMERCADO SANTA RIT" to Categories.Market,
        "Enxuto" to Categories.Market,
        "ATACADAO" to Categories.Market,
        "FARMACIA E DROGARIA" to Categories.Health,
        "Amazon Servicos" to Categories.Market,
        "UBER*" to Categories.Transport,
        "AUTOPASS" to Categories.Transport,
        "NETFLIX" to Categories.Subscriptions,
        "SPOTIFY" to Categories.Subscriptions,
        "AMAZON PRIME" to Categories.Subscriptions,
        "ChatGPT" to Categories.Subscriptions,
        "DL GOOGLE" to Categories.Subscriptions,
        "TickTick" to Categories.Subscriptions,
        "IFOOD CLUB" to Categories.Subscriptions,
        "Mp *jaquelineduart" to Categories.Investment,
        "Tinho Lanches" to Categories.Food,
        "Barbaros Premium Bar" to Categories.Food,
        "Shopping Da Utilidade" to Categories.Market,
        "Cheers" to Categories.Leisure,
        "Esquina do Acai" to Categories.Food,
        "Tropically" to Categories.Food,
        "V 8" to Categories.Food,
        "99POP" to Categories.Transport,
        "Vb Transportes" to Categories.Transport,
        "Vb Campinas" to Categories.Transport,
    )

    fun categorize(text: String): String {
        for ((keyword, category) in rules) {
            if (text.contains(keyword, ignoreCase = true)) {
                return category.name
            }
        }
        return Categories.Others.name
    }
}
