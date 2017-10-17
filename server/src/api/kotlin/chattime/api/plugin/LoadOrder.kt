package chattime.api.plugin

/**
 * Represents an entry in the plugin load order list.
 *
 * @constructor The primary constructor.
 *
 * @param id the other plugin
 * @param isRequired set to true if the other plugin is required
 */
sealed class LoadOrder(
    /** The other plugin. */
    val id: String,
    /** If this is true, the other plugin is required. */
    val isRequired: Boolean)
{
    /**
     * Loads this plugin before the other plugin.
     *
     * @constructor The primary constructor.
     *
     * @param id the other plugin
     * @param isRequired set to true if the other plugin is required
     */
    class Before(id: String, isRequired: Boolean = false) : LoadOrder(id, isRequired)

    /**
     * Loads this plugin after the other plugin.
     *
     * @constructor The primary constructor.
     *
     * @param id the other plugin
     * @param isRequired set to true if the other plugin is required
     */
    class After(id: String, isRequired: Boolean = false) : LoadOrder(id, isRequired)
}
