param(
    [string]$BaseUrl = "http://localhost:8080",
    [long]$CampaignId,
    [int]$TotalRequests = 10000,
    [int]$Concurrency = 200,
    [long]$UserIdStart = 100000,
    [string]$AdminToken = "",
    [string]$UserIdHeader = "X-User-Id",
    [string]$OutputDir = "docs/performance/results"
)

if ($CampaignId -le 0) {
    throw "CampaignId 必须为正数，例如：-CampaignId 2001"
}
if ($TotalRequests -le 0 -or $Concurrency -le 0) {
    throw "TotalRequests 和 Concurrency 必须为正数"
}

$ErrorActionPreference = "Stop"
$endpoint = "$BaseUrl/api/user/coupons/receive"
$startedAt = Get-Date
$timestamp = $startedAt.ToString("yyyyMMdd-HHmmss")
$resultDir = Join-Path $OutputDir $timestamp
New-Item -ItemType Directory -Force -Path $resultDir | Out-Null

$requestQueue = [System.Collections.Concurrent.ConcurrentQueue[object]]::new()
for ($i = 0; $i -lt $TotalRequests; $i++) {
    $requestQueue.Enqueue([pscustomobject]@{
        Index = $i
        UserId = $UserIdStart + $i
        RequestId = "bench-$timestamp-$i"
    })
}

$resultBag = [System.Collections.Concurrent.ConcurrentBag[object]]::new()
$pool = [runspacefactory]::CreateRunspacePool(1, $Concurrency)
$pool.Open()
$jobs = New-Object System.Collections.Generic.List[object]

$worker = {
    param($Queue, $Results, $Endpoint, $CampaignId, $UserIdHeader, $AdminToken)

    $item = $null
    while ($Queue.TryDequeue([ref]$item)) {
        $headers = @{
            "Content-Type" = "application/json"
            $UserIdHeader = [string]$item.UserId
        }
        if ($AdminToken) {
            $headers["X-Admin-Token"] = $AdminToken
        }

        $body = @{
            requestId = $item.RequestId
            userId = $item.UserId
            campaignId = $CampaignId
        } | ConvertTo-Json -Compress

        $watch = [System.Diagnostics.Stopwatch]::StartNew()
        $statusCode = 0
        $ok = $false
        $errorMessage = ""
        try {
            $response = Invoke-WebRequest -Uri $Endpoint -Method Post -Headers $headers -Body $body -UseBasicParsing -TimeoutSec 15
            $statusCode = [int]$response.StatusCode
            $ok = $statusCode -ge 200 -and $statusCode -lt 300
        } catch {
            if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
                $statusCode = [int]$_.Exception.Response.StatusCode
            }
            $errorMessage = $_.Exception.Message
        } finally {
            $watch.Stop()
        }

        $Results.Add([pscustomobject]@{
            index = $item.Index
            userId = $item.UserId
            requestId = $item.RequestId
            statusCode = $statusCode
            success = $ok
            latencyMs = $watch.Elapsed.TotalMilliseconds
            error = $errorMessage
        })
    }
}

for ($i = 0; $i -lt $Concurrency; $i++) {
    $powershell = [powershell]::Create()
    $powershell.RunspacePool = $pool
    $null = $powershell.AddScript($worker)
        .AddArgument($requestQueue)
        .AddArgument($resultBag)
        .AddArgument($endpoint)
        .AddArgument($CampaignId)
        .AddArgument($UserIdHeader)
        .AddArgument($AdminToken)
    $jobs.Add([pscustomobject]@{
        PowerShell = $powershell
        Handle = $powershell.BeginInvoke()
    })
}

foreach ($job in $jobs) {
    $job.PowerShell.EndInvoke($job.Handle)
    $job.PowerShell.Dispose()
}
$pool.Close()
$pool.Dispose()

$finishedAt = Get-Date
$elapsedSeconds = [Math]::Max(($finishedAt - $startedAt).TotalSeconds, 0.001)
$results = $resultBag.ToArray() | Sort-Object index
$successCount = ($results | Where-Object success).Count
$failureCount = $results.Count - $successCount
$latencies = @($results | ForEach-Object { [double]$_.latencyMs } | Sort-Object)

function Get-Percentile([double[]]$Values, [double]$Percentile) {
    if ($Values.Count -eq 0) {
        return 0
    }
    $position = [Math]::Ceiling(($Percentile / 100.0) * $Values.Count) - 1
    $position = [Math]::Min([Math]::Max($position, 0), $Values.Count - 1)
    return [Math]::Round($Values[$position], 2)
}

$summary = [pscustomobject]@{
    baseUrl = $BaseUrl
    endpoint = $endpoint
    campaignId = $CampaignId
    totalRequests = $TotalRequests
    concurrency = $Concurrency
    startedAt = $startedAt.ToString("o")
    finishedAt = $finishedAt.ToString("o")
    elapsedSeconds = [Math]::Round($elapsedSeconds, 3)
    qps = [Math]::Round($results.Count / $elapsedSeconds, 2)
    successCount = $successCount
    failureCount = $failureCount
    successRate = [Math]::Round($successCount * 100.0 / [Math]::Max($results.Count, 1), 2)
    latency = [pscustomobject]@{
        p50Ms = Get-Percentile $latencies 50
        p95Ms = Get-Percentile $latencies 95
        p99Ms = Get-Percentile $latencies 99
        maxMs = if ($latencies.Count -eq 0) { 0 } else { [Math]::Round($latencies[-1], 2) }
    }
}

$summaryPath = Join-Path $resultDir "summary.json"
$detailPath = Join-Path $resultDir "detail.csv"
$summary | ConvertTo-Json -Depth 5 | Set-Content -Path $summaryPath -Encoding UTF8
$results | Export-Csv -Path $detailPath -NoTypeInformation -Encoding UTF8

$summary | Format-List
Write-Host "summary: $summaryPath"
Write-Host "detail : $detailPath"
