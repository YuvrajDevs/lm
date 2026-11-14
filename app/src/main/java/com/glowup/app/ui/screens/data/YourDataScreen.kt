package com.glowup.app.ui.screens.data

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.glowup.app.R
import com.glowup.app.ui.components.DeleteEverythingDialog
import com.glowup.app.ui.components.DetailHeader
import com.glowup.app.ui.components.ReMakeUDialog
import com.glowup.app.ui.components.exportMyData
import com.glowup.app.ui.components.goBack
import com.glowup.app.ui.data.ConsentStore
import com.glowup.app.ui.data.DataCatalog
import com.glowup.app.ui.data.DataCategory
import com.glowup.app.ui.data.PhotoVault
import com.glowup.app.ui.design.LineHeight
import com.glowup.app.ui.design.Shapes
import com.glowup.app.ui.design.Space
import com.glowup.app.ui.design.TextSize
import com.glowup.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun YourDataScreen(navController: NavHostController) {
    val context = LocalContext.current
    var pendingDelete by remember { mutableStateOf<DataCategory?>(null) }
    var deletePhotos by remember { mutableStateOf(false) }
    var deleteEverything by remember { mutableStateOf(false) }
    // Bumped after a delete so summaries (which read plain values like file counts) recompose.
    var version by remember { mutableIntStateOf(0) }
    val photoCount = remember(version) { PhotoVault.totalCount() }

    Box(modifier = Modifier.fillMaxSize().background(Surface).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(stringResource(R.string.your_data_your_data)) { navController.goBack() }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(Space.Gutter, Space.L, Space.Gutter, Space.Xxl),
                verticalArrangement = Arrangement.spacedBy(Space.S)
            ) {
                item { PrivacyHero(photoCount) }

                item { SectionLabel("What we keep and why") }
                items(DataCatalog.categories, key = { it.id }) { category ->
                    CategoryCard(category) { pendingDelete = category }
                }

                item { SectionLabel("Photos") }
                item {
                    InfoCard(
                        icon = Icons.Outlined.PhotoLibrary,
                        title = if (photoCount == 0) "No photos saved" else "$photoCount photo${if (photoCount == 1) "" else "s"} saved",
                        body = "Face and body scans are measured on your phone and thrown away straight after — only the numbers are kept. " +
                            "Photos are saved only for features you switch on, like progress photos. They stay inside the app: never in your gallery, never backed up, never uploaded.",
                        action = if (photoCount > 0) "Delete all photos" else null,
                        onAction = { deletePhotos = true }
                    )
                }

                item { SectionLabel("Permissions you've agreed to") }
                if (ConsentStore.granted.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.your_data_none_yet_we_ll_always),
                            color = TextSecondary,
                            fontSize = TextSize.BodySmall,
                            lineHeight = LineHeight.Compact,
                            modifier = Modifier.padding(horizontal = Space.Xs)
                        )
                    }
                }
                items(ConsentStore.granted.entries.toList(), key = { it.key.key }) { (consent, date) ->
                    InfoCard(
                        icon = Icons.Outlined.Visibility,
                        title = consent.title,
                        body =
                        consent.summary + "\nAgreed on " + LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())) +
                            ".",
                        action = "Withdraw",
                        onAction = {
                            ConsentStore.withdraw(consent)
                            // Android's own permission can only be turned off in system settings.
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    )
                }

                item { SectionLabel("Backup") }
                item {
                    InfoCard(
                        icon = Icons.Outlined.CloudOff,
                        title = "We never upload your data",
                        body = "Re:MakeU has no account and no servers. If you've turned on Android backup for your Google account, your phone may include Re:MakeU's data (never photos) in its own encrypted backup, so you can restore it on a new phone. We can't see that backup."
                    )
                }

                item { Spacer(Modifier.height(Space.S)) }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Pill)
                            .background(Ink)
                            .clickable { exportMyData(context) }
                            .padding(vertical = Space.L),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Space.S))
                        Text(
                            stringResource(R.string.your_data_export_a_copy_of_my),
                            color = Color.White,
                            fontSize = TextSize.BodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.Pill)
                            .background(MaroonTint)
                            .clickable { deleteEverything = true }
                            .padding(vertical = Space.L),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.your_data_delete_everything),
                            color = Maroon,
                            fontSize = TextSize.BodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        pendingDelete?.let { category ->
            ReMakeUDialog(
                onDismissRequest = { pendingDelete = null },
                title = "Delete ${category.title.lowercase().replaceFirstChar { it.uppercase() }}?",
                message = "This removes ${category.summary()} from this phone. Points you've already earned stay. This can't be undone.",
                secondaryText = "Cancel",
                primaryText = "Delete",
                primaryDestructive = true,
                onPrimary = {
                    category.clear?.invoke()
                    pendingDelete = null
                    version++
                }
            )
        }

        if (deletePhotos) {
            ReMakeUDialog(
                onDismissRequest = { deletePhotos = false },
                title = "Delete all photos?",
                message = "Every saved photo is removed from this phone. This can't be undone.",
                secondaryText = "Cancel",
                primaryText = "Delete",
                primaryDestructive = true,
                onPrimary = {
                    PhotoVault.deleteAll()
                    deletePhotos = false
                    version++
                }
            )
        }

        if (deleteEverything) DeleteEverythingDialog(navController) { deleteEverything = false }
    }
}

@Composable
private fun PrivacyHero(photoCount: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(Shapes.Large).background(Ink).padding(Space.Xl, Space.Xl)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(Space.M))
            Column {
                Text(stringResource(R.string.your_data_private_by_design), color = Color.White, fontSize = TextSize.Title, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.your_data_everything_stays_on_this_phone), color = Color.White.copy(alpha = 0.6f), fontSize = TextSize.BodySmall)
            }
        }
        Spacer(Modifier.height(Space.L))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.S)) {
            listOf("No account", "No ads", "No trackers", "Never sold").forEach {
                Text(
                    it,
                    color = Color.White,
                    fontSize = TextSize.Micro,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(Shapes.Pill).background(Color.White.copy(alpha = 0.12f)).padding(Space.S, Space.Xs),
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(Space.M))
        Text(
            "You can see, export or delete any of it below.\n$photoCount photo${if (photoCount == 1) "" else "s"} stored · 0 bytes uploaded, ever.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = TextSize.BodySmall,
            lineHeight = LineHeight.Compact
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        color = TextSecondary,
        fontSize = TextSize.Caption,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.4.sp,
        modifier = Modifier.padding(top = Space.L, bottom = 2.dp, start = Space.Xs)
    )
}

@Composable
private fun CategoryCard(category: DataCategory, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.M)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(category.title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            if (category.clear != null) {
                Text(
                    stringResource(R.string.common_delete),
                    color = Maroon,
                    fontSize = TextSize.BodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(Shapes.Pill).clickable(onClick = onDelete).padding(Space.S, Space.Xs)
                )
            } else {
                Text(stringResource(R.string.your_data_needed_by_the_app), color = TextSecondary, fontSize = TextSize.Caption, fontWeight = FontWeight.Medium)
            }
        }
        Text(category.summary(), color = Ink, fontSize = TextSize.BodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.height(Space.S))
        Text(category.why, color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
    }
}

@Composable
private fun InfoCard(icon: ImageVector, title: String, body: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clip(Shapes.Card).background(CardNeutral).padding(Space.L, Space.M)) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.padding(top = 1.dp).size(20.dp))
        Spacer(Modifier.width(Space.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = TextSize.BodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(Space.Xs))
            Text(body, color = TextSecondary, fontSize = TextSize.BodySmall, lineHeight = LineHeight.Compact)
            action?.let {
                Text(
                    it,
                    color = Maroon,
                    fontSize = TextSize.BodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = Space.S).clip(Shapes.Pill).clickable(onClick = onAction).padding(vertical = Space.Xs)
                )
            }
        }
    }
}
