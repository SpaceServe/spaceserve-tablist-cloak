package org.spaceserve.tablist.cloak

import kotlinx.serialization.Serializable
import org.spaceserve.config.IConfigure

@Serializable
data class TablistCloakConfig(
    var enableCloaking: Boolean = true,
    var hideFromOperatorsAndSpectators: Boolean = false,
) : IConfigure
