package io.github.yasumichi.gme.service

import gitbucket.core.util.Directory._
import java.io.File
import PluginSettingsService._
import scala.util.Using

/**
  * trait for system settings for GitBucket Markdown Enhanced Plugin
  */
trait PluginSettingsService {
    // configuration file of GitBucket Markdown Enhanced Plugin
    val gmeConfig = new File(GitBucketHome, "gme.conf")

    /**
        * save settings of GitBucket Markdown Enhanced Plugin
        *
        * @param settings settings of GitBucket Markdown Enhanced Plugin
        */
    def saveSettings(settings: GmeSettings): Unit = {
        val props = new java.util.Properties()
        props.setProperty(krokiUrl, settings.krokiUrl)
        Using.resource(new java.io.FileOutputStream(gmeConfig)) { out =>
            props.store(out, null)
        }
    }

    /**
      * load settings of GitBucket Markdown Enhanced Plugin
      *
      * @return settings of GitBucket Markdown Enhanced Plugin
      */
    def loadSettings(): GmeSettings = {
        val props = new java.util.Properties()
        if (gmeConfig != null && gmeConfig.exists) {
            Using.resource(new java.io.FileInputStream(gmeConfig)) { in =>
                props.load(in)
            }
        }
        GmeSettings(
            getValue[String](props, krokiUrl, "https://kroki.io")  
        )
    }
}

object PluginSettingsService {
    import scala.reflect.ClassTag

    case class GmeSettings(krokiUrl: String)
    case class KrokiParams(diagram_source: String, diagram_type: String, output_format: String)

    private val krokiUrl = "krokiUrl"

    /**
      * get value of settings for GitBucket Markdown Enhanced Plugin
      *
      * @param props properties
      * @param key name of setting
      * @param default default value
      * @return value of settings
      */
    private def getValue[A: ClassTag](props: java.util.Properties, key: String, default: A): A = {
        val value = props.getProperty(key)
        if (value == null || value.isEmpty) default
        else convertType(value).asInstanceOf[A]
    }

    /**
      *  get value of settings for GitBucket Markdown Enhanced Plugin (Option type)
      *
      * @param props properties
      * @param key name of setting
      * @param default default value
      * @return value of settings
      */
    private def getOptionValue[A: ClassTag](props: java.util.Properties,
                                            key: String,
                                            default: Option[A]): Option[A] = {
        val value = props.getProperty(key)
        if (value == null || value.isEmpty) default
        else Some(convertType(value)).asInstanceOf[Option[A]]
    }

    /**
      * convert value type
      *
      * @param value string of value
      * @return converted value
      */
    private def convertType[A: ClassTag](value: String) = {
        val c = implicitly[ClassTag[A]].runtimeClass
        if (c == classOf[Boolean]) value.toBoolean
        else if (c == classOf[Int]) value.toInt
        else value
    }

}
