package com.team12.ElSpar.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team12.ElSpar.ui.theme.ElSparTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.team12.ElSpar.R
import com.team12.ElSpar.model.PriceArea
import com.team12.ElSpar.model.PricePeriod
import com.team12.ElSpar.ui.chart.PriceChart
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElSparScreen(
    priceList: Map<LocalDateTime, Double>,
    currentPricePeriod: PricePeriod,
    onChangePricePeriod: (PricePeriod) -> Unit,
    onUpdatePriceArea: (PriceArea) -> Unit,
    modifier: Modifier = Modifier
) {

    var selectedPriceArea by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    var placeHolderPadding by remember { mutableStateOf(0) }
    val maxPlaceHolderPadding = 7
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Column(){
                    //dropdown-meny
                    val list: List<String> = stringArrayResource(id = R.array.districtList).asList()
                    var textFiledSize by remember { mutableStateOf(Size.Zero) }

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedPriceArea,
                            enabled = false,
                            onValueChange = {
                                selectedPriceArea = it
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    textFiledSize = coordinates.size.toSize()
                                },
                            label = {Text(
                                text = "Velg prisområde",
                                modifier = Modifier.padding(top = placeHolderPadding.dp)
                            )},

                            colors = TextFieldDefaults.outlinedTextFieldColors (
                                focusedBorderColor =  MaterialTheme.colorScheme.primaryContainer, //hide the indicator
                                unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                                disabledBorderColor = MaterialTheme.colorScheme.primaryContainer,
                                disabledTextColor = Color.Black
                            )
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .width(with(LocalDensity.current){textFiledSize.width.toDp()})
                        ) {
                            list.forEach {label ->
                                DropdownMenuItem(text = {Text(text = label)}, onClick = {
                                    selectedPriceArea = label
                                    expanded = false
                                    placeHolderPadding = maxPlaceHolderPadding
                                    when(selectedPriceArea){
                                        "NO1 – Østlandet" -> onUpdatePriceArea(PriceArea.NO1)
                                        "NO2 – Sørlandet" -> onUpdatePriceArea(PriceArea.NO2)
                                        "NO3 – Midt-Norge" -> onUpdatePriceArea(PriceArea.NO3)
                                        "NO4 – Nord-Norge" -> onUpdatePriceArea(PriceArea.NO4)
                                        "NO5 – Vestlandet" -> onUpdatePriceArea(PriceArea.NO5)
                                    }
                                })
                            }
                        }
                    }
                }},
                modifier = Modifier,
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                actions = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Velg prisområde"
                        )
                    }
                },
            )
        }/*,
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ){
                    Icon(imageVector = Icons.Default.List, contentDescription = "Person Icon")
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Person Icon")
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Person Icon")

                }
            }
        }*/
    ) { padding ->
        //HER GÅR DET SOM ER I "MIDTED" AV SCAFFOLDET
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 10.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            //Plass mellom hvert element
            verticalArrangement = Arrangement.spacedBy(10.dp)

        ) {

            //Dette er rekken med knapper på toppen.
            CreateTimeIntervalButtons(currentPricePeriod, padding) { onChangePricePeriod(it) }

            //Dette er kortet på toppen.
            ScaffoldContent(
                padding,

                avgPrice = priceList.values.average(),
                maxPrice = priceList.values.max(),
                minPrice = priceList.values.min())

            //Kan ha grafen her
            PriceChart(priceList, currentPricePeriod)

        }
    }

    /*
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(stringResource(R.string.avg_price, avgPrice))
        Text(stringResource(R.string.max_price, maxPrice))
        Text(stringResource(R.string.min_price, minPrice))
    }*/
}
@Composable
fun CreateTimeIntervalButtons(
    currentPricePeriod: PricePeriod,
    topPaddingValues: PaddingValues,
    onSelectPricePeriod: (PricePeriod) -> Unit
){
    Row(modifier = Modifier.padding(top = topPaddingValues.calculateTopPadding()).height(40.dp)){
        SwitchButton(40, 0, currentPricePeriod, PricePeriod.DAY) { onSelectPricePeriod(it) }
        SwitchButton(0, 0, currentPricePeriod, PricePeriod.WEEK) { onSelectPricePeriod(it) }
        SwitchButton(0, 40, currentPricePeriod, PricePeriod.MONTH) { onSelectPricePeriod(it) }
    }
}
@Composable
fun SwitchButton(
    leftRound:Int = 0,
    rightRound:Int = 0,
    currentPricePeriod: PricePeriod,
    btnPricePeriod: PricePeriod,
    onSelectPricePeriod: (PricePeriod) -> Unit)
    {
    val unselectedColor = MaterialTheme.colorScheme.background;
    val selectedColor = MaterialTheme.colorScheme.primaryContainer;
    OutlinedButton(
        onClick = {
            onSelectPricePeriod(btnPricePeriod)
        },
        modifier = Modifier.width(120.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor =  (
                    if (currentPricePeriod == btnPricePeriod) selectedColor else unselectedColor
                    )
            ),
        shape = RoundedCornerShape(
            topStartPercent = leftRound,
            topEndPercent = rightRound,
            bottomEndPercent = rightRound,
            bottomStartPercent = leftRound
        )
        //shape = CutCornerShape(1.dp)
    ) {
        Text(text = btnPricePeriod.text, color = Color.Black)
    }
}
@Composable
fun CardContent(topText:String, midText:String){
    val defModifier = Modifier
        .padding(2.dp)
        .fillMaxSize()
    Column(
        modifier = defModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = topText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Justify
        )
        Row(
            verticalAlignment = Alignment.Bottom
        ){
            Text(
                text = midText,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Justify
            )
            Text(
                text = "øre/kWh",
                fontSize = 16.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }

    }
}
@Composable
fun ScaffoldContent(
    padding: PaddingValues,
    avgPrice: Double,
    maxPrice: Double,
    minPrice: Double,
    modifier: Modifier = Modifier){
val defModifier = Modifier
    .padding(2.dp)
    .fillMaxSize()
    Column(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            //.padding(top = padding.calculateTopPadding()),
            .height(230.dp),
        horizontalAlignment = Alignment.CenterHorizontally


    ) {
        //Kortet på toppen som holder info om nåværende, min og maks
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
        ){
            //C1, nåværende
            Box(
                modifier = defModifier.weight(0.6f)
            ) {

                Column(
                    modifier = defModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Information icon"
                        )
                        Text(text = "Dagens strømpris",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Gjennomsnitt",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Justify
                    )
                    Text(
                        text = roundOffDecimal(avgPrice).toString(),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Justify
                    )
                    Text(
                        text = "øre/kWh",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Justify
                    )
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .width(1.dp),
                        color = Color.Gray
                    )
                }

            }

            //c2 og c4, laveste og høyeste
            Row(
                modifier = defModifier.weight(0.4f)
            ) {
                Box(
                    modifier = defModifier.weight(0.5f)
                ) {
                    CardContent("Laveste - 12:00", roundOffDecimal(minPrice).toString()) //Endre, skal være variabel
                }
                //vertical Divider
                Divider(
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxHeight(0.9f)
                        .width(1.dp)
                )
                Box(
                    modifier = defModifier.weight(0.5f)
                ) {
                    CardContent("Høyeste - 16:00", roundOffDecimal(maxPrice).toString()) //Endre, skal være variabel

                }

            }

        }
        }

}
fun roundOffDecimal(number: Double): Double? {
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.CEILING
    return df.format(number).toDouble()
}
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    fun getPowerPricesByDate(
        date: LocalDateTime,
        area: PriceArea
    ): Map<LocalDateTime, Double>
    {
        return  mapOf<LocalDateTime, Double>(
            LocalDateTime.of(2023, 1, 30, 0, 0) to  10.0,
            LocalDateTime.of(2023, 1, 30, 1, 0) to  10.0,
        )
    }

    //Test data
    fun updatePricePeriod(pricePeriod: PricePeriod) {
    }
    fun updatePriceArea(v: PriceArea) {
    }

    ElSparTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ElSparScreen(
                priceList = getPowerPricesByDate(LocalDateTime.of(1,1,1,1,1), PriceArea.NO1),
                currentPricePeriod = PricePeriod.DAY,
                onChangePricePeriod = { updatePricePeriod(it) },
                onUpdatePriceArea = {updatePriceArea(it)}
            )
        }
    }
}

