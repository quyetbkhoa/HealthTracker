$Output = "x:\AndroidStudioProject\HealthTracker\app_structure.md"
$SourceDir = "x:\AndroidStudioProject\HealthTracker\app\src\main\java\com\quyetbkhoa\healthtracker"

"# Cấu trúc dự án HealthTracker và Chi tiết Hàm/Lớp`n" | Out-File -FilePath $Output -Encoding utf8

Get-ChildItem -Path $SourceDir -Recurse -Filter *.kt | ForEach-Object {
    $relativePath = $_.FullName.Substring($SourceDir.Length + 1)
    "## File: $relativePath`n" | Out-File -FilePath $Output -Append -Encoding utf8
    
    $content = Get-Content $_.FullName
    foreach ($line in $content) {
        $trimmed = $line.Trim()
        if ($trimmed -match "^(public\s+|private\s+|protected\s+|internal\s+)?(abstract\s+|sealed\s+|open\s+|data\s+)?(class|interface|object|enum class)\s+([A-Za-z0-9_]+)") {
            "- **" + $matches[3] + " " + $matches[4] + "**`n" | Out-File -FilePath $Output -Append -Encoding utf8
        }
        elseif ($trimmed -match "^(public\s+|private\s+|protected\s+|internal\s+|override\s+|suspend\s+|inline\s+)?(fun)\s+([A-Za-z0-9_]+)\s*\(") {
            "  - Hàm: `" + $matches[3] + "()`n" | Out-File -FilePath $Output -Append -Encoding utf8
        }
        elseif ($trimmed -match "^@Composable\s*fun\s+([A-Za-z0-9_]+)\s*\(") {
            "  - Composable: `" + $matches[1] + "()`n" | Out-File -FilePath $Output -Append -Encoding utf8
        }
    }
    "`n" | Out-File -FilePath $Output -Append -Encoding utf8
}

Write-Output "Done"
