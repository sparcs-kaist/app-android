package com.example.soap.Features.TaxiPreview

//
//@Composable
//fun TaxiPreviewView(
//    room: TaxiRoom,
//    viewModel: ViewModel,
//    onDismiss: () -> Unit = {}
//) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    var cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(room.source.toLatLng(), 13f)
//    }
//
//    var showError by remember { mutableStateOf(false) }
//    var errorMessage by remember { mutableStateOf("") }
//    var pathPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
//
//    LaunchedEffect(Unit) {
//        try {
//            pathPoints = viewModel.calculateRoutePoints(
//                room.source.toLatLng(),
//                room.destination.toLatLng()
//            )
//        } catch (e: Exception) {
//            errorMessage = e.localizedMessage ?: "Unknown error"
//            showError = true
//        }
//    }
//
//    if (showError) {
//        AlertDialog(
//            onDismissRequest = { showError = false },
//            confirmButton = {
//                TextButton(onClick = { showError = false }) {
//                    Text("Okay")
//                }
//            },
//            title = { Text("Error") },
//            text = { Text(errorMessage) }
//        )
//    }
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        GoogleMap(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp),
//            cameraPositionState = cameraPositionState
//        ) {
//            Marker(
//                state = MarkerState(position = room.source.toLatLng()),
//                title = room.source.title.localized(),
//                snippet = "Source"
//            )
//            Marker(
//                state = MarkerState(position = room.destination.toLatLng()),
//                title = room.destination.title.localized(),
//                snippet = "Destination"
//            )
//            if (pathPoints.isNotEmpty()) {
//                Polyline(points = pathPoints, color = Color.Blue)
//            }
//        }
//
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = room.title,
//                    style = MaterialTheme.typography.titleSmall,
//                    modifier = Modifier.weight(1f)
//                )
//                Text("${room.participants.size}/${room.capacity} joined")
//            }
//
//            Spacer(Modifier.height(8.dp))
//            Text("From: ${room.source.title.localized()}")
//            Text("To: ${room.destination.title.localized()}")
//            Spacer(Modifier.height(8.dp))
//            Text("Depart at: ${room.departAt.formattedString()}")
//
//            Spacer(Modifier.height(16.dp))
//
//            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                IconButton(onClick = { /* share */ }) {
//                    Icon(Icons.Default.Share, contentDescription = "Share")
//                }
//
//                Button(
//                    onClick = {
//                        scope.launch {
//                            try {
//                                viewModel.joinRoom(
//                                    id = room.id,
//                                    onSuccess = onDismiss,
//                                    onError = {
//                                        errorMessage = it.message ?: "Unknown error"
//                                        showError = true
//                                    }
//                                )
//                            } catch (e: Exception) {
//                                errorMessage = e.message ?: "Unknown error"
//                                showError = true
//                            }
//                        }
//                    },
//                    modifier = Modifier.weight(1f),
//                    enabled = room.participants.size < room.capacity &&
//                            !viewModel.isJoined(room.participants)
//                ) {
//                    Text(
//                        when {
//                            viewModel.isJoined(room.participants) -> "Joined"
//                            room.participants.size >= room.capacity -> "This room is full"
//                            else -> "Join"
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Preview
//@Composable
//private fun Preview() {
//    SoapTheme {
//        TaxiPreviewView(room = TaxiRoom.mockList[0])
//    }
//}
