<?php

/**
 * Quick helper to check your Mac's IP address for configuring the Android watch app
 * 
 * Run this from terminal:
 * php -S localhost:8000
 * 
 * Then visit: http://localhost:8000/get-ip.php (or your-mac-ip:8000/get-ip.php)
 */

http_response_code(200);
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

$output = shell_exec('ifconfig 2>&1');
$lines = explode("\n", $output);

$ips = [];
foreach ($lines as $line) {
    if (preg_match('/inet\s+(\d+\.\d+\.\d+\.\d+)/', $line, $matches)) {
        $ip = $matches[1];
        if ($ip !== '127.0.0.1') {
            $ips[] = $ip;
        }
    }
}

$localIp = $ips[0] ?? gethostbyname(gethostname());

echo json_encode([
    'success' => true,
    'message' => 'Your machine IP addresses:',
    'ips' => $ips,
    'recommended_for_watch' => $localIp,
    'watch_config_url' => "http://{$localIp}:8000/api/v1",
    'instructions' => [
        'For Android Wear Emulator: use http://10.0.2.2:8000/api/v1',
        'For Physical Watch on Same Wi-Fi: use http://{$localIp}:8000/api/v1',
        'Update the URL in: app/src/main/java/com/school/erp/watch/data/SchoolDataRepository.kt',
        'Or use: SchoolDataRepository.Companion.setApiBaseUrl("http://{$localIp}:8000/api/v1")',
    ]
], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
