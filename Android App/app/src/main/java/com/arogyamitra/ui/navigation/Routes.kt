package com.arogyamitra.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object WelcomeRoute

@Serializable
object ModelImportRoute

@Serializable
data class ChatRoute(val systemPrompt: String? = null)

@Serializable
object PpgRoute

@Serializable
object ModelSelectRoute

@Serializable
object LoginRoute

@Serializable
object MarketplaceRoute

@Serializable
object DeveloperRoute

@Serializable
data class LoraDetailRoute(val modelId: String)
