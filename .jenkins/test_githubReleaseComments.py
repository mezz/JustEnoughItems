from __future__ import annotations

import io
import json
import tempfile
import unittest
import urllib.error
from pathlib import Path
from unittest.mock import patch

import githubReleaseComments


class FakeResponse:
    def __init__(self, payload: dict[str, str]):
        self.body = json.dumps(payload).encode("utf-8")
        self.headers: dict[str, str] = {}

    def __enter__(self) -> FakeResponse:
        return self

    def __exit__(self, *args: object) -> None:
        return None

    def read(self) -> bytes:
        return self.body


def http_error(status: int) -> urllib.error.HTTPError:
    return urllib.error.HTTPError(
        "https://api.github.com/user",
        status,
        "temporary failure",
        {},
        io.BytesIO(b'{"message":"temporary failure"}'),
    )


def release(version: str, base: str, head: str) -> githubReleaseComments.Release:
    return githubReleaseComments.Release(
        repo="mezz/JustEnoughItems",
        project_name="JEI",
        version=version,
        minecraft_version="26.2",
        base=base,
        head=head,
        release_link_lines=(f"**Modrinth:** [NeoForge](https://example.invalid/{version})",),
        marker_prefix="jenkins-github-release-notifier",
        enhancement_labels=("enhancement",),
        review_delay_note="",
        skip_pr_comments=False,
        skip_issue_comments=False,
    )


class GitHubClientTest(unittest.TestCase):
    def test_authenticated_user_retries_transient_service_failure(self) -> None:
        client = githubReleaseComments.GitHubClient(
            "token",
            "https://api.github.com",
            False,
            retry_delays_seconds=(0, 0),
        )

        with patch.object(
            githubReleaseComments.urllib.request,
            "urlopen",
            side_effect=[http_error(503), http_error(503), FakeResponse({"login": "mezz-bot"})],
        ) as urlopen:
            login = client.authenticated_user_login()

        self.assertEqual("mezz-bot", login)
        self.assertEqual(3, urlopen.call_count)

    def test_comment_post_is_not_retried_after_service_failure(self) -> None:
        client = githubReleaseComments.GitHubClient(
            "token",
            "https://api.github.com",
            False,
            retry_delays_seconds=(0, 0),
        )

        with patch.object(
            githubReleaseComments.urllib.request,
            "urlopen",
            side_effect=http_error(503),
        ) as urlopen:
            with self.assertRaisesRegex(RuntimeError, "failed with HTTP 503"):
                client.create_issue_comment("mezz/JustEnoughItems", 4431, "Released")

        self.assertEqual(1, urlopen.call_count)


class NotificationStateTest(unittest.TestCase):
    def test_pending_releases_resume_from_the_last_completed_release(self) -> None:
        first = release("26.2.0.100", "unexpected-base", "b" * 40)
        second = release("26.2.0.101", "another-unexpected-base", "c" * 40)
        state = githubReleaseComments.NotificationState("a" * 40, ())
        state = githubReleaseComments.queue_release(state, first)
        state = githubReleaseComments.queue_release(state, second)

        self.assertEqual("a" * 40, state.releases[0].base)
        self.assertEqual("b" * 40, state.releases[1].base)

        with tempfile.TemporaryDirectory() as temporary_directory:
            state_path = Path(temporary_directory) / "release-comments.json"
            githubReleaseComments.write_notification_state(state_path, state)

            with patch.object(
                githubReleaseComments,
                "notify_release",
                side_effect=[None, RuntimeError("GitHub unavailable")],
            ):
                with self.assertRaisesRegex(RuntimeError, "GitHub unavailable"):
                    githubReleaseComments.notify_pending_releases(
                        githubReleaseComments.GitHubClient("token", "https://api.github.com", False),
                        Path(temporary_directory),
                        state_path,
                        state,
                    )

            partially_completed = githubReleaseComments.read_notification_state(state_path)
            self.assertEqual("b" * 40, partially_completed.last_notified_head)
            self.assertEqual((second.version,), tuple(item.version for item in partially_completed.releases))

            description = githubReleaseComments.state_description(partially_completed)
            restored = githubReleaseComments.state_from_description(description)
            self.assertEqual(partially_completed, restored)

            with patch.object(githubReleaseComments, "notify_release", return_value=None):
                completed = githubReleaseComments.notify_pending_releases(
                    githubReleaseComments.GitHubClient("token", "https://api.github.com", False),
                    Path(temporary_directory),
                    state_path,
                    restored,
                )

            self.assertEqual("c" * 40, completed.last_notified_head)
            self.assertEqual((), completed.releases)
            completed_description = githubReleaseComments.state_description(completed)
            self.assertEqual(completed, githubReleaseComments.state_from_description(completed_description))


if __name__ == "__main__":
    unittest.main()
