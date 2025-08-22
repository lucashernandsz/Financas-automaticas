import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nate.autofinance.utils.Categories

@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    chipHeight: Dp = 32.dp,    // um pouco menor
    iconSize: Dp = 14.dp,      // ícone reduzido
    hSpacing: Dp = 8.dp
) {
    val scroll = rememberScrollState()

    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .height(chipHeight)                 // fixa a altura da linha
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(hSpacing)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category
            val iconId = Categories.fixedCategories
                .firstOrNull { it.name.equals(category, ignoreCase = true) }
                ?.iconResId

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                },
                leadingIcon = iconId?.let { id ->
                    {
                        Icon(
                            painter = painterResource(id),
                            contentDescription = null,
                            modifier = Modifier.height(iconSize)
                        )
                    }
                },
                modifier = Modifier.height(chipHeight),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary,
                    labelColor = if (isSelected)
                        Color.White
                    else
                        MaterialTheme.colorScheme.onSurface,
                    iconColor = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSecondary,
                    selectedContainerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
