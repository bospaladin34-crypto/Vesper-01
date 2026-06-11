<#
VESPER-01 Windows 11 GPU Validation + Truth Lock Harness Installer

Installs/updates:
  - src/chrono_55_summary.rs
  - src/windows_gpu_lock.rs
  - src/validation_truth_lock_harness.rs

Patches:
  - src/main.rs
  - src/axis_router.rs
  - src/report.rs

Usage from PowerShell:
  Set-ExecutionPolicy -Scope Process Bypass -Force
  .\VESPER_truth_lock_harness_windows11_gpu.ps1

Optional explicit project path:
  .\VESPER_truth_lock_harness_windows11_gpu.ps1 -ProjectDir "C:\Users\YOU\vesper\vesper_core"

Default path:
  $env:VESPER_CORE_DIR, if set
  otherwise: $HOME\vesper\vesper_core
#>

param(
    [string]$ProjectDir = $env:VESPER_CORE_DIR
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ProjectDir)) {
    $ProjectDir = Join-Path $HOME "vesper\vesper_core"
}

$ProjectDir = [System.IO.Path]::GetFullPath($ProjectDir)
$SrcDir = Join-Path $ProjectDir "src"

if (!(Test-Path $SrcDir)) {
    Write-Host "ERROR: could not find VESPER source directory: $SrcDir" -ForegroundColor Red
    Write-Host "Run with: .\VESPER_truth_lock_harness_windows11_gpu.ps1 -ProjectDir C:\path\to\vesper_core"
    exit 1
}

Set-Location $ProjectDir
New-Item -ItemType Directory -Force -Path "data", "data\chrono", "data\truth_lock", "data\gpu" | Out-Null

function Write-Utf8File {
    param(
        [string]$Path,
        [string]$Content
    )
    $Utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $Utf8NoBom)
}

function Patch-TextFile {
    param(
        [string]$Path,
        [scriptblock]$Transform
    )
    if (!(Test-Path $Path)) { return }
    $Text = [System.IO.File]::ReadAllText($Path)
    $NewText = & $Transform $Text
    if ($NewText -ne $Text) {
        Write-Utf8File -Path $Path -Content $NewText
    }
}

Write-Utf8File -Path "src\chrono_55_summary.rs" -Content @'
use std::collections::BTreeSet;
use std::fs;
use std::io;

use crate::chrono_55_batch::CHRONO_55_LEDGER_PATH;
use crate::theory_55_unity::THEORY_ID;

pub const CHRONO_55_SUMMARY_TXT: &str = "data/chrono/chrono_55_summary.txt";
pub const CHRONO_55_SUMMARY_MD: &str = "data/chrono/chrono_55_summary.md";
pub const CHRONO_55_SUMMARY_JSON: &str = "data/chrono/chrono_55_summary.json";

#[derive(Debug, Clone)]
pub struct Chrono55Summary {
    pub source_path: String,
    pub ledger_path: String,
    pub duplicate_rows: u64,
    pub logical_rows: u64,
    pub unique_row_indices: u64,
    pub vault_eligible: u64,
    pub theory_gate_passed: u64,
    pub parity_passed: u64,
    pub parity_failed: u64,
    pub mean_unity_55_score: f64,
    pub mean_legacy_44_score: f64,
    pub mean_unity_advantage: f64,
    pub mean_grid_rejection_score: f64,
    pub mean_jitter_proxy_s: f64,
    pub verdict: &'static str,
    pub verdict_reason: &'static str,
}

fn push_line(out: &mut String, line: &str) {
    out.push_str(line);
    out.push('\n');
}

fn json_escape(input: &str) -> String {
    let mut out = String::new();
    for ch in input.chars() {
        match ch {
            '"' => out.push_str("\\\""),
            '\\' => out.push_str("\\\\"),
            '\n' => out.push_str("\\n"),
            '\r' => out.push_str("\\r"),
            '\t' => out.push_str("\\t"),
            _ => out.push(ch),
        }
    }
    out
}

fn split_csv_simple(line: &str) -> Vec<String> {
    let mut fields = Vec::new();
    let mut current = String::new();
    let mut in_quotes = false;
    let mut chars = line.chars().peekable();

    while let Some(ch) = chars.next() {
        match ch {
            '"' => {
                if in_quotes && chars.peek() == Some(&'"') {
                    current.push('"');
                    chars.next();
                } else {
                    in_quotes = !in_quotes;
                }
            }
            ',' if !in_quotes => {
                fields.push(current.trim().to_string());
                current.clear();
            }
            _ => current.push(ch),
        }
    }

    fields.push(current.trim().to_string());
    fields
}

fn column_index(header: &[String], name: &str) -> Result<usize, String> {
    header
        .iter()
        .position(|field| field.trim() == name)
        .ok_or_else(|| format!("missing required ledger column: {}", name))
}

fn parse_u64_cell(fields: &[String], index: usize) -> Result<u64, String> {
    fields
        .get(index)
        .ok_or_else(|| "missing u64 cell".to_string())?
        .trim()
        .parse::<u64>()
        .map_err(|error| format!("invalid u64 cell: {}", error))
}

fn parse_f64_cell(fields: &[String], index: usize) -> Result<f64, String> {
    fields
        .get(index)
        .ok_or_else(|| "missing f64 cell".to_string())?
        .trim()
        .parse::<f64>()
        .map_err(|error| format!("invalid f64 cell: {}", error))
}

fn parse_bool_cell(fields: &[String], index: usize) -> Result<bool, String> {
    let value = fields
        .get(index)
        .ok_or_else(|| "missing bool cell".to_string())?
        .trim()
        .to_lowercase();

    match value.as_str() {
        "true" | "1" | "pass" | "passed" => Ok(true),
        "false" | "0" | "fail" | "failed" => Ok(false),
        _ => Err(format!("invalid bool cell: {}", value)),
    }
}

fn parse_status_cell(fields: &[String], index: usize) -> Result<bool, String> {
    let value = fields
        .get(index)
        .ok_or_else(|| "missing status cell".to_string())?
        .trim()
        .to_uppercase();

    Ok(value == "PASS")
}

pub fn summarize_chrono_55_ledger(path: &str) -> Result<Chrono55Summary, String> {
    let text = fs::read_to_string(path)
        .map_err(|error| format!("failed to read chrono ledger {}: {}", path, error))?;

    let mut lines = text.lines().filter(|line| !line.trim().is_empty());
    let header_line = lines
        .next()
        .ok_or_else(|| "chrono ledger is empty".to_string())?;

    let header = split_csv_simple(header_line);

    let idx_source_path = column_index(&header, "source_path")?;
    let idx_row_index = column_index(&header, "row_index")?;
    let idx_duplicate_id = column_index(&header, "duplicate_id")?;
    let idx_unity_55_score = column_index(&header, "unity_55_score")?;
    let idx_legacy_44_score = column_index(&header, "legacy_44_score")?;
    let idx_unity_advantage = column_index(&header, "unity_advantage")?;
    let idx_grid_rejection_score = column_index(&header, "grid_rejection_score")?;
    let idx_jitter_proxy_s = column_index(&header, "jitter_proxy_s")?;
    let idx_vault_eligible = column_index(&header, "vault_eligible")?;
    let idx_theory_gate = column_index(&header, "theory_gate")?;
    let idx_parity_status = column_index(&header, "parity_status")?;

    let mut source_path = String::new();
    let mut duplicate_rows = 0_u64;
    let mut logical_rows = 0_u64;
    let mut unique_indices = BTreeSet::new();
    let mut vault_eligible = 0_u64;
    let mut theory_gate_passed = 0_u64;
    let mut parity_passed = 0_u64;
    let mut parity_failed = 0_u64;
    let mut unity_55_sum = 0.0_f64;
    let mut legacy_44_sum = 0.0_f64;
    let mut unity_advantage_sum = 0.0_f64;
    let mut grid_rejection_sum = 0.0_f64;
    let mut jitter_sum = 0.0_f64;

    for line in lines {
        let fields = split_csv_simple(line);
        if fields.len() < header.len() {
            continue;
        }

        duplicate_rows += 1;
        let row_index = parse_u64_cell(&fields, idx_row_index)?;
        let duplicate_id = parse_u64_cell(&fields, idx_duplicate_id)?;
        unique_indices.insert(row_index);

        if source_path.is_empty() {
            source_path = fields
                .get(idx_source_path)
                .map(|field| field.trim_matches('"').to_string())
                .unwrap_or_default();
        }

        if parse_status_cell(&fields, idx_parity_status)? {
            parity_passed += 1;
        } else {
            parity_failed += 1;
        }

        if duplicate_id != 0 {
            continue;
        }

        logical_rows += 1;

        if parse_bool_cell(&fields, idx_vault_eligible)? {
            vault_eligible += 1;
        }

        if parse_bool_cell(&fields, idx_theory_gate)? {
            theory_gate_passed += 1;
        }

        unity_55_sum += parse_f64_cell(&fields, idx_unity_55_score)?;
        legacy_44_sum += parse_f64_cell(&fields, idx_legacy_44_score)?;
        unity_advantage_sum += parse_f64_cell(&fields, idx_unity_advantage)?;
        grid_rejection_sum += parse_f64_cell(&fields, idx_grid_rejection_score)?;
        jitter_sum += parse_f64_cell(&fields, idx_jitter_proxy_s)?;
    }

    let denom = if logical_rows == 0 { 1.0 } else { logical_rows as f64 };
    let mean_unity_advantage = unity_advantage_sum / denom;
    let mean_grid_rejection_score = grid_rejection_sum / denom;
    let gate_rate = if logical_rows == 0 {
        0.0
    } else {
        theory_gate_passed as f64 / logical_rows as f64
    };

    let parity_clean = parity_failed == 0 && duplicate_rows > 0;

    let (verdict, verdict_reason) = if logical_rows == 0 {
        ("INSUFFICIENT_DATA", "ledger contains no logical rows")
    } else if !parity_clean {
        ("AUDIT_FAIL", "duplicate parity audit contains failed rows")
    } else if mean_unity_advantage > 0.0 && mean_grid_rejection_score >= 0.25 && gate_rate > 0.0 {
        ("PROMISING_UNDER_CURRENT_CONTROLS", "55 coefficient outperforms 44 reference with nonzero theory gate rate and 60Hz rejection control")
    } else if mean_unity_advantage > 0.0 {
        ("WEAK_POSITIVE", "55 coefficient outperforms 44 reference but controls or theory-gate rate are not strong")
    } else if mean_unity_advantage == 0.0 {
        ("NEUTRAL", "55 coefficient and 44 reference are equivalent under current ledger")
    } else {
        ("NEGATIVE_UNDER_CURRENT_CONTROLS", "44 reference outperforms 55 coefficient under current ledger")
    };

    Ok(Chrono55Summary {
        source_path,
        ledger_path: path.to_string(),
        duplicate_rows,
        logical_rows,
        unique_row_indices: unique_indices.len() as u64,
        vault_eligible,
        theory_gate_passed,
        parity_passed,
        parity_failed,
        mean_unity_55_score: unity_55_sum / denom,
        mean_legacy_44_score: legacy_44_sum / denom,
        mean_unity_advantage,
        mean_grid_rejection_score,
        mean_jitter_proxy_s: jitter_sum / denom,
        verdict,
        verdict_reason,
    })
}

pub fn render_summary_text(summary: &Chrono55Summary) -> String {
    let mut out = String::new();
    push_line(&mut out, &format!("CHRONO_55_SUMMARY: {}", THEORY_ID));
    push_line(&mut out, &format!("SOURCE_PATH: {}", summary.source_path));
    push_line(&mut out, &format!("LEDGER_PATH: {}", summary.ledger_path));
    push_line(&mut out, &format!("DUPLICATE_ROWS: {}", summary.duplicate_rows));
    push_line(&mut out, &format!("LOGICAL_ROWS: {}", summary.logical_rows));
    push_line(&mut out, &format!("UNIQUE_ROW_INDICES: {}", summary.unique_row_indices));
    push_line(&mut out, &format!("VAULT_ELIGIBLE: {}", summary.vault_eligible));
    push_line(&mut out, &format!("THEORY_GATE_PASSED: {}", summary.theory_gate_passed));
    push_line(&mut out, &format!("PARITY_PASSED: {}", summary.parity_passed));
    push_line(&mut out, &format!("PARITY_FAILED: {}", summary.parity_failed));
    push_line(&mut out, &format!("MEAN_UNITY_55_SCORE: {:.12}", summary.mean_unity_55_score));
    push_line(&mut out, &format!("MEAN_LEGACY_44_SCORE: {:.12}", summary.mean_legacy_44_score));
    push_line(&mut out, &format!("MEAN_UNITY_ADVANTAGE: {:.12}", summary.mean_unity_advantage));
    push_line(&mut out, &format!("MEAN_GRID_REJECTION_SCORE: {:.12}", summary.mean_grid_rejection_score));
    push_line(&mut out, &format!("MEAN_JITTER_PROXY_S: {:.12}", summary.mean_jitter_proxy_s));
    push_line(&mut out, &format!("VERDICT: {}", summary.verdict));
    push_line(&mut out, &format!("VERDICT_REASON: {}", summary.verdict_reason));
    push_line(&mut out, "STATUS: OK");
    out
}

pub fn render_summary_markdown(summary: &Chrono55Summary) -> String {
    let mut out = String::new();
    push_line(&mut out, "# Chrono-55 Evidence Summary");
    push_line(&mut out, "");
    push_line(&mut out, &format!("**Theory:** `{}`", THEORY_ID));
    push_line(&mut out, &format!("**Ledger:** `{}`", summary.ledger_path));
    push_line(&mut out, &format!("**Source:** `{}`", summary.source_path));
    push_line(&mut out, "");
    push_line(&mut out, "## Verdict");
    push_line(&mut out, &format!("- Verdict: `{}`", summary.verdict));
    push_line(&mut out, &format!("- Reason: {}", summary.verdict_reason));
    push_line(&mut out, "");
    push_line(&mut out, "## Counts");
    push_line(&mut out, "| Metric | Value |");
    push_line(&mut out, "|---|---:|");
    push_line(&mut out, &format!("| Duplicate rows | `{}` |", summary.duplicate_rows));
    push_line(&mut out, &format!("| Logical rows | `{}` |", summary.logical_rows));
    push_line(&mut out, &format!("| Unique row indices | `{}` |", summary.unique_row_indices));
    push_line(&mut out, &format!("| Vault eligible | `{}` |", summary.vault_eligible));
    push_line(&mut out, &format!("| Theory gate passed | `{}` |", summary.theory_gate_passed));
    push_line(&mut out, &format!("| Parity passed | `{}` |", summary.parity_passed));
    push_line(&mut out, &format!("| Parity failed | `{}` |", summary.parity_failed));
    push_line(&mut out, "");
    push_line(&mut out, "## Means");
    push_line(&mut out, "| Metric | Mean |");
    push_line(&mut out, "|---|---:|");
    push_line(&mut out, &format!("| Unity 55 score | `{:.12}` |", summary.mean_unity_55_score));
    push_line(&mut out, &format!("| Legacy 44 score | `{:.12}` |", summary.mean_legacy_44_score));
    push_line(&mut out, &format!("| Unity advantage | `{:.12}` |", summary.mean_unity_advantage));
    push_line(&mut out, &format!("| Grid rejection score | `{:.12}` |", summary.mean_grid_rejection_score));
    push_line(&mut out, &format!("| Jitter proxy seconds | `{:.12}` |", summary.mean_jitter_proxy_s));
    out
}

pub fn render_summary_json(summary: &Chrono55Summary) -> String {
    let mut out = String::new();
    push_line(&mut out, "{");
    push_line(&mut out, &format!("  \"theory_id\": \"{}\",", json_escape(THEORY_ID)));
    push_line(&mut out, &format!("  \"source_path\": \"{}\",", json_escape(&summary.source_path)));
    push_line(&mut out, &format!("  \"ledger_path\": \"{}\",", json_escape(&summary.ledger_path)));
    push_line(&mut out, &format!("  \"duplicate_rows\": {},", summary.duplicate_rows));
    push_line(&mut out, &format!("  \"logical_rows\": {},", summary.logical_rows));
    push_line(&mut out, &format!("  \"unique_row_indices\": {},", summary.unique_row_indices));
    push_line(&mut out, &format!("  \"vault_eligible\": {},", summary.vault_eligible));
    push_line(&mut out, &format!("  \"theory_gate_passed\": {},", summary.theory_gate_passed));
    push_line(&mut out, &format!("  \"parity_passed\": {},", summary.parity_passed));
    push_line(&mut out, &format!("  \"parity_failed\": {},", summary.parity_failed));
    push_line(&mut out, &format!("  \"mean_unity_55_score\": {:.12},", summary.mean_unity_55_score));
    push_line(&mut out, &format!("  \"mean_legacy_44_score\": {:.12},", summary.mean_legacy_44_score));
    push_line(&mut out, &format!("  \"mean_unity_advantage\": {:.12},", summary.mean_unity_advantage));
    push_line(&mut out, &format!("  \"mean_grid_rejection_score\": {:.12},", summary.mean_grid_rejection_score));
    push_line(&mut out, &format!("  \"mean_jitter_proxy_s\": {:.12},", summary.mean_jitter_proxy_s));
    push_line(&mut out, &format!("  \"verdict\": \"{}\",", json_escape(summary.verdict)));
    push_line(&mut out, &format!("  \"verdict_reason\": \"{}\"", json_escape(summary.verdict_reason)));
    push_line(&mut out, "}");
    out
}

pub fn export_summary(summary: &Chrono55Summary) -> io::Result<()> {
    fs::create_dir_all("data/chrono")?;
    fs::write(CHRONO_55_SUMMARY_TXT, render_summary_text(summary))?;
    fs::write(CHRONO_55_SUMMARY_MD, render_summary_markdown(summary))?;
    fs::write(CHRONO_55_SUMMARY_JSON, render_summary_json(summary))?;
    Ok(())
}

fn summary_path_from_args(args: &[String], command_name: &str) -> &'_ str {
    if args.len() == 3 {
        args[2].as_str()
    } else if args.len() == 2 {
        CHRONO_55_LEDGER_PATH
    } else {
        eprintln!("ERROR: {} accepts optional ledger path", command_name);
        eprintln!("Example: cargo run -- {} data/chrono_55_ledger.csv", command_name);
        std::process::exit(1);
    }
}

pub fn run_chrono_55_summary(args: &[String]) {
    let path = summary_path_from_args(args, "chrono-55-summary");
    let summary = summarize_chrono_55_ledger(path).expect("chrono-55 summary failed");
    print!("{}", render_summary_text(&summary));
}

pub fn run_chrono_55_summary_json(args: &[String]) {
    let path = summary_path_from_args(args, "chrono-55-summary-json");
    let summary = summarize_chrono_55_ledger(path).expect("chrono-55 JSON summary failed");
    print!("{}", render_summary_json(&summary));
}

pub fn run_chrono_55_summary_export(args: &[String]) {
    let path = summary_path_from_args(args, "chrono-55-summary-export");
    let summary = summarize_chrono_55_ledger(path).expect("chrono-55 summary export failed");
    export_summary(&summary).expect("failed to export chrono-55 summary");
    println!("CHRONO_55_SUMMARY_EXPORT: {}", THEORY_ID);
    println!("TXT: {}", CHRONO_55_SUMMARY_TXT);
    println!("MARKDOWN: {}", CHRONO_55_SUMMARY_MD);
    println!("JSON: {}", CHRONO_55_SUMMARY_JSON);
    println!("STATUS: OK");
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_csv_split_simple() {
        let fields = split_csv_simple("a,\"b,c\",d");
        assert_eq!(fields.len(), 3);
        assert_eq!(fields[1], "b,c");
    }

    #[test]
    fn test_json_escape() {
        assert_eq!(json_escape("a\"b"), "a\\\"b");
    }
}
'@

Write-Utf8File -Path "src\windows_gpu_lock.rs" -Content @'
use std::env;
use std::fs;
use std::io;
use std::process::Command;

pub const WINDOWS_GPU_LOCK_VERSION: &str = "windows_11_gpu_lock.v1";
pub const GPU_LOCK_TXT: &str = "data/gpu/windows_gpu_lock.txt";
pub const GPU_LOCK_JSON: &str = "data/gpu/windows_gpu_lock.json";

#[derive(Debug, Clone)]
pub struct WindowsGpuLock {
    pub os: &'static str,
    pub arch: &'static str,
    pub cuda_path_present: bool,
    pub vulkan_sdk_present: bool,
    pub oneapi_root_present: bool,
    pub nvidia_smi_present: bool,
    pub nvidia_smi_summary: String,
    pub directml_hint_present: bool,
    pub gpu_truth_lock_status: &'static str,
}

fn env_present(name: &str) -> bool {
    env::var(name)
        .map(|value| !value.trim().is_empty())
        .unwrap_or(false)
}

fn run_nvidia_smi() -> (bool, String) {
    let output = Command::new("nvidia-smi")
        .args([
            "--query-gpu=name,driver_version,memory.total",
            "--format=csv,noheader",
        ])
        .output();

    match output {
        Ok(output) if output.status.success() => {
            let stdout = String::from_utf8_lossy(&output.stdout).trim().to_string();
            if stdout.is_empty() {
                (true, "nvidia-smi detected but returned empty query".to_string())
            } else {
                (true, stdout)
            }
        }
        Ok(output) => {
            let stderr = String::from_utf8_lossy(&output.stderr).trim().to_string();
            if stderr.is_empty() {
                (false, "nvidia-smi returned nonzero status".to_string())
            } else {
                (false, stderr)
            }
        }
        Err(error) => (false, format!("nvidia-smi unavailable: {}", error)),
    }
}

fn json_escape(input: &str) -> String {
    let mut out = String::new();
    for ch in input.chars() {
        match ch {
            '"' => out.push_str("\\\""),
            '\\' => out.push_str("\\\\"),
            '\n' => out.push_str("\\n"),
            '\r' => out.push_str("\\r"),
            '\t' => out.push_str("\\t"),
            _ => out.push(ch),
        }
    }
    out
}

pub fn detect_windows_gpu_lock() -> WindowsGpuLock {
    let (nvidia_smi_present, nvidia_smi_summary) = run_nvidia_smi();
    let cuda_path_present = env_present("CUDA_PATH");
    let vulkan_sdk_present = env_present("VULKAN_SDK");
    let oneapi_root_present = env_present("ONEAPI_ROOT");
    let directml_hint_present = env_present("DIRECTML_PATH") || env_present("ONNXRUNTIME_DIR");

    let gpu_truth_lock_status = if nvidia_smi_present || cuda_path_present || vulkan_sdk_present || directml_hint_present {
        "GPU_PATH_DETECTED"
    } else {
        "CPU_FALLBACK_OR_UNKNOWN_GPU_PATH"
    };

    WindowsGpuLock {
        os: env::consts::OS,
        arch: env::consts::ARCH,
        cuda_path_present,
        vulkan_sdk_present,
        oneapi_root_present,
        nvidia_smi_present,
        nvidia_smi_summary,
        directml_hint_present,
        gpu_truth_lock_status,
    }
}

pub fn render_gpu_lock_text(lock: &WindowsGpuLock) -> String {
    format!(
        "WINDOWS_GPU_LOCK: {}\nOS: {}\nARCH: {}\nCUDA_PATH_PRESENT: {}\nVULKAN_SDK_PRESENT: {}\nONEAPI_ROOT_PRESENT: {}\nDIRECTML_HINT_PRESENT: {}\nNVIDIA_SMI_PRESENT: {}\nNVIDIA_SMI_SUMMARY: {}\nGPU_TRUTH_LOCK_STATUS: {}\nGPU_SCOPE: acceleration boundary only; scientific claims still require deterministic ledger verification\n",
        WINDOWS_GPU_LOCK_VERSION,
        lock.os,
        lock.arch,
        lock.cuda_path_present,
        lock.vulkan_sdk_present,
        lock.oneapi_root_present,
        lock.directml_hint_present,
        lock.nvidia_smi_present,
        lock.nvidia_smi_summary,
        lock.gpu_truth_lock_status,
    )
}

pub fn render_gpu_lock_json(lock: &WindowsGpuLock) -> String {
    format!(
        "{{\n  \"version\": \"{}\",\n  \"os\": \"{}\",\n  \"arch\": \"{}\",\n  \"cuda_path_present\": {},\n  \"vulkan_sdk_present\": {},\n  \"oneapi_root_present\": {},\n  \"directml_hint_present\": {},\n  \"nvidia_smi_present\": {},\n  \"nvidia_smi_summary\": \"{}\",\n  \"gpu_truth_lock_status\": \"{}\",\n  \"gpu_scope\": \"acceleration boundary only; scientific claims still require deterministic ledger verification\"\n}}\n",
        WINDOWS_GPU_LOCK_VERSION,
        json_escape(lock.os),
        json_escape(lock.arch),
        lock.cuda_path_present,
        lock.vulkan_sdk_present,
        lock.oneapi_root_present,
        lock.directml_hint_present,
        lock.nvidia_smi_present,
        json_escape(&lock.nvidia_smi_summary),
        json_escape(lock.gpu_truth_lock_status),
    )
}

pub fn export_gpu_lock(lock: &WindowsGpuLock) -> io::Result<()> {
    fs::create_dir_all("data/gpu")?;
    fs::write(GPU_LOCK_TXT, render_gpu_lock_text(lock))?;
    fs::write(GPU_LOCK_JSON, render_gpu_lock_json(lock))?;
    Ok(())
}

pub fn run_gpu_lock() {
    let lock = detect_windows_gpu_lock();
    print!("{}", render_gpu_lock_text(&lock));
}

pub fn run_gpu_lock_json() {
    let lock = detect_windows_gpu_lock();
    print!("{}", render_gpu_lock_json(&lock));
}

pub fn run_gpu_lock_export() {
    let lock = detect_windows_gpu_lock();
    export_gpu_lock(&lock).expect("failed to export GPU lock");
    println!("GPU_LOCK_EXPORT: {}", WINDOWS_GPU_LOCK_VERSION);
    println!("TXT: {}", GPU_LOCK_TXT);
    println!("JSON: {}", GPU_LOCK_JSON);
    println!("STATUS: OK");
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_gpu_lock_detection_runs() {
        let lock = detect_windows_gpu_lock();
        assert!(!lock.os.is_empty());
        assert!(!lock.arch.is_empty());
    }

    #[test]
    fn test_gpu_json_contains_status() {
        let lock = detect_windows_gpu_lock();
        let json = render_gpu_lock_json(&lock);
        assert!(json.contains("gpu_truth_lock_status"));
    }
}
'@

Write-Utf8File -Path "src\validation_truth_lock_harness.rs" -Content @'
use std::fs;
use std::io;

use crate::chrono_55_batch::CHRONO_55_LEDGER_PATH;
use crate::chrono_55_summary::{
    render_summary_json,
    render_summary_markdown,
    render_summary_text,
    summarize_chrono_55_ledger,
};
use crate::formal_hypothesis::{
    render_hypothesis_json,
    render_hypothesis_markdown,
    render_hypothesis_text,
};
use crate::report::{
    render_report_json,
    render_report_markdown,
    render_report_text,
};
use crate::self_audit::run_self_audit_checks;
use crate::theory_55_unity::{
    GRID_REJECTION_HZ,
    HEARTBEAT_HZ,
    LEGACY_COEFFICIENT,
    NODE_AXIS,
    PHASE_ANCHOR,
    THEORY_ID,
    THEORY_NAME,
    UNITY_COEFFICIENT,
};
use crate::windows_gpu_lock::{
    detect_windows_gpu_lock,
    render_gpu_lock_json,
    render_gpu_lock_text,
};

pub const TRUTH_LOCK_VERSION: &str = "vesper_truth_lock_harness.windows11_gpu.v1";
pub const TRUTH_LOCK_DIR: &str = "data/truth_lock";
pub const TRUTH_LOCK_TXT: &str = "data/truth_lock/VESPER_VALIDATION_TRUTH_LOCK_HARNESS_WINDOWS11_GPU.txt";
pub const TRUTH_LOCK_MD: &str = "data/truth_lock/VESPER_VALIDATION_TRUTH_LOCK_HARNESS_WINDOWS11_GPU.md";
pub const TRUTH_LOCK_JSON: &str = "data/truth_lock/VESPER_VALIDATION_TRUTH_LOCK_HARNESS_WINDOWS11_GPU.json";

const MODULES: &[(&str, &str, &str, &str)] = &[
    ("windows_gpu_lock.v1", "COMPUTATION", "ACTIVE", "Windows 11 GPU path detection and acceleration-boundary truth lock"),
    ("magnetometer.processor.v1", "DYNAMICS", "ACTIVE", "x/y/z magnitude, phase_delta, vault gate"),
    ("sensor_input.android_csv_bridge.v1", "DYNAMICS", "ACTIVE", "CSV/JSON-lines sensor intake"),
    ("chrono_mag.55_unity_phase_analysis.v1", "DYNAMICS", "ACTIVE", "single-row 55-vs-44 chronometric phase analysis"),
    ("chrono_mag.55_unity_batch_ledger.v1", "DYNAMICS", "ACTIVE", "batch ledger generation with duplicated rows"),
    ("chrono_mag.55_unity_summary.v1", "DYNAMICS", "ACTIVE", "evidence verdict from generated ledger"),
    ("formal_hypothesis.55_unity.v1", "COGNITION", "ACTIVE_PRIMARY_HYPOTHESIS", "H0/H1 theory record"),
    ("self_audit.runtime_verification.v1", "MULTI_AXIS", "ACTIVE", "deterministic runtime verification"),
    ("report.review_packet.v1", "MULTI_AXIS", "ACTIVE", "TXT/Markdown/JSON review packet"),
    ("noether.invariance_check.v1", "STRUCTURE", "ACTIVE", "symmetry/conservation tolerance checks"),
    ("harmonic_reservoir.storage.v1", "DYNAMICS", "ACTIVE", "stateful reservoir + 1N4148 virtual gate + F15 damping"),
    ("parity_clock.66_6ms.v1", "COMPUTATION", "ACTIVE", "66.6ms parity tick schedule"),
    ("pauli.propagation.v1", "COMPUTATION", "ACTIVE", "Clifford/Pauli propagation and Noether audit"),
    ("rabi.coupled_mode_splitting.v1", "DYNAMICS", "ACTIVE", "spectral center conservation audit"),
    ("floquet.periodic_drive_coupling.v1", "DYNAMICS", "ACTIVE", "periodic-drive recurrence audit"),
    ("tda.graph_betti.v1", "STRUCTURE", "ACTIVE", "graph topology and persistence family"),
    ("hardware_gate.v1", "DYNAMICS", "ACTIVE", "safe hardware-boundary gate"),
];

const COMMANDS: &[(&str, &str)] = &[
    ("cargo run -- gpu-lock", "print Windows/GPU acceleration-boundary status"),
    ("cargo run -- gpu-lock-export", "export Windows/GPU acceleration-boundary status"),
    ("cargo run -- theory-55", "print primary 55-Unity theory record"),
    ("cargo run -- hypothesis-55", "print formal H0/H1 hypothesis"),
    ("cargo run -- sensor-template", "create sample sensor stream"),
    ("cargo run -- chrono-55-batch data/sensor_stream.csv 0", "generate chrono-55 duplicated ledger"),
    ("cargo run -- chrono-55-summary", "summarize default chrono-55 ledger"),
    ("cargo run -- chrono-55-summary-export", "export chrono-55 evidence summary"),
    ("cargo run -- self-audit", "run deterministic runtime self-audit"),
    ("cargo run -- report-export", "export review packet"),
    ("cargo run -- truth-lock-export", "export Windows 11 GPU truth-lock bundle"),
];

fn push_line(out: &mut String, line: &str) {
    out.push_str(line);
    out.push('\n');
}

fn json_escape(input: &str) -> String {
    let mut out = String::new();
    for ch in input.chars() {
        match ch {
            '"' => out.push_str("\\\""),
            '\\' => out.push_str("\\\\"),
            '\n' => out.push_str("\\n"),
            '\r' => out.push_str("\\r"),
            '\t' => out.push_str("\\t"),
            _ => out.push(ch),
        }
    }
    out
}

fn indent_json_fragment(fragment: &str, spaces: usize) -> String {
    let pad = " ".repeat(spaces);
    fragment
        .lines()
        .map(|line| format!("{}{}", pad, line))
        .collect::<Vec<_>>()
        .join("\n")
}

fn self_audit_status() -> (&'static str, usize, usize, usize) {
    let checks = run_self_audit_checks();
    let total = checks.len();
    let passed = checks.iter().filter(|check| check.passed).count();
    let failed = total - passed;

    if failed == 0 {
        ("PASS", total, passed, failed)
    } else {
        ("FAIL", total, passed, failed)
    }
}

fn chrono_summary_text_if_available() -> String {
    match summarize_chrono_55_ledger(CHRONO_55_LEDGER_PATH) {
        Ok(summary) => render_summary_text(&summary),
        Err(error) => format!(
            "CHRONO_55_SUMMARY: UNAVAILABLE\nLEDGER_PATH: {}\nREASON: {}\n",
            CHRONO_55_LEDGER_PATH,
            error
        ),
    }
}

fn chrono_summary_markdown_if_available() -> String {
    match summarize_chrono_55_ledger(CHRONO_55_LEDGER_PATH) {
        Ok(summary) => render_summary_markdown(&summary),
        Err(error) => format!(
            "# Chrono-55 Evidence Summary\n\n`UNAVAILABLE`\n\nLedger: `{}`\n\nReason: `{}`\n",
            CHRONO_55_LEDGER_PATH,
            error
        ),
    }
}

fn chrono_summary_json_if_available() -> String {
    match summarize_chrono_55_ledger(CHRONO_55_LEDGER_PATH) {
        Ok(summary) => render_summary_json(&summary),
        Err(error) => format!(
            "{{\"status\":\"UNAVAILABLE\",\"ledger_path\":\"{}\",\"reason\":\"{}\"}}",
            json_escape(CHRONO_55_LEDGER_PATH),
            json_escape(&error)
        ),
    }
}

pub fn render_truth_lock_text() -> String {
    let (audit_status, audit_total, audit_passed, audit_failed) = self_audit_status();
    let gpu_lock = detect_windows_gpu_lock();
    let mut out = String::new();

    push_line(&mut out, &format!("VESPER_TRUTH_LOCK_HARNESS: {}", TRUTH_LOCK_VERSION));
    push_line(&mut out, &format!("THEORY_ID: {}", THEORY_ID));
    push_line(&mut out, &format!("THEORY_NAME: {}", THEORY_NAME));
    push_line(&mut out, &format!("AXIS: {}", NODE_AXIS));
    push_line(&mut out, "TARGET_OS: Windows 11");
    push_line(&mut out, "GPU_STATUS: GPU acceleration boundary registered");
    push_line(&mut out, "STATUS: ACTIVE_VALIDATION_HARNESS");
    push_line(&mut out, "PROOF_STATUS: NOT_PROVEN");
    push_line(&mut out, "VALIDATION_STATUS: LOCAL_EXPERIMENTAL");
    push_line(&mut out, "PEER_REVIEW_STATUS: PENDING");
    push_line(&mut out, "TRUTH_LOCK_RULE: claims must map to source, computation, invariant, ledger, or hypothesis boundary");
    push_line(&mut out, "GPU_TRUTH_LOCK_RULE: GPU acceleration can speed computation but does not increase evidentiary rank without deterministic ledger parity");

    push_line(&mut out, "SECTION: WINDOWS_GPU_LOCK");
    push_line(&mut out, &render_gpu_lock_text(&gpu_lock));

    push_line(&mut out, "SECTION: CONSTANTS");
    push_line(&mut out, &format!("PHASE_ANCHOR: {:.8}", PHASE_ANCHOR));
    push_line(&mut out, &format!("HEARTBEAT_HZ: {:.12}", HEARTBEAT_HZ));
    push_line(&mut out, &format!("UNITY_COEFFICIENT: {:.12}", UNITY_COEFFICIENT));
    push_line(&mut out, &format!("LEGACY_COEFFICIENT: {:.12}", LEGACY_COEFFICIENT));
    push_line(&mut out, &format!("GRID_REJECTION_HZ: {:.12}", GRID_REJECTION_HZ));

    push_line(&mut out, "SECTION: SELF_AUDIT");
    push_line(&mut out, &format!("SELF_AUDIT_STATUS: {}", audit_status));
    push_line(&mut out, &format!("SELF_AUDIT_TOTAL: {}", audit_total));
    push_line(&mut out, &format!("SELF_AUDIT_PASSED: {}", audit_passed));
    push_line(&mut out, &format!("SELF_AUDIT_FAILED: {}", audit_failed));

    push_line(&mut out, "SECTION: MODULE_REGISTRY");
    for (module, axis, status, role) in MODULES {
        push_line(&mut out, &format!("MODULE: {} | AXIS: {} | STATUS: {} | ROLE: {}", module, axis, status, role));
    }

    push_line(&mut out, "SECTION: VERIFICATION_COMMANDS");
    for (command, role) in COMMANDS {
        push_line(&mut out, &format!("COMMAND: {} | ROLE: {}", command, role));
    }

    push_line(&mut out, "SECTION: CLAIM_CLASSIFICATION");
    push_line(&mut out, "CLAIM_CLASS: MEASURED | requirement: raw sensor row or reproducible ledger row");
    push_line(&mut out, "CLAIM_CLASS: COMPUTED | requirement: deterministic function and output");
    push_line(&mut out, "CLAIM_CLASS: GPU_ACCELERATED | requirement: CPU/GPU parity, same ledger hash, and deterministic output tolerance");
    push_line(&mut out, "CLAIM_CLASS: INVARIANT | requirement: parity, Noether, or self-audit check");
    push_line(&mut out, "CLAIM_CLASS: HYPOTHESIS | requirement: H0/H1 and disproof criteria");
    push_line(&mut out, "CLAIM_CLASS: SPECULATIVE | requirement: excluded from evidence claims until tested");

    push_line(&mut out, "SECTION: FORMAL_HYPOTHESIS_TEXT");
    push_line(&mut out, &render_hypothesis_text());

    push_line(&mut out, "SECTION: CHRONO_55_SUMMARY");
    push_line(&mut out, &chrono_summary_text_if_available());

    push_line(&mut out, "SECTION: REPORT_TEXT");
    push_line(&mut out, &render_report_text());

    out
}

pub fn render_truth_lock_markdown() -> String {
    let (audit_status, audit_total, audit_passed, audit_failed) = self_audit_status();
    let gpu_lock = detect_windows_gpu_lock();
    let mut out = String::new();

    push_line(&mut out, "# VESPER-01 Windows 11 GPU Validation and Truth Lock Harness");
    push_line(&mut out, "");
    push_line(&mut out, &format!("**Version:** `{}`", TRUTH_LOCK_VERSION));
    push_line(&mut out, &format!("**Theory ID:** `{}`", THEORY_ID));
    push_line(&mut out, &format!("**Theory name:** `{}`", THEORY_NAME));
    push_line(&mut out, &format!("**Axis:** `{}`", NODE_AXIS));
    push_line(&mut out, "**Target OS:** `Windows 11`");
    push_line(&mut out, "**GPU status:** `GPU acceleration boundary registered`");
    push_line(&mut out, "**Proof status:** `NOT_PROVEN`");
    push_line(&mut out, "**Validation status:** `LOCAL_EXPERIMENTAL`");
    push_line(&mut out, "**Peer-review status:** `PENDING`");
    push_line(&mut out, "");
    push_line(&mut out, "## Truth-lock rule");
    push_line(&mut out, "Claims must map to at least one of: source, computation, invariant, ledger, or hypothesis boundary.");
    push_line(&mut out, "");
    push_line(&mut out, "## GPU truth-lock rule");
    push_line(&mut out, "GPU acceleration can speed computation, but it does not increase evidentiary rank unless CPU/GPU parity and deterministic ledger reproduction both pass.");
    push_line(&mut out, "");
    push_line(&mut out, "## Windows GPU lock");
    push_line(&mut out, "```text");
    push_line(&mut out, &render_gpu_lock_text(&gpu_lock));
    push_line(&mut out, "```");
    push_line(&mut out, "");
    push_line(&mut out, "## Self-audit");
    push_line(&mut out, &format!("- Status: `{}`", audit_status));
    push_line(&mut out, &format!("- Total: `{}`", audit_total));
    push_line(&mut out, &format!("- Passed: `{}`", audit_passed));
    push_line(&mut out, &format!("- Failed: `{}`", audit_failed));
    push_line(&mut out, "");
    push_line(&mut out, "## Module registry");
    push_line(&mut out, "| Module | Axis | Status | Role |");
    push_line(&mut out, "|---|---:|---:|---|");
    for (module, axis, status, role) in MODULES {
        push_line(&mut out, &format!("| `{}` | `{}` | `{}` | {} |", module, axis, status, role));
    }
    push_line(&mut out, "");
    push_line(&mut out, "## Verification commands");
    for (command, role) in COMMANDS {
        push_line(&mut out, &format!("- `{}` — {}", command, role));
    }
    push_line(&mut out, "");
    push_line(&mut out, "---");
    push_line(&mut out, "");
    push_line(&mut out, &render_hypothesis_markdown());
    push_line(&mut out, "");
    push_line(&mut out, "---");
    push_line(&mut out, "");
    push_line(&mut out, &chrono_summary_markdown_if_available());
    push_line(&mut out, "");
    push_line(&mut out, "---");
    push_line(&mut out, "");
    push_line(&mut out, &render_report_markdown());

    out
}

pub fn render_truth_lock_json() -> String {
    let (audit_status, audit_total, audit_passed, audit_failed) = self_audit_status();
    let gpu_lock = detect_windows_gpu_lock();
    let mut out = String::new();

    push_line(&mut out, "{");
    push_line(&mut out, &format!("  \"truth_lock_version\": \"{}\",", json_escape(TRUTH_LOCK_VERSION)));
    push_line(&mut out, &format!("  \"theory_id\": \"{}\",", json_escape(THEORY_ID)));
    push_line(&mut out, &format!("  \"theory_name\": \"{}\",", json_escape(THEORY_NAME)));
    push_line(&mut out, &format!("  \"axis\": \"{}\",", json_escape(NODE_AXIS)));
    push_line(&mut out, "  \"target_os\": \"Windows 11\",");
    push_line(&mut out, "  \"proof_status\": \"NOT_PROVEN\",");
    push_line(&mut out, "  \"validation_status\": \"LOCAL_EXPERIMENTAL\",");
    push_line(&mut out, "  \"peer_review_status\": \"PENDING\",");
    push_line(&mut out, "  \"gpu_truth_lock_rule\": \"GPU acceleration can speed computation but does not increase evidentiary rank without deterministic ledger parity\",");
    push_line(&mut out, "  \"self_audit\": {");
    push_line(&mut out, &format!("    \"status\": \"{}\",", audit_status));
    push_line(&mut out, &format!("    \"total\": {},", audit_total));
    push_line(&mut out, &format!("    \"passed\": {},", audit_passed));
    push_line(&mut out, &format!("    \"failed\": {}", audit_failed));
    push_line(&mut out, "  },");
    push_line(&mut out, "  \"gpu_lock\": ");
    push_line(&mut out, &indent_json_fragment(&render_gpu_lock_json(&gpu_lock), 2));
    push_line(&mut out, ",");
    push_line(&mut out, "  \"modules\": [");
    for (index, (module, axis, status, role)) in MODULES.iter().enumerate() {
        let comma = if index + 1 == MODULES.len() { "" } else { "," };
        push_line(&mut out, &format!("    {{\"module\":\"{}\",\"axis\":\"{}\",\"status\":\"{}\",\"role\":\"{}\"}}{}", json_escape(module), json_escape(axis), json_escape(status), json_escape(role), comma));
    }
    push_line(&mut out, "  ],");
    push_line(&mut out, "  \"commands\": [");
    for (index, (command, role)) in COMMANDS.iter().enumerate() {
        let comma = if index + 1 == COMMANDS.len() { "" } else { "," };
        push_line(&mut out, &format!("    {{\"command\":\"{}\",\"role\":\"{}\"}}{}", json_escape(command), json_escape(role), comma));
    }
    push_line(&mut out, "  ],");
    push_line(&mut out, "  \"formal_hypothesis\": ");
    push_line(&mut out, &indent_json_fragment(&render_hypothesis_json(), 2));
    push_line(&mut out, ",");
    push_line(&mut out, "  \"chrono_summary\": ");
    push_line(&mut out, &indent_json_fragment(&chrono_summary_json_if_available(), 2));
    push_line(&mut out, ",");
    push_line(&mut out, "  \"report\": ");
    push_line(&mut out, &indent_json_fragment(&render_report_json(), 2));
    push_line(&mut out, "");
    push_line(&mut out, "}");

    out
}

pub fn export_truth_lock_harness() -> io::Result<()> {
    fs::create_dir_all(TRUTH_LOCK_DIR)?;
    fs::write(TRUTH_LOCK_TXT, render_truth_lock_text())?;
    fs::write(TRUTH_LOCK_MD, render_truth_lock_markdown())?;
    fs::write(TRUTH_LOCK_JSON, render_truth_lock_json())?;
    Ok(())
}

pub fn print_truth_lock_harness() {
    print!("{}", render_truth_lock_text());
}

pub fn print_truth_lock_harness_md() {
    print!("{}", render_truth_lock_markdown());
}

pub fn print_truth_lock_harness_json() {
    print!("{}", render_truth_lock_json());
}

pub fn export_truth_lock_harness_bundle() {
    export_truth_lock_harness().expect("failed to export truth-lock harness");
    println!("TRUTH_LOCK_EXPORT: {}", TRUTH_LOCK_VERSION);
    println!("TXT: {}", TRUTH_LOCK_TXT);
    println!("MARKDOWN: {}", TRUTH_LOCK_MD);
    println!("JSON: {}", TRUTH_LOCK_JSON);
    println!("STATUS: OK");
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_truth_lock_text_contains_signature() {
        let text = render_truth_lock_text();
        assert!(text.contains("VESPER_TRUTH_LOCK_HARNESS"));
        assert!(text.contains("WINDOWS_GPU_LOCK"));
    }

    #[test]
    fn test_truth_lock_json_contains_gpu_lock() {
        let text = render_truth_lock_json();
        assert!(text.contains("gpu_lock"));
        assert!(text.contains("windows_11_gpu_lock"));
    }
}
'@

# Patch main.rs.
Patch-TextFile -Path "src\main.rs" -Transform {
    param($Text)

    if ($Text -notmatch 'mod chrono_55_summary;') {
        if ($Text -match 'mod chrono_55_batch;') {
            $Text = $Text -replace 'mod chrono_55_batch;', "mod chrono_55_batch;`nmod chrono_55_summary;"
        } elseif ($Text -match 'mod theory_55_unity;') {
            $Text = $Text -replace 'mod theory_55_unity;', "mod theory_55_unity;`nmod chrono_55_summary;"
        } else {
            $Text = "mod chrono_55_summary;`n" + $Text
        }
    }

    if ($Text -notmatch 'mod windows_gpu_lock;') {
        $Text = $Text -replace 'mod chrono_55_summary;', "mod chrono_55_summary;`nmod windows_gpu_lock;"
    }

    if ($Text -notmatch 'mod validation_truth_lock_harness;') {
        $Text = $Text -replace 'mod windows_gpu_lock;', "mod windows_gpu_lock;`nmod validation_truth_lock_harness;"
    }

    $SummaryImport = 'use chrono_55_summary::{run_chrono_55_summary, run_chrono_55_summary_export, run_chrono_55_summary_json};'
    if ($Text -notlike "*$SummaryImport*") {
        $Text = $SummaryImport + "`n" + $Text
    }

    $GpuImport = 'use windows_gpu_lock::{run_gpu_lock, run_gpu_lock_export, run_gpu_lock_json};'
    if ($Text -notlike "*$GpuImport*") {
        $Text = $GpuImport + "`n" + $Text
    }

    $TruthImport = 'use validation_truth_lock_harness::{export_truth_lock_harness_bundle, print_truth_lock_harness, print_truth_lock_harness_json, print_truth_lock_harness_md};'
    if ($Text -notlike "*$TruthImport*") {
        $Text = $TruthImport + "`n" + $Text
    }

    if ($Text -notmatch 'chrono-55-summary \[ledger_path\]') {
        $Help = @'
    println!("  gpu-lock               Print Windows 11 GPU acceleration-boundary lock");
    println!("  gpu-lock-json          Print Windows 11 GPU lock as JSON");
    println!("  gpu-lock-export        Export Windows 11 GPU lock");
    println!("  chrono-55-summary [ledger_path]");
    println!("  chrono-55-summary-json [ledger_path]");
    println!("  chrono-55-summary-export [ledger_path]");
    println!("  truth-lock             Print Windows 11 GPU validation/truth-lock harness");
    println!("  truth-lock-md          Print Windows 11 GPU validation/truth-lock harness as Markdown");
    println!("  truth-lock-json        Print Windows 11 GPU validation/truth-lock harness as JSON");
    println!("  truth-lock-export      Export Windows 11 GPU validation/truth-lock harness bundle");
'@
        if ($Text -match 'println!\("  chrono-55') {
            $Text = $Text -replace '(?m)^\s*println!\("  chrono-55', ($Help + "`n    println!(\"  chrono-55")
        } elseif ($Text -match 'println!\("  report') {
            $Text = $Text -replace '(?m)^\s*println!\("  report', ($Help + "`n    println!(\"  report")
        }
    }

    if ($Text -notmatch '"gpu-lock" => run_gpu_lock\(\)') {
        $Branches = @'
        "gpu-lock" => run_gpu_lock(),
        "gpu-lock-json" => run_gpu_lock_json(),
        "gpu-lock-export" => run_gpu_lock_export(),
        "chrono-55-summary" => run_chrono_55_summary(&args),
        "chrono-55-summary-json" => run_chrono_55_summary_json(&args),
        "chrono-55-summary-export" => run_chrono_55_summary_export(&args),
        "truth-lock" => print_truth_lock_harness(),
        "truth-lock-md" => print_truth_lock_harness_md(),
        "truth-lock-json" => print_truth_lock_harness_json(),
        "truth-lock-export" => export_truth_lock_harness_bundle(),
'@
        if ($Text -match '"chrono-55-batch" => run_chrono_55_batch\(&args\),') {
            $Text = $Text -replace '"chrono-55-batch" => run_chrono_55_batch\(&args\),', "\"chrono-55-batch\" => run_chrono_55_batch(&args),`n$Branches"
        } elseif ($Text -match '"report" => print_report\(\),') {
            $Text = $Text -replace '"report" => print_report\(\),', "\"report\" => print_report(),`n$Branches"
        }
    }

    return $Text
}

# Patch axis_router.rs if present.
Patch-TextFile -Path "src\axis_router.rs" -Transform {
    param($Text)

    if ($Text -match 'command: "truth-lock-export"') { return $Text }

    $Routes = @'
        AxisRoute {
            command: "gpu-lock",
            axis: VesperAxis::Computation,
            module: "windows_gpu_lock.status.v1",
            rationale: "detects Windows 11 GPU acceleration boundary and local GPU toolchain hints",
        },
        AxisRoute {
            command: "gpu-lock-json",
            axis: VesperAxis::Computation,
            module: "windows_gpu_lock.json.v1",
            rationale: "prints machine-readable Windows 11 GPU lock state",
        },
        AxisRoute {
            command: "gpu-lock-export",
            axis: VesperAxis::Computation,
            module: "windows_gpu_lock.export.v1",
            rationale: "exports Windows 11 GPU lock state",
        },
        AxisRoute {
            command: "chrono-55-summary",
            axis: VesperAxis::Dynamics,
            module: "chrono_mag.55_unity_summary.v1",
            rationale: "summarizes chrono-55 duplicated ledger into evidence verdict and verification metrics",
        },
        AxisRoute {
            command: "chrono-55-summary-json",
            axis: VesperAxis::Dynamics,
            module: "chrono_mag.55_unity_summary_json.v1",
            rationale: "prints machine-readable chrono-55 evidence summary",
        },
        AxisRoute {
            command: "chrono-55-summary-export",
            axis: VesperAxis::Dynamics,
            module: "chrono_mag.55_unity_summary_export.v1",
            rationale: "exports TXT, Markdown, and JSON chrono-55 evidence summaries",
        },
        AxisRoute {
            command: "truth-lock",
            axis: VesperAxis::MultiAxis,
            module: "truth_lock.windows11_gpu_text.v1",
            rationale: "prints complete Windows 11 GPU validation and truth-lock harness",
        },
        AxisRoute {
            command: "truth-lock-md",
            axis: VesperAxis::MultiAxis,
            module: "truth_lock.windows11_gpu_markdown.v1",
            rationale: "prints complete Windows 11 GPU validation and truth-lock harness as Markdown",
        },
        AxisRoute {
            command: "truth-lock-json",
            axis: VesperAxis::MultiAxis,
            module: "truth_lock.windows11_gpu_json.v1",
            rationale: "prints complete Windows 11 GPU validation and truth-lock harness as JSON",
        },
        AxisRoute {
            command: "truth-lock-export",
            axis: VesperAxis::MultiAxis,
            module: "truth_lock.windows11_gpu_export.v1",
            rationale: "exports complete Windows 11 GPU validation and truth-lock harness bundle",
        },

'@

    if ($Text -match '        AxisRoute \{\r?\n            command: "chrono-55-batch",') {
        $Text = $Text -replace '        AxisRoute \{\r?\n            command: "chrono-55-batch",', ($Routes + '        AxisRoute {' + "`n" + '            command: "chrono-55-batch",')
    } elseif ($Text -match '        AxisRoute \{\r?\n            command: "report",') {
        $Text = $Text -replace '        AxisRoute \{\r?\n            command: "report",', ($Routes + '        AxisRoute {' + "`n" + '            command: "report",')
    }

    return $Text
}

# Patch report.rs module registry if present.
Patch-TextFile -Path "src\report.rs" -Transform {
    param($Text)

    if ($Text -match 'windows_gpu_lock.v1') { return $Text }

    if ($Text -match '\("chrono_mag\.55_unity_batch_ledger\.v1", "DYNAMICS", "ACTIVE"\),') {
        $Text = $Text -replace '\("chrono_mag\.55_unity_batch_ledger\.v1", "DYNAMICS", "ACTIVE"\),', '("chrono_mag.55_unity_batch_ledger.v1", "DYNAMICS", "ACTIVE"),' + "`n    " + '("chrono_mag.55_unity_summary.v1", "DYNAMICS", "ACTIVE"),' + "`n    " + '("windows_gpu_lock.v1", "COMPUTATION", "ACTIVE"),' + "`n    " + '("truth_lock.windows11_gpu.v1", "MULTI_AXIS", "ACTIVE"),'
    } elseif ($Text -match '\("sensor_input\.adapter\.v1", "DYNAMICS", "ACTIVE"\),') {
        $Text = $Text -replace '\("sensor_input\.adapter\.v1", "DYNAMICS", "ACTIVE"\),', '("sensor_input.adapter.v1", "DYNAMICS", "ACTIVE"),' + "`n    " + '("chrono_mag.55_unity_summary.v1", "DYNAMICS", "ACTIVE"),' + "`n    " + '("windows_gpu_lock.v1", "COMPUTATION", "ACTIVE"),' + "`n    " + '("truth_lock.windows11_gpu.v1", "MULTI_AXIS", "ACTIVE"),'
    }

    return $Text
}

Write-Host "VESPER Windows 11 GPU truth-lock harness installed." -ForegroundColor Green
Write-Host ""
Write-Host "Run:"
Write-Host "  cargo test"
Write-Host "  cargo run -- gpu-lock"
Write-Host "  cargo run -- chrono-55-summary"
Write-Host "  cargo run -- truth-lock-export"
Write-Host ""
Write-Host "Exported files after truth-lock-export:"
Write-Host "  data\truth_lock\VESPER_VALIDATION_TRUTH_LOCK_HARNESS_WINDOWS11_GPU.txt"
Write-Host "  data\truth_lock\VESPER_VALIDATION_TRUTH_LOCK_HARNESS_WINDOWS11_GPU.md"
Write-Host "  data\truth_lock\VESPER_VALIDATION_TRUTH_LOCK_HARNESS_WINDOWS11_GPU.json"
