package com.example.judio_premium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.judio_premium.data.Gasto
import com.example.judio_premium.data.GastoDatabase
import com.example.judio_premium.ui.theme.Judio_premiumTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Judio_premiumTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("ingresar") { IngresarGastoScreen(navController) }
        composable("grafico") { GraficoScreen(navController) }
        composable("historial") { HistorialScreen(navController) }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_app),
            contentDescription = "Logo App",
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "JUDIO PREMIUM",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = { navController.navigate("ingresar") }, modifier = Modifier.fillMaxWidth()) {
            Text("Ingresar gasto")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { navController.navigate("grafico") }, modifier = Modifier.fillMaxWidth()) {
            Text("Ver gráfico")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { navController.navigate("historial") }, 
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Historial / Eliminar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresarGastoScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { GastoDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var monto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val categorias = listOf("Comida", "Ocio", "Transporte", "Ropa", "Otros")
    var categoriaSeleccionada by remember { mutableStateOf("-") }
    var expandedCat by remember { mutableStateOf(false) }

    val personas = listOf("Yo", "Lucila", "Emma", "Anastassia", "Sofia", "Rocio", "Mama", "Papa")
    var personaSeleccionada by remember { mutableStateOf("-") }
    var expandedPer by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text("Fecha: $fechaHoy", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = monto, 
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' }) {
                    monto = input
                }
            }, 
            label = { Text("Monto") }, 
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(expanded = expandedCat, onExpandedChange = { expandedCat = !expandedCat }) {
            OutlinedTextField(value = categoriaSeleccionada, onValueChange = {}, readOnly = true, label = { Text("Categoría") }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth())
            ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                categorias.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat) }, onClick = { categoriaSeleccionada = cat; expandedCat = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = expandedPer, onExpandedChange = { expandedPer = !expandedPer }) {
            OutlinedTextField(value = personaSeleccionada, onValueChange = {}, readOnly = true, label = { Text("Persona") }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth())
            ExposedDropdownMenu(expanded = expandedPer, onDismissRequest = { expandedPer = false }) {
                personas.forEach { per ->
                    DropdownMenuItem(text = { Text(per) }, onClick = { personaSeleccionada = per; expandedPer = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val m = monto.toDoubleOrNull() ?: 0.0
                val catValida = categoriaSeleccionada != "-"
                val perValida = personaSeleccionada != "-"
                
                if (m > 0 && catValida && perValida) {
                    scope.launch {
                        db.gastoDao().insertar(Gasto(monto = m, categoria = categoriaSeleccionada, descripcion = descripcion, persona = personaSeleccionada, fecha = fechaHoy))
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = monto.isNotEmpty() && categoriaSeleccionada != "-" && personaSeleccionada != "-"
        ) {
            Text("Guardar gasto")
        }
    }
}

@Composable
fun HistorialScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { GastoDatabase.getDatabase(context) }
    val gastos by db.gastoDao().obtenerTodos().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial de Gastos", style = MaterialTheme.typography.headlineMedium)
        Text("Toca el icono para eliminar", style = MaterialTheme.typography.bodySmall)
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(gastos) { gasto ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("$${gasto.monto}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${gasto.categoria} - ${gasto.persona}", style = MaterialTheme.typography.bodyMedium)
                            if (gasto.descripcion.isNotEmpty()) {
                                Text(gasto.descripcion, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Text(gasto.fecha, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                db.gastoDao().eliminar(gasto)
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraficoScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { GastoDatabase.getDatabase(context) }
    val gastosRaw by db.gastoDao().obtenerTodos().collectAsState(initial = emptyList())
    
    var agruparPorCategoria by remember { mutableStateOf(true) }
    
    val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val anios = (2023..Calendar.getInstance().get(Calendar.YEAR)).map { it.toString() }.reversed()

    var mesSeleccionadoIdx by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var anioSeleccionado by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    
    var expandedMes by remember { mutableStateOf(false) }
    var expandedAnio by remember { mutableStateOf(false) }

    val datosFiltrados = remember(gastosRaw, mesSeleccionadoIdx, anioSeleccionado) {
        val prefijo = String.format("%s-%02d", anioSeleccionado, mesSeleccionadoIdx + 1)
        gastosRaw.filter { it.fecha.startsWith(prefijo) }
    }

    val datosAgrupados = remember(datosFiltrados, agruparPorCategoria) {
        if (agruparPorCategoria) {
            datosFiltrados.groupBy { it.categoria }.mapValues { it.value.sumOf { g -> g.monto } }
        } else {
            datosFiltrados.groupBy { it.persona }.mapValues { it.value.sumOf { g -> g.monto } }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Reporte de Gastos", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Selector de Año
            ExposedDropdownMenuBox(
                expanded = expandedAnio, 
                onExpandedChange = { expandedAnio = !expandedAnio },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = anioSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Año") },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedAnio, onDismissRequest = { expandedAnio = false }) {
                    anios.forEach { anio ->
                        DropdownMenuItem(text = { Text(anio) }, onClick = { anioSeleccionado = anio; expandedAnio = false })
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Selector de Mes
            ExposedDropdownMenuBox(
                expanded = expandedMes, 
                onExpandedChange = { expandedMes = !expandedMes },
                modifier = Modifier.weight(1.5f)
            ) {
                OutlinedTextField(
                    value = meses[mesSeleccionadoIdx],
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mes") },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedMes, onDismissRequest = { expandedMes = false }) {
                    meses.forEachIndexed { index, mes ->
                        DropdownMenuItem(text = { Text(mes) }, onClick = { mesSeleccionadoIdx = index; expandedMes = false })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Persona")
            Spacer(modifier = Modifier.width(12.dp))
            Switch(checked = agruparPorCategoria, onCheckedChange = { agruparPorCategoria = it })
            Spacer(modifier = Modifier.width(12.dp))
            Text("Categoría")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (datosAgrupados.isNotEmpty()) {
            PieChart(datosAgrupados)
        } else {
            Text("No hay gastos en ${meses[mesSeleccionadoIdx]} $anioSeleccionado", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}

@Composable
fun PieChart(datos: Map<String, Double>) {
    val total = datos.values.sum()
    val colores = listOf(Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784), Color(0xFFFFD54F), Color(0xFFBA68C8), Color(0xFF4DB6AC), Color(0xFFFF8A65))
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(200.dp)) {
            var startAngle = 0f
            datos.values.forEachIndexed { index, valor ->
                val sweepAngle = (valor / total * 360).toFloat()
                drawArc(
                    color = colores[index % colores.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                startAngle += sweepAngle
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
            items(datos.keys.toList()) { key ->
                Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(colores[datos.keys.toList().indexOf(key) % colores.size]))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$key: $${String.format("%.2f", datos[key])}")
                }
            }
        }
    }
}
