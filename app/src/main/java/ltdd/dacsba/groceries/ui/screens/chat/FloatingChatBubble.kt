package ltdd.dacsba.groceries.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import ltdd.dacsba.groceries.R
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun FloatingChatBubble(
    onClick: () -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) return

    val chatRooms by chatViewModel.chatRooms.collectAsState()

val totalUnread = chatRooms.sumOf { it.unreadCounts[currentUser.uid] ?: 0 }

    val scope = rememberCoroutineScope()
    
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val configuration = LocalConfiguration.current
    val screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 16.dp, bottom = 80.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {

                            val isOutOfBounds = offsetX.value > 0f || offsetY.value > 0f || 
                                                offsetX.value < -screenWidth + 100f || 
                                                offsetY.value < -screenHeight + 150f
                            
                            if (isOutOfBounds) {
                                scope.launch {
                                    launch { offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
                                    launch { offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    }
                }
        ) {
            FloatingActionButton(
                onClick = onClick,
                containerColor = Color(0xFF1877F2),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_messenger),
                    contentDescription = "Tin nhắn",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Unspecified
                )
            }

            if (totalUnread > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .background(Color.Red, CircleShape)
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (totalUnread > 99) "99+" else totalUnread.toString(),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
