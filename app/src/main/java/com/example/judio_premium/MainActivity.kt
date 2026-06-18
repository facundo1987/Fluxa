package com.example.judio_premium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.judio_premium.data.*
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
            val context = LocalContext.current
            val onboardingManager = remember { OnboardingManager(context) }
            val showOnboarding = remember { mutableStateOf(!onboardingManager.isOnboardingCompleted()) }

            Judio_premiumTheme {
                if (showOnboarding.value) {
                    OnboardingScreen(onFinished = {
                        onboardingManager.setOnboardingCompleted()
                        showOnboarding.value = false
                    })
                } else {
                    AppNavigation()
                }
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
        composable("configuracion") { ConfiguracionScreen(navController) }
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { GastoDatabase.getDatabase(context) }
    
    val categorias by db.gastoDao().obtenerCategorias().collectAsState(initial = emptyList())
    val personas by db.gastoDao().obtenerPersonas().collectAsState(initial = emptyList())
    
    var showValidationDialog by remember { mutableStateOf<ValidationState?>(null) }

    if (showValidationDialog != null) {
        AlertDialog(
            onDismissRequest = { showValidationDialog = null },
            title = { Text("Falta información") },
            text = { Text(showValidationDialog!!.message) },
            confirmButton = {
                Button(onClick = {
                    showValidationDialog = null
                    navController.navigate("configuracion")
                }) {
                    Text(showValidationDialog!!.buttonText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showValidationDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).systemBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo_app),
            contentDescription = "Logo Fluxa",
            modifier = Modifier.size(120.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Fluxa",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        MainButton(
            text = "Ingresar Gasto",
            icon = Icons.Default.Add,
            onClick = {
                val validation = validateBeforeAction(categorias, personas)
                if (validation == null) {
                    navController.navigate("ingresar")
                } else {
                    showValidationDialog = validation
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MainButton(
            text = "Reportes",
            icon = Icons.Default.Assessment,
            onClick = {
                val validation = validateBeforeAction(categorias, personas)
                if (validation == null) {
                    navController.navigate("grafico")
                } else {
                    showValidationDialog = validation
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MainButton(
            text = "Historial",
            icon = Icons.AutoMirrored.Filled.List,
            onClick = {
                val validation = validateBeforeAction(categorias, personas)
                if (validation == null) {
                    navController.navigate("historial")
                } else {
                    showValidationDialog = validation
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { navController.navigate("configuracion") }, 
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Personalizar", fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "by Facundo Bustamante",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

data class ValidationState(val message: String, val buttonText: String)

fun validateBeforeAction(categorias: List<Categoria>, personas: List<Persona>): ValidationState? {
    return when {
        categorias.isEmpty() -> ValidationState(
            message = "Primero necesitás crear al menos una categoría",
            buttonText = "Ir a crear"
        )
        personas.isEmpty() -> ValidationState(
            message = "Primero necesitás agregar una persona",
            buttonText = "Ir a agregar"
        )
        else -> null
    }
}

@Composable
fun MainButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresarGastoScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { GastoDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val categorias by db.gastoDao().obtenerCategorias().collectAsState(initial = emptyList())
    val personas by db.gastoDao().obtenerPersonas().collectAsState(initial = emptyList())

    var monto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val fechaDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    var categoriaSeleccionada by remember { mutableStateOf("-") }
    var expandedCat by remember { mutableStateOf(false) }

    var personaSeleccionada by remember { mutableStateOf("-") }
    var expandedPer by remember { mutableStateOf(false) }

    fun guardarGasto() {
        val m = monto.toDoubleOrNull() ?: 0.0
        if (m > 0 && categoriaSeleccionada != "-" && personaSeleccionada != "-") {
            scope.launch {
                db.gastoDao().insertar(Gasto(monto = m, categoria = categoriaSeleccionada, descripcion = descripcion, persona = personaSeleccionada, fecha = fechaDb))
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Gasto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            InfoCard(label = "Fecha de hoy", value = fechaHoy)
            
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = monto, 
                onValueChange = { input -> if (input.all { it.isDigit() || it == '.' }) monto = input }, 
                label = { Text("Monto ($)") }, 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusRequester.requestFocus() }
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                prefix = { Text("$ ") },
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SelectorField(
                label = "Categoría",
                selected = categoriaSeleccionada,
                expanded = expandedCat,
                onExpandedChange = { expandedCat = it },
                items = categorias.map { it.nombre },
                onSelect = { categoriaSeleccionada = it; expandedCat = false }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = descripcion, 
                onValueChange = { descripcion = it }, 
                label = { Text("¿En qué gastaste? (opcional)") }, 
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        guardarGasto()
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SelectorField(
                label = "Persona",
                selected = personaSeleccionada,
                expanded = expandedPer,
                onExpandedChange = { expandedPer = it },
                items = personas.map { it.nombre },
                onSelect = { personaSeleccionada = it; expandedPer = false }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { guardarGasto() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = monto.isNotEmpty() && categoriaSeleccionada != "-" && personaSeleccionada != "-",
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Guardar Gasto", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorField(label: String, selected: String, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, items: List<String>, onSelect: (String) -> Unit) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = if(selected == "-") "Seleccionar $label" else selected, 
            onValueChange = {}, 
            readOnly = true, 
            label = { Text(label) }, 
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(item) }, onClick = { onSelect(item) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { GastoDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    val categorias by db.gastoDao().obtenerCategorias().collectAsState(initial = emptyList())
    val personas by db.gastoDao().obtenerPersonas().collectAsState(initial = emptyList())

    var nuevaCat by remember { mutableStateOf("") }
    var nuevaPer by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalizar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Categorías", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            
            if (categorias.isEmpty()) {
                EmptyStateSection(
                    message = "No tenés categorías todavía",
                    buttonText = "Crear categoría",
                    icon = Icons.Default.Category
                ) { }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nuevaCat, 
                    onValueChange = { nuevaCat = it }, 
                    placeholder = { Text("Ej: Comida, Ocio...") },
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (nuevaCat.isNotBlank()) {
                            scope.launch { db.gastoDao().insertarCategoria(Categoria(nombre = nuevaCat)); nuevaCat = "" }
                            focusManager.clearFocus()
                        }
                    })
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    if (nuevaCat.isNotBlank()) {
                        scope.launch { db.gastoDao().insertarCategoria(Categoria(nombre = nuevaCat)); nuevaCat = "" }
                        focusManager.clearFocus()
                    }
                }, colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)) {
                    Icon(Icons.Default.Add, null)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            categorias.forEach { item ->
                ListItem(
                    headlineContent = { Text(item.nombre) },
                    trailingContent = {
                        IconButton(onClick = { scope.launch { db.gastoDao().eliminarCategoria(item) } }) {
                            Icon(Icons.Default.DeleteOutline, "Eliminar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Personas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))

            if (personas.isEmpty()) {
                EmptyStateSection(
                    message = "No agregaste personas aún",
                    buttonText = "Agregar persona",
                    icon = Icons.Default.PersonAdd
                ) { }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nuevaPer, 
                    onValueChange = { nuevaPer = it }, 
                    placeholder = { Text("Ej: Yo, Juan...") },
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (nuevaPer.isNotBlank()) {
                            scope.launch { db.gastoDao().insertarPersona(Persona(nombre = nuevaPer)); nuevaPer = "" }
                            focusManager.clearFocus()
                        }
                    })
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    if (nuevaPer.isNotBlank()) {
                        scope.launch { db.gastoDao().insertarPersona(Persona(nombre = nuevaPer)); nuevaPer = "" }
                        focusManager.clearFocus()
                    }
                }, colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)) {
                    Icon(Icons.Default.Add, null)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            personas.forEach { item ->
                ListItem(
                    headlineContent = { Text(item.nombre) },
                    trailingContent = {
                        IconButton(onClick = { scope.launch { db.gastoDao().eliminarPersona(item) } }) {
                            Icon(Icons.Default.DeleteOutline, "Eliminar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun EmptyStateSection(message: String, buttonText: String, icon: ImageVector, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        // El botón en este contexto de configuración es informativo o para guiar al TextField de arriba
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { GastoDatabase.getDatabase(context) }
    val gastos by db.gastoDao().obtenerTodos().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    var gastoParaEliminar by remember { mutableStateOf<Gasto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (gastos.isEmpty()) {
            EmptyState(message = "Aún no has registrado gastos", icon = Icons.AutoMirrored.Filled.List)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                item {
                    val total = gastos.sumOf { it.monto }
                    TotalCard(total = total)
                }
                items(gastos) { gasto ->
                    ExpenseCard(gasto) { gastoParaEliminar = gasto }
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }

    if (gastoParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { gastoParaEliminar = null },
            title = { Text("¿Eliminar gasto?") },
            text = { Text("Esta acción borrará el registro de $${gastoParaEliminar?.monto} permanentemente.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { db.gastoDao().eliminar(gastoParaEliminar!!); gastoParaEliminar = null }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { gastoParaEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun TotalCard(total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("Gasto Total Acumulado", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            Text("$${String.format("%.2f", total)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun ExpenseCard(gasto: Gasto, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if(gasto.descripcion.isEmpty()) gasto.categoria else gasto.descripcion,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${gasto.categoria} • ${gasto.persona}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", gasto.monto)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
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

    val totalMes = datosFiltrados.sumOf { it.monto }

    val datosAgrupados = remember(datosFiltrados, agruparPorCategoria) {
        if (agruparPorCategoria) {
            datosFiltrados.groupBy { it.categoria }.mapValues { it.value.sumOf { g -> g.monto } }
        } else {
            datosFiltrados.groupBy { it.persona }.mapValues { it.value.sumOf { g -> g.monto } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Header con Total del Mes
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total del Mes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text("$${String.format("%.2f", totalMes)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { /* Exportar? */ }) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary) }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Row(Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(expanded = expandedAnio, onExpandedChange = { expandedAnio = it }, Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = anioSeleccionado, onValueChange = {}, readOnly = true, label = { Text("Año") },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAnio) },
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                            )
                            ExposedDropdownMenu(expanded = expandedAnio, onDismissRequest = { expandedAnio = false }) {
                                anios.forEach { a -> DropdownMenuItem(text = { Text(a) }, onClick = { anioSeleccionado = a; expandedAnio = false }) }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        ExposedDropdownMenuBox(expanded = expandedMes, onExpandedChange = { expandedMes = it }, Modifier.weight(1.2f)) {
                            OutlinedTextField(
                                value = meses[mesSeleccionadoIdx], onValueChange = {}, readOnly = true, label = { Text("Mes") },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMes) },
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                            )
                            ExposedDropdownMenu(expanded = expandedMes, onDismissRequest = { expandedMes = false }) {
                                meses.forEachIndexed { i, m -> DropdownMenuItem(text = { Text(m) }, onClick = { mesSeleccionadoIdx = i; expandedMes = false }) }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selector Pro (Segment Control)
            Surface(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    SegmentControlItem(text = "Personas", selected = !agruparPorCategoria, modifier = Modifier.weight(1f)) { agruparPorCategoria = false }
                    SegmentControlItem(text = "Categorías", selected = agruparPorCategoria, modifier = Modifier.weight(1f)) { agruparPorCategoria = true }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mostrando: ${if(agruparPorCategoria) "Categorías" else "Personas"}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (datosAgrupados.isNotEmpty()) {
                BarChartPro(datosAgrupados)
            } else {
                EmptyState("No hay gastos en este período", icon = Icons.Default.Assessment)
            }
        }
    }
}

@Composable
fun SegmentControlItem(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(22.dp)).clickable { onClick() },
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun EmptyState(message: String, icon: ImageVector) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 64.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        Spacer(Modifier.height(24.dp))
        Text(message, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), textAlign = TextAlign.Center)
    }
}

@Composable
fun BarChartPro(datos: Map<String, Double>) {
    val total = datos.values.sum()
    val maxVal = datos.values.maxOrNull() ?: 1.0
    val colores = listOf(Color(0xFF7C4DFF), Color(0xFFB388FF), Color(0xFF64B5F6), Color(0xFF81C784), Color(0xFFFFD54F), Color(0xFFBA68C8))
    
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        val keys = datos.keys.toList().sortedByDescending { datos[it] }
        items(keys) { key ->
            val valor = datos[key] ?: 0.0
            val porcentaje = if (total > 0) (valor / total) * 100 else 0.0
            val fraction = (valor / maxVal).toFloat().coerceIn(0.01f, 1f)
            val color = colores[keys.indexOf(key) % colores.size]
            
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(text = key, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text("$${String.format("%.2f", valor)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).height(14.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(modifier = Modifier.fillMaxWidth(fraction).fillMaxHeight().clip(CircleShape).background(color))
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "${String.format("%.1f", porcentaje)}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.width(45.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
