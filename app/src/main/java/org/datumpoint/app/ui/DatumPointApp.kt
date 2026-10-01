package org.datumpoint.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.datumpoint.app.R
import org.datumpoint.app.core.export.Exporters
import org.datumpoint.app.core.geometry.Geometry
import org.datumpoint.app.core.geometry.Point2d
import org.datumpoint.app.core.gnss.FakeGnssRepository
import org.datumpoint.app.core.model.ProjectModel
import org.datumpoint.app.core.model.QualityClass
import org.datumpoint.app.core.model.SurveyPointModel
import org.datumpoint.app.core.processing.ObservationProcessor
import java.util.Locale

private enum class Screen { PROJECT, CAPTURE, GEOMETRY, EXPORT, ABOUT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatumPointApp() {
    val project = remember { ProjectModel(id = 1, name = "Teste", description = "Projeto local de reconhecimento") }
    val points = remember { mutableStateListOf<SurveyPointModel>() }
    var screen by remember { mutableStateOf(Screen.PROJECT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Datum Point", fontWeight = FontWeight.Bold)
                        Text("Reconhecimento GNSS offline", style = MaterialTheme.typography.labelMedium)
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = Screen.entries.indexOf(screen)) {
                Screen.entries.forEach { item ->
                    Tab(
                        selected = screen == item,
                        onClick = { screen = item },
                        text = { Text(item.label()) },
                    )
                }
            }
            when (screen) {
                Screen.PROJECT -> ProjectScreen(project, points, onCapture = { screen = Screen.CAPTURE })
                Screen.CAPTURE -> CaptureScreen(
                    nextName = "P%02d".format(points.size + 1),
                    onSave = { processed ->
                        points += SurveyPointModel(
                            id = points.size + 1L,
                            projectId = project.id,
                            name = "P%02d".format(points.size + 1),
                            sequence = points.size + 1,
                            latitude = processed.latitude,
                            longitude = processed.longitude,
                            easting = processed.easting,
                            northing = processed.northing,
                            altitude = processed.altitude,
                            qualityClass = processed.qualityClass,
                            acceptedFixes = processed.acceptedFixes,
                            rejectedFixes = processed.rejectedFixes + processed.outlierFixes,
                            radialRms = processed.radialRms,
                            medianReportedAccuracy = processed.medianReportedAccuracy,
                            durationSeconds = processed.durationSeconds,
                        )
                        screen = Screen.PROJECT
                    },
                )
                Screen.GEOMETRY -> GeometryScreen(points)
                Screen.EXPORT -> ExportScreen(project, points)
                Screen.ABOUT -> AboutScreen()
            }
        }
    }
}

@Composable
private fun ProjectScreen(
    project: ProjectModel,
    points: List<SurveyPointModel>,
    onCapture: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SummaryCard(project, points)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onCapture, modifier = Modifier.fillMaxWidth()) {
                Text("OCUPAR NOVO PONTO")
            }
        }
        if (points.isEmpty()) {
            item {
                Text(
                    "Nenhum ponto salvo ainda. Use a captura simulada para validar o fluxo; em aparelho real, a fonte GNSS usa LocationManager/GPS_PROVIDER.",
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        } else {
            items(points) { point -> PointCard(point) }
        }
    }
}

@Composable
private fun SummaryCard(project: ProjectModel, points: List<SurveyPointModel>) {
    val geom = points.map { Point2d(it.name, it.easting, it.northing) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(project.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(project.crs, color = MaterialTheme.colorScheme.secondary)
            Text("${points.size} ponto(s)")
            Text("Perimetro: ${Geometry.perimeter(geom).m()} m")
            Text("Area: ${Geometry.area(geom).m()} m2")
            Text(stringResource(R.string.decimal_disclaimer), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PointCard(point: SurveyPointModel) {
    Card {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(point.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(point.qualityClass.display(), color = point.qualityClass.color())
            }
            Text("E: ${point.easting.m()}  N: ${point.northing.m()}")
            Text("${point.durationSeconds} s • ${point.acceptedFixes} amostras • RMS ${point.radialRms.m()} m")
        }
    }
}

@Composable
private fun CaptureScreen(
    nextName: String,
    onSave: (org.datumpoint.app.core.model.ProcessedOccupation) -> Unit,
) {
    var processed by remember { mutableStateOf<org.datumpoint.app.core.model.ProcessedOccupation?>(null) }
    val samples = remember { FakeGnssRepository.stableOccupation() }
    val processor = remember { ObservationProcessor(maxAccuracyMeters = 10.0) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("PONTO $nextName", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("GNSS: PRONTO PARA OCUPACAO", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(progress = { if (processed == null) 0.0f else 1.0f }, modifier = Modifier.fillMaxWidth())
        Text("Fluxo: Aguardando GNSS -> Estabilizando -> Observando -> Processando -> Resultado")
        Text("Fonte principal prevista: GNSS real do Android via LocationManager/GPS_PROVIDER.")
        Text(stringResource(R.string.stability_disclaimer), style = MaterialTheme.typography.bodySmall)

        if (processed == null) {
            Button(
                onClick = { processed = processor.process(samples, durationSeconds = 60) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("SIMULAR OCUPACAO DE 60 S")
            }
        } else {
            val result = processed!!
            ResultCard(result)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { processed = processor.process(FakeGnssRepository.stableOccupation(seed = samples.size + 7), 60) }) {
                    Text("Repetir")
                }
                Button(onClick = { onSave(result) }) {
                    Text("Salvar")
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: org.datumpoint.app.core.model.ProcessedOccupation) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Resultado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Estabilidade: ${result.qualityClass.display()}", color = result.qualityClass.color())
            Text("Latitude: ${"%.8f".format(Locale.US, result.latitude)}")
            Text("Longitude: ${"%.8f".format(Locale.US, result.longitude)}")
            Text("E: ${result.easting.m()}  N: ${result.northing.m()}")
            Text("Fixes: ${result.acceptedFixes} aceitos, ${result.rejectedFixes + result.outlierFixes} rejeitados")
            Text("Precisao Android mediana: ${result.medianReportedAccuracy.m()} m")
            Text("RMS radial: ${result.radialRms.m()} m")
            Text("Satelites usados: ${result.satellitesUsed ?: "-"}")
        }
    }
}

@Composable
private fun GeometryScreen(points: List<SurveyPointModel>) {
    val geom = points.map { Point2d(it.name, it.easting, it.northing) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Croqui", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Sketch(points)
        Text("Perimetro: ${Geometry.perimeter(geom).m()} m")
        Text("Area: ${Geometry.area(geom).m()} m2")
        HorizontalDivider()
        geom.zipWithNext().forEach { (a, b) ->
            Text("${a.name} -> ${b.name}: ${Geometry.distance(a, b).m()} m • Az ${Geometry.azimuthDegrees(a, b).m()}°")
        }
        if (points.size < 2) Text("Cadastre ao menos dois pontos para calcular lados e azimutes.")
    }
}

@Composable
private fun Sketch(points: List<SurveyPointModel>) {
    val height = (LocalConfiguration.current.screenWidthDp * 0.75f).dp
    val line = MaterialTheme.colorScheme.primary
    val dot = MaterialTheme.colorScheme.tertiary
    Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth().height(height)) {
        Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                if (points.isEmpty()) return@Canvas
                val minE = points.minOf { it.easting }
                val maxE = points.maxOf { it.easting }
                val minN = points.minOf { it.northing }
                val maxN = points.maxOf { it.northing }
                val spanE = (maxE - minE).coerceAtLeast(1.0)
                val spanN = (maxN - minN).coerceAtLeast(1.0)
                fun map(point: SurveyPointModel): Offset {
                    val x = ((point.easting - minE) / spanE).toFloat() * (size.width * 0.8f) + size.width * 0.1f
                    val y = size.height - (((point.northing - minN) / spanN).toFloat() * (size.height * 0.8f) + size.height * 0.1f)
                    return Offset(x, y)
                }
                val path = Path()
                points.forEachIndexed { index, point ->
                    val p = map(point)
                    if (index == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                }
                if (points.size >= 3) path.close()
                drawPath(path, color = line, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                points.forEach { drawCircle(dot, radius = 8f, center = map(it)) }
            }
        }
    }
}

@Composable
private fun ExportScreen(project: ProjectModel, points: List<SurveyPointModel>) {
    var preview by remember { mutableStateOf("Escolha um formato para gerar uma previa UTF-8.") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Exportar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { preview = Exporters.csv(project, points) }) { Text("CSV") }
            Button(onClick = { preview = Exporters.kml(project, points) }) { Text("KML") }
            Button(onClick = { preview = Exporters.dxf(points) }) { Text("DXF") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { preview = Exporters.geoJson(project, points) }) { Text("GeoJSON") }
            OutlinedButton(onClick = { preview = Exporters.txt(project, points) }) { Text("TXT") }
        }
        Card {
            Text(
                preview.take(2500),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AboutScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Sobre", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Datum Point v0.1.0")
        Text("Licenca MIT. Offline-first, sem login, sem analytics e sem backend.")
        Text("O que este aplicativo nao e", fontWeight = FontWeight.Bold)
        Text("Nao substitui RTK, PPK, receptor geodesico, estacao total ou levantamento oficial.")
        Text("A media de posicoes ajuda a avaliar estabilidade, mas nao transforma smartphone em equipamento centimetrico.")
        Text("Medições GNSS brutas sao usadas como diagnostico quando o dispositivo oferece suporte.")
    }
}

private fun Screen.label(): String = when (this) {
    Screen.PROJECT -> "Projeto"
    Screen.CAPTURE -> "Captura"
    Screen.GEOMETRY -> "Croqui"
    Screen.EXPORT -> "Exportar"
    Screen.ABOUT -> "Sobre"
}

private fun Double.m(): String = "%.3f".format(Locale.US, this)

private fun QualityClass.display(): String = when (this) {
    QualityClass.STABLE -> "Estavel"
    QualityClass.MODERATE -> "Moderada"
    QualityClass.WEAK -> "Fraca"
}

@Composable
private fun QualityClass.color(): Color = when (this) {
    QualityClass.STABLE -> Color(0xFF0E7A4F)
    QualityClass.MODERATE -> MaterialTheme.colorScheme.tertiary
    QualityClass.WEAK -> MaterialTheme.colorScheme.error
}
