package com.school.erp.watch.data

object ApiConfig {
    // For Android Wear emulator: use 10.0.2.2 to access host machine localhost
    // For physical watch on same Wi-Fi: replace with your Mac's IP address
    // You can find your Mac IP by running: ifconfig | grep "inet "
    const val API_BASE_URL = "http://10.0.2.2:8000/api/v1"
    
    const val CONNECT_TIMEOUT_MS = 5_000  // Increased from 3s to 5s
    const val READ_TIMEOUT_MS = 10_000    // Increased from 5s to 10s for better reliability
}
