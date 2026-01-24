package io.github.yasumichi.gme.controller

import gme.html

import gitbucket.core.controller.ControllerBase
import gitbucket.core.util.AdminAuthenticator
import io.github.yasumichi.gme.service.PluginSettingsService
import io.github.yasumichi.gme.service.PluginSettingsService._
import org.scalatra.forms._

class PluginSettingsController extends PluginSettingsControllerBase with PluginSettingsService with AdminAuthenticator

trait PluginSettingsControllerBase extends ControllerBase {

  self: PluginSettingsService with AdminAuthenticator =>

  // form of system settings
  val settingsForm: MappingValueType[GmeSettings] = mapping(
    "krokiUrl" -> text(required, maxlength(200))
  )(GmeSettings.apply)

  /**
      * click system settings menu of GitBucket Markdown Enhanced Plugin, then return html from twirl template
      */
  get("/admin/markdown-enhanced")(adminOnly {
    val settings = loadSettings()
    html.settings(settings.krokiUrl, None)
  })

  /**
      * click Apply button of system settings form of Markdown Enhanced Plugin, regist to configuration file
      */
  post("/admin/markdown-enhanced", settingsForm)(adminOnly { form =>
    assert(form.krokiUrl != null)
    saveSettings(form)
    html.settings(form.krokiUrl, Some("Settings Saved"))
  })
}
