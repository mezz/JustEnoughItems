#!/usr/bin/env python3
"""Run a command and capture JVM diagnostics if it is still running later."""

from __future__ import annotations

import os
import shutil
import signal
import subprocess
import sys
import threading
from datetime import datetime
from pathlib import Path


DIAGNOSTIC_DELAY_ENV = "JEI_JVM_DIAGNOSTIC_DELAY_SECONDS"
DIAGNOSTIC_FILE_ENV = "JEI_JVM_DIAGNOSTIC_FILE"
DEFAULT_DIAGNOSTIC_DELAY_SECONDS = 600
DEFAULT_DIAGNOSTIC_FILE = "client-gametest-diagnostics.txt"
JCMD_TIMEOUT_SECONDS = 30


class DiagnosticWriter:
    def __init__(self, output_file):
        self.output_file = output_file

    def write(self, value):
        sys.stdout.write(value)
        sys.stdout.flush()
        self.output_file.write(value)
        self.output_file.flush()

    def line(self, value=""):
        self.write(value + "\n")


def parse_positive_int_env(name, default):
    value = os.environ.get(name, str(default))
    try:
        parsed_value = int(value)
    except ValueError:
        parsed_value = 0

    if parsed_value <= 0:
        raise ValueError(f"{name} must be a positive integer, got: {value}")
    return parsed_value


def collect_descendant_pids(root_pid):
    result = subprocess.run(
        ["ps", "-eo", "pid=,ppid="],
        check=True,
        capture_output=True,
        text=True,
    )
    children_by_parent = {}
    for line in result.stdout.splitlines():
        fields = line.split()
        if len(fields) != 2:
            continue
        pid, parent_pid = (int(field) for field in fields)
        children_by_parent.setdefault(parent_pid, []).append(pid)

    descendant_pids = []
    pending_pids = [root_pid]
    while pending_pids:
        pid = pending_pids.pop()
        descendant_pids.append(pid)
        pending_pids.extend(children_by_parent.get(pid, ()))
    return sorted(descendant_pids)


def get_process_command_name(pid):
    result = subprocess.run(
        ["ps", "-p", str(pid), "-o", "comm="],
        check=False,
        capture_output=True,
        text=True,
    )
    return Path(result.stdout.strip()).name


def write_process_state(pid, writer):
    result = subprocess.run(
        [
            "ps",
            "-p",
            str(pid),
            "-o",
            "pid=,ppid=,stat=,etime=,pcpu=,pmem=,comm=,args=",
        ],
        check=False,
        capture_output=True,
        text=True,
    )
    if result.stdout:
        writer.write(result.stdout)

    wait_channel_path = Path("/proc") / str(pid) / "wchan"
    try:
        wait_channel = wait_channel_path.read_text(encoding="utf-8").strip()
    except (OSError, UnicodeError):
        return
    writer.line(f"PID {pid} kernel wait channel: {wait_channel}")


def write_subprocess_output(writer, output):
    if not output:
        return
    if isinstance(output, bytes):
        output = output.decode("utf-8", errors="replace")
    writer.write(output)


def run_jcmd(pid, command, writer):
    try:
        result = subprocess.run(
            ["jcmd", str(pid), *command],
            check=False,
            capture_output=True,
            text=True,
            timeout=JCMD_TIMEOUT_SECONDS,
        )
    except subprocess.TimeoutExpired as error:
        write_subprocess_output(writer, error.stdout)
        write_subprocess_output(writer, error.stderr)
        writer.line(f"jcmd {' '.join(command)} timed out for JVM {pid}")
        return False

    write_subprocess_output(writer, result.stdout)
    write_subprocess_output(writer, result.stderr)
    return result.returncode == 0


def dump_jvm_threads(pid, writer):
    if get_process_command_name(pid) not in {"java", "java.exe"}:
        return

    writer.line()
    writer.line(f"===== JVM {pid} command line =====")
    if not run_jcmd(pid, ["VM.command_line"], writer):
        writer.line(f"jcmd VM.command_line failed for JVM {pid}")

    writer.line()
    writer.line(f"===== JVM {pid} thread dump =====")
    if not run_jcmd(pid, ["Thread.print", "-l"], writer):
        writer.line(
            f"jcmd Thread.print failed for JVM {pid}; "
            "requesting a SIGQUIT thread dump instead."
        )
        try:
            os.kill(pid, signal.SIGQUIT)
        except ProcessLookupError:
            pass


def dump_diagnostics(command_process, diagnostic_delay_seconds, diagnostic_file):
    diagnostic_file.parent.mkdir(parents=True, exist_ok=True)
    with diagnostic_file.open("w", encoding="utf-8") as output_file:
        writer = DiagnosticWriter(output_file)
        try:
            descendant_pids = collect_descendant_pids(command_process.pid)
        except (OSError, subprocess.SubprocessError, ValueError) as error:
            descendant_pids = [command_process.pid]
            writer.line(f"Failed to collect descendant processes: {error}")

        writer.line("===== Client GameTest diagnostic dump =====")
        writer.line(f"Timestamp: {datetime.now().astimezone().isoformat()}")
        writer.line(f"Watched command PID: {command_process.pid}")
        writer.line(f"Diagnostic delay: {diagnostic_delay_seconds}s")
        writer.line()
        writer.line("===== Descendant process state =====")
        for pid in descendant_pids:
            try:
                write_process_state(pid, writer)
            except (OSError, subprocess.SubprocessError) as error:
                writer.line(f"Failed to read process state for PID {pid}: {error}")

        if shutil.which("jcmd") is None:
            writer.line()
            writer.line("jcmd is unavailable; JVM thread dumps could not be captured.")
        else:
            for pid in descendant_pids:
                try:
                    dump_jvm_threads(pid, writer)
                except (OSError, subprocess.SubprocessError) as error:
                    writer.line(f"Failed to dump JVM {pid}: {error}")

        writer.line()
        writer.line("===== End Client GameTest diagnostic dump =====")


def run_watchdog(
    stop_event,
    command_process,
    diagnostic_delay_seconds,
    diagnostic_file,
):
    if stop_event.wait(diagnostic_delay_seconds):
        return
    if command_process.poll() is None:
        try:
            dump_diagnostics(command_process, diagnostic_delay_seconds, diagnostic_file)
        except OSError as error:
            print(f"Failed to write JVM diagnostics: {error}", file=sys.stderr)


def normalize_exit_code(return_code):
    if return_code < 0:
        return 128 - return_code
    return return_code


def main():
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} command [args...]", file=sys.stderr)
        return 2

    try:
        diagnostic_delay_seconds = parse_positive_int_env(
            DIAGNOSTIC_DELAY_ENV,
            DEFAULT_DIAGNOSTIC_DELAY_SECONDS,
        )
    except ValueError as error:
        print(error, file=sys.stderr)
        return 2

    diagnostic_file = Path(os.environ.get(DIAGNOSTIC_FILE_ENV, DEFAULT_DIAGNOSTIC_FILE))
    try:
        diagnostic_file.unlink(missing_ok=True)
    except OSError as error:
        print(f"Failed to remove stale JVM diagnostics: {error}", file=sys.stderr)
        return 2

    command_process = subprocess.Popen(sys.argv[1:])
    stop_event = threading.Event()
    watchdog_thread = threading.Thread(
        target=run_watchdog,
        args=(
            stop_event,
            command_process,
            diagnostic_delay_seconds,
            diagnostic_file,
        ),
        name="Client GameTest diagnostic watchdog",
    )
    watchdog_thread.start()

    def forward_termination_signal(signum, _frame):
        if command_process.poll() is None:
            try:
                command_process.send_signal(signum)
            except ProcessLookupError:
                pass

    previous_sigterm_handler = signal.signal(signal.SIGTERM, forward_termination_signal)
    previous_sighup_handler = signal.signal(signal.SIGHUP, forward_termination_signal)
    try:
        return_code = command_process.wait()
    finally:
        stop_event.set()
        watchdog_thread.join()
        signal.signal(signal.SIGTERM, previous_sigterm_handler)
        signal.signal(signal.SIGHUP, previous_sighup_handler)

    return normalize_exit_code(return_code)


if __name__ == "__main__":
    sys.exit(main())
