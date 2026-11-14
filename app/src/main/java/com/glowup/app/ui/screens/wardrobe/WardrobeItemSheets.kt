package com.glowup.app.ui.screens.wardrobe

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.glowup.app.R
import com.glowup.app.ui.components.ConsentSheet
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.data.Consent
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.PhotoVault
import com.glowup.app.ui.data.WardrobeItem
import com.glowup.app.ui.data.WardrobeStore
import com.glowup.app.ui.data.defaultFormality
import com.glowup.app.ui.data.garmentLabel
import com.glowup.app.ui.data.style.Garment
import com.glowup.app.ui.data.style.Slot
import com.glowup.app.ui.data.style.Swatch
import com.glowup.app.ui.design.AppButton
import com.glowup.app.ui.design.ButtonVariant
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.screens.fitness.ChoicePill
import com.glowup.app.ui.theme.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun ItemSheet(item: WardrobeItem, onClose: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    ReMakeUDialog(
        onDismissRequest = onClose,
        title = item.title,
        primaryText = if (today in item.worn) "Worn today ✓" else "Wore it today",
        primaryEnabled = today !in item.worn,
        onPrimary = {
            WardrobeStore.markWorn(listOf(item.id), today)
            onClose()
        },
        secondaryText = "Close",
        content = {
            ItemImage(item, Modifier.fillMaxWidth().aspectRatio(1.2f), 20.dp)
            Spacer(Modifier.height(Space.M))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SwatchDot(item.colour, 14.dp)
                Spacer(Modifier.width(Space.S))
                Text(
                    "${item.colour.name} · ${garmentLabel(item.garment)} · " + listOf("Casual", "Smart casual", "Formal")[item.formality - 1],
                    fontSize = TextSize.BodySmall,
                    color = TextSecondary
                )
            }
            Text(
                (
                    if (item.wearCount ==
                        0
                    ) {
                        stringResource(R.string.common_not_worn_yet)
                    } else {
                        "Worn ${item.wearCount}× · last on ${item.lastWorn!!.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))}"
                    }
                    ) +
                    (item.costPerWear?.let { " · %.0f per wear".format(it) } ?: ""),
                fontSize = TextSize.BodySmall,
                modifier = Modifier.padding(top = Space.Xs)
            )
            Text(
                stringResource(R.string.wardrobe_delete_item),
                color = Maroon,
                fontSize = TextSize.BodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = Space.M).clickable { confirmDelete = true }
            )
        }
    )
    if (confirmDelete) {
        ReMakeUDialog(
            onDismissRequest = { confirmDelete = false },
            title = "Delete ${item.title}?",
            message = "Its photo and wear history are removed from this phone.",
            primaryText = "Delete",
            primaryDestructive = true,
            onPrimary = {
                WardrobeStore.remove(item.id)
                confirmDelete = false
                onClose()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddItemFlow(onDone: () -> Unit) {
    val context = LocalContext.current
    var pendingSource by remember { mutableStateOf<String?>(null) }
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var slot by remember { mutableStateOf(Slot.TOP) }
    var garment by remember { mutableStateOf(Garment.TEE) }
    var colour by remember { mutableStateOf<Swatch?>(null) }
    var autoColour by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var formality by remember { mutableStateOf(defaultFormality(Garment.TEE)) }
    var price by remember { mutableStateOf("") }
    val cameraFile = remember { File(File(context.cacheDir, "captures").apply { mkdirs() }, "wardrobe_capture.jpg") }
    val cameraUri = remember { FileProvider.getUriForFile(context, "${context.packageName}.files", cameraFile) }

    fun use(uri: Uri) {
        PhotoTools.load(context, uri)?.let { bmp ->
            photo = bmp
            colour = PhotoTools.dominantSwatch(bmp)
            autoColour = true
        }
        cameraFile.delete()
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> if (ok) use(cameraUri) }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let(::use) }

    BackHandler(onBack = onDone)
    Column(Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        DetailHeader(stringResource(R.string.common_add_clothes), onDone)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Space.Gutter, Space.L, Space.Gutter, Space.L)) {
            val bmp = photo
            if (bmp != null) {
                Box {
                    Image(
                        bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.1f).clip(Shapes.Large)
                    )
                    Text(
                        stringResource(R.string.common_setup_change),
                        color = Color.White,
                        fontSize = TextSize.BodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(
                            Alignment.BottomEnd
                        ).padding(Space.M).clip(Shapes.Pill).background(Color.Black.copy(alpha = 0.55f)).clickable {
                            photo =
                                null
                        }.padding(Space.M, Space.S)
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                    PhotoButton(Icons.Filled.PhotoCamera, "Take photo", Modifier.weight(1f)) {
                        if (ConsentStore.has(Consent.WARDROBE_PHOTOS)) takePicture.launch(cameraUri) else pendingSource = "camera"
                    }
                    PhotoButton(Icons.Outlined.Collections, "From gallery", Modifier.weight(1f)) {
                        if (ConsentStore.has(Consent.WARDROBE_PHOTOS)) {
                            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            pendingSource =
                                "gallery"
                        }
                    }
                }
                Text(
                    stringResource(R.string.wardrobe_lay_it_flat_or_hang),
                    color = TextSecondary,
                    fontSize = TextSize.BodySmall,
                    lineHeight = LineHeight.Compact,
                    modifier = Modifier.padding(top = Space.S)
                )
            }

            Spacer(Modifier.height(Space.Xl))
            Text(stringResource(R.string.wardrobe_what_is_it), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(Space.S))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                items(Slot.entries) { s ->
                    ChoicePill(s.label, slot == s) {
                        slot = s
                        garment = WardrobeStore.garmentsBySlot[s]!!.first()
                        formality = defaultFormality(garment)
                    }
                }
            }
            Spacer(Modifier.height(Space.S))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalArrangement = Arrangement.spacedBy(Space.S)) {
                WardrobeStore.garmentsBySlot[slot]!!.forEach { g ->
                    ChoicePill(garmentLabel(g), garment == g) {
                        garment = g
                        formality = defaultFormality(g)
                    }
                }
            }

            Spacer(Modifier.height(Space.Xl))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.wardrobe_colour), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
                if (autoColour &&
                    colour != null
                ) {
                    Text(
                        stringResource(R.string.wardrobe_picked_from_your_photo),
                        color = Green,
                        fontSize = TextSize.Caption,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(Space.S))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.S), verticalArrangement = Arrangement.spacedBy(Space.S)) {
                WardrobeStore.swatches.forEach { s ->
                    Box(
                        Modifier.clickable {
                            colour = s
                            autoColour = false
                        }
                    ) { SwatchDot(s, 32.dp, colour == s) }
                }
            }
            colour?.let { Text(it.name, fontSize = TextSize.BodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = Space.S)) }

            Spacer(Modifier.height(Space.Xl))
            Text(stringResource(R.string.wardrobe_how_dressy), fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(Space.S))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
                listOf("Casual", "Smart", "Formal").forEachIndexed { i, l -> ChoicePill(l, formality == i + 1, Modifier.weight(1f)) { formality = i + 1 } }
            }

            Spacer(Modifier.height(Space.Xl))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(40) },
                singleLine = true,
                label = { Text(stringResource(R.string.wardrobe_name_optional)) },
                placeholder = { Text("${colour?.name ?: "Navy"} ${garmentLabel(garment).lowercase()}") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Space.S))
            OutlinedTextField(
                value = price,
                onValueChange = {
                    price = it.filter { c -> c.isDigit() || c == '.' }.take(8)
                },
                singleLine = true,
                label = { Text(stringResource(R.string.wardrobe_price_optional_for_cost_per)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ink, unfocusedBorderColor = TextMuted),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(Modifier.padding(Space.Gutter, Space.S, Space.Gutter, Space.Gutter)) {
            val ready = colour != null
            AppButton(
                if (ready) stringResource(R.string.wardrobe_add_to_closet) else stringResource(R.string.wardrobe_pick_a_colour),
                variant = ButtonVariant.Primary,
                enabled = ready
            ) {
                val saved = photo?.let { PhotoVault.save(PhotoVault.Album.WARDROBE, PhotoTools.jpeg(it), tag = "item") }
                WardrobeStore.add(garment, colour!!, title, formality, saved?.name, price.toDoubleOrNull())
                onDone()
            }
        }
    }
    pendingSource?.let { source ->
        ConsentSheet(
            Consent.WARDROBE_PHOTOS,
            Icons.Filled.PhotoCamera,
            onAllow = {
                pendingSource = null
                if (source ==
                    "camera"
                ) {
                    takePicture.launch(cameraUri)
                } else {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            },
            onDecline = { pendingSource = null }
        )
    }
}

@Composable
internal fun PhotoButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.height(
            120.dp
        ).clip(Shapes.Card).background(CardNeutral).border(1.5.dp, TextMuted.copy(alpha = 0.5f), Shapes.Card).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(Space.S))
        Text(label, fontSize = TextSize.Body, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}
