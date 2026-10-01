package org.datumpoint.app.core.export

import org.datumpoint.app.core.geometry.Geometry
import org.datumpoint.app.core.geometry.Point2d
import org.datumpoint.app.core.model.ProjectModel
import org.datumpoint.app.core.model.SurveyPointModel
import java.util.Locale

object Exporters {
    fun csv(project: ProjectModel, points: List<SurveyPointModel>): String {
        val header = listOf(
            "project",
            "point",
            "sequence",
            "latitude",
            "longitude",
            "easting",
            "northing",
            "altitude",
            "crs",
            "timestamp",
            "duration_seconds",
            "accepted_fixes",
            "rejected_fixes",
            "reported_accuracy_median",
            "sigma_e",
            "sigma_n",
            "radial_rms",
            "satellites_used",
            "quality",
            "notes",
        ).joinToString(",")
        val rows = points.joinToString("\n") { point ->
            listOf(
                project.name.csvEscape(),
                point.name.csvEscape(),
                point.sequence.toString(),
                "%.8f".format(Locale.US, point.latitude),
                "%.8f".format(Locale.US, point.longitude),
                "%.3f".format(Locale.US, point.easting),
                "%.3f".format(Locale.US, point.northing),
                point.altitude?.let { "%.3f".format(Locale.US, it) } ?: "",
                project.crs.csvEscape(),
                "",
                point.durationSeconds.toString(),
                point.acceptedFixes.toString(),
                point.rejectedFixes.toString(),
                "%.2f".format(Locale.US, point.medianReportedAccuracy),
                "",
                "",
                "%.3f".format(Locale.US, point.radialRms),
                "",
                point.qualityClass.name,
                point.notes.csvEscape(),
            ).joinToString(",")
        }
        return "$header\n$rows\n"
    }

    fun txt(project: ProjectModel, points: List<SurveyPointModel>): String {
        val geom = points.map { Point2d(it.name, it.easting, it.northing) }
        return buildString {
            appendLine("Datum Point")
            appendLine("Projeto: ${project.name}")
            appendLine("CRS: ${project.crs}")
            appendLine("Finalidade: reconhecimento GNSS com smartphone")
            appendLine()
            appendLine("Pontos")
            points.forEach {
                appendLine("${it.name}  E=${"%.3f".format(Locale.US, it.easting)}  N=${"%.3f".format(Locale.US, it.northing)}  estabilidade=${it.qualityClass}")
            }
            appendLine()
            appendLine("Perimetro: ${"%.3f".format(Locale.US, Geometry.perimeter(geom))} m")
            appendLine("Area: ${"%.3f".format(Locale.US, Geometry.area(geom))} m2")
            appendLine()
            appendLine("Levantamento GNSS de reconhecimento realizado com smartphone. Este relatório não substitui levantamento topográfico/geodésico executado com equipamento e metodologia apropriados.")
        }
    }

    fun geoJson(project: ProjectModel, points: List<SurveyPointModel>): String {
        val pointFeatures = points.joinToString(",\n") { point ->
            """{"type":"Feature","properties":{"project":${project.name.json()},"name":${point.name.json()},"easting":${point.easting},"northing":${point.northing},"quality":${point.qualityClass.name.json()}},"geometry":{"type":"Point","coordinates":[${point.longitude},${point.latitude}]}}"""
        }
        val line = if (points.size >= 2) {
            val coords = points.joinToString(",") { "[${it.longitude},${it.latitude}]" }
            """,
{"type":"Feature","properties":{"project":${project.name.json()},"kind":"line"},"geometry":{"type":"LineString","coordinates":[$coords]}}"""
        } else {
            ""
        }
        val polygon = if (points.size >= 3) {
            val coords = (points + points.first()).joinToString(",") { "[${it.longitude},${it.latitude}]" }
            """,
{"type":"Feature","properties":{"project":${project.name.json()},"kind":"polygon"},"geometry":{"type":"Polygon","coordinates":[[$coords]]}}"""
        } else {
            ""
        }
        return """{"type":"FeatureCollection","features":[$pointFeatures$line$polygon]}"""
    }

    fun kml(project: ProjectModel, points: List<SurveyPointModel>): String {
        val placemarks = points.joinToString("\n") {
            """
            <Placemark>
              <name>${it.name.xml()}</name>
              <description>Estabilidade: ${it.qualityClass}</description>
              <Point><coordinates>${it.longitude},${it.latitude},${it.altitude ?: 0.0}</coordinates></Point>
            </Placemark>
            """.trimIndent()
        }
        val line = if (points.size >= 2) {
            val coordinates = points.joinToString(" ") { "${it.longitude},${it.latitude},0" }
            """
            <Placemark>
              <name>${project.name.xml()} - linha</name>
              <LineString><coordinates>$coordinates</coordinates></LineString>
            </Placemark>
            """.trimIndent()
        } else {
            ""
        }
        return """<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>
<name>${project.name.xml()}</name>
$placemarks
$line
</Document>
</kml>
"""
    }

    fun dxf(points: List<SurveyPointModel>): String = buildString {
        appendLine("0")
        appendLine("SECTION")
        appendLine("2")
        appendLine("ENTITIES")
        points.forEach {
            appendLine("0")
            appendLine("POINT")
            appendLine("8")
            appendLine("DATUM_POINTS")
            appendLine("10")
            appendLine("%.3f".format(Locale.US, it.easting))
            appendLine("20")
            appendLine("%.3f".format(Locale.US, it.northing))
            appendLine("0")
            appendLine("TEXT")
            appendLine("8")
            appendLine("DATUM_LABELS")
            appendLine("10")
            appendLine("%.3f".format(Locale.US, it.easting))
            appendLine("20")
            appendLine("%.3f".format(Locale.US, it.northing))
            appendLine("40")
            appendLine("1.5")
            appendLine("1")
            appendLine(it.name)
        }
        if (points.size >= 2) {
            appendLine("0")
            appendLine("LWPOLYLINE")
            appendLine("8")
            appendLine("DATUM_PERIMETER")
            appendLine("90")
            appendLine(points.size)
            appendLine("70")
            appendLine(if (points.size >= 3) "1" else "0")
            points.forEach {
                appendLine("10")
                appendLine("%.3f".format(Locale.US, it.easting))
                appendLine("20")
                appendLine("%.3f".format(Locale.US, it.northing))
            }
        }
        appendLine("0")
        appendLine("ENDSEC")
        appendLine("0")
        appendLine("EOF")
    }

    private fun String.csvEscape(): String =
        if (any { it == ',' || it == '"' || it == '\n' }) "\"${replace("\"", "\"\"")}\"" else this

    private fun String.json(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    private fun String.xml(): String = replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}
