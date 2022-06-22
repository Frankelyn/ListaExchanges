package com.ejemplo.listaexchanges

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejemplo.listaexchanges.ui.theme.ListaExchangesTheme
import com.ejemplo.listaexchanges.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import retrofit2.HttpException
import retrofit2.http.GET
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListaExchangesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                   // Greeting("Android")
                    ListExchangesScreen()
                }
            }
        }
    }
}

@Composable
fun ListExchangesScreen(
    viewModel: ListExchangesViewmodel = hiltViewModel()
) {

    val state = viewModel.state.value

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            Text(
                text = "Nombre",
                style = MaterialTheme.typography.subtitle1
            )

            Text(
                text = "Estado",
                style = MaterialTheme.typography.subtitle1
            )


            Text(
                text = "Actualizado:",
                style = MaterialTheme.typography.subtitle1
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize()){
            items( state.exchanges){ exchange ->
                ListExchangesItem(exchange = exchange, {})
            }
        }

        if (state.isLoading)
            CircularProgressIndicator()

    }

}

@Composable
fun ListExchangesItem(
    exchange:ListaExchangesDto,
    onClick : (ListaExchangesDto) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(exchange) }
            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${exchange.name}",
                style = MaterialTheme.typography.caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(100.dp)
            )

            Text(
                text = if(exchange.active) "Activa" else "Inactiva",
                color = if(exchange.active) Color.Green else Color.Red ,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.align(CenterVertically)
            )

            Text(
                text = "${exchange.last_updated}",
                style = MaterialTheme.typography.caption,
                overflow = TextOverflow.Ellipsis
            )

        }
    }

}

//RUTA: data/remote/dto
data class ListaExchangesDto(
    val id: String = "",
    val name: String = "",
    val active: Boolean = false,
    val last_updated: String = ""
)

//RUTA: data/remote
interface ListaExchangesApi {
    @GET("/v1/exchanges")
    suspend fun getListExchanges(): List<ListaExchangesDto>
}

class ListExchangesRepository @Inject constructor(
    private val api: ListaExchangesApi
) {
    fun getListExchanges(): Flow<Resource<List<ListaExchangesDto>>> = flow {
        try {
            emit(Resource.Loading()) //indicar que estamos cargando

            val exchanges = api.getListExchanges() //descarga las monedas de internet, se supone quedemora algo

            emit(Resource.Success(exchanges)) //indicar que se cargo correctamente y pasarle las monedas
        } catch (e: HttpException) {
            //error general HTTP
            emit(Resource.Error(e.message ?: "Error HTTP GENERAL"))
        } catch (e: IOException) {
            //debe verificar tu conexion a internet
            emit(Resource.Error(e.message ?: "verificar tu conexion a internet"))
        }
    }
}

data class ListExchangesState(
    val isLoading: Boolean = false,
    val exchanges: List<ListaExchangesDto> = emptyList(),
    val error: String = ""
)

@HiltViewModel
class ListExchangesViewmodel @Inject constructor(
    private val listExchangesRepository: ListExchangesRepository
) : ViewModel() {

    private var _state = mutableStateOf(ListExchangesState())
    val state: State<ListExchangesState> = _state

    init {
        listExchangesRepository.getListExchanges().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = ListExchangesState(isLoading = true)
                }

                is Resource.Success -> {
                    _state.value = ListExchangesState(exchanges = result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = ListExchangesState(error = result.message ?: "Error desconocido")
                }
            }
        }.launchIn(viewModelScope)
    }
}