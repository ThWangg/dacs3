package ltdd.dacsba.groceries.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ltdd.dacsba.groceries.data.model.ProductTags

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagSelectorSection(
    categoryId: String,
    selectedTags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF787FF6)
) {
    var isExpanded by remember { mutableStateOf(false) }
    var customTag by remember { mutableStateOf("") }
    val suggestedTags = remember(categoryId) {
        ProductTags.suggestedTagsByCategory[categoryId] ?: emptyList()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Nhãn sản phẩm (Item Profile)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
                Text(
                    text = "Đã chọn: ${selectedTags.size}/${ProductTags.MAX_TAGS}",
                    fontSize = 12.sp,
                    color = if (selectedTags.size >= ProductTags.MAX_TAGS) Color.Red else Color.Gray
                )
            }
            TextButton(
                onClick = { isExpanded = !isExpanded },
                colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
            ) {
                Text(if (isExpanded) "Thu gọn" else "Quản lý nhãn")
            }
        }

if (!isExpanded && selectedTags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedTags.take(3).forEach { tag ->
                    SelectedTagChip(tag = tag, onRemove = {
                        onTagsChanged(selectedTags - tag)
                    }, accentColor = accentColor)
                }
                if (selectedTags.size > 3) {
                    AssistChip(
                        onClick = { isExpanded = true },
                        label = { Text("+${selectedTags.size - 3}") },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF0F0F0)),
                        border = null
                    )
                }
            }
        }

if (isExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    if (selectedTags.isNotEmpty()) {
                        Text("Nhãn đã chọn", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedTags.forEach { tag ->
                                SelectedTagChip(tag = tag, onRemove = {
                                    onTagsChanged(selectedTags - tag)
                                }, accentColor = accentColor)
                            }
                        }
                        Divider(color = Color(0xFFEEEEEE))
                    }

Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customTag,
                            onValueChange = { customTag = it },
                            placeholder = { Text("Thêm nhãn tự do...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                        IconButton(
                            onClick = {
                                val tagToAdd = customTag.trim().lowercase().replace(" ", "_")
                                if (tagToAdd.isNotEmpty() && !selectedTags.contains(tagToAdd)) {
                                    if (selectedTags.size < ProductTags.MAX_TAGS) {
                                        onTagsChanged(selectedTags + tagToAdd)
                                        customTag = ""
                                    }
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = accentColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Thêm")
                        }
                    }

if (suggestedTags.isNotEmpty()) {
                        Text("Gợi ý cho danh mục này", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val availableSuggestions = suggestedTags.filter { !selectedTags.contains(it) }
                            if (availableSuggestions.isEmpty()) {
                                Text("Đã chọn tất cả gợi ý", fontSize = 13.sp, color = Color.LightGray)
                            } else {
                                availableSuggestions.forEach { tag ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            if (selectedTags.size < ProductTags.MAX_TAGS) {
                                                onTagsChanged(selectedTags + tag)
                                            }
                                        },
                                        label = { Text(ProductTags.displayNameOf(tag)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color.White,
                                            labelColor = Color.DarkGray
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = Color(0xFFE0E0E0),
                                            enabled = true,
                                            selected = false
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedTagChip(
    tag: String,
    onRemove: () -> Unit,
    accentColor: Color
) {
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(ProductTags.displayNameOf(tag), fontWeight = FontWeight.Medium) },
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Xóa",
                modifier = Modifier.size(16.dp)
            )
        },
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = accentColor.copy(alpha = 0.15f),
            selectedLabelColor = accentColor,
            selectedTrailingIconColor = accentColor
        ),
        border = InputChipDefaults.inputChipBorder(
            borderColor = accentColor.copy(alpha = 0.3f),
            selectedBorderColor = accentColor.copy(alpha = 0.3f),
            enabled = true,
            selected = true
        )
    )
}
