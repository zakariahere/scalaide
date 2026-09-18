# Scala Learning with DeepSeek

Open **Scala → Scala Learning**, or right-click a Scala editor and choose
**Summarize This Scala File**. The independent Learning panel provides a summarize
button, cancellation, formatted learning notes and a copy button. It is separate
from the project/build Workbench.

## Setup

1. In **Learning settings…**, enter your DeepSeek API key in the masked field.
2. Leave the model as `deepseek-flash`, or enter another model available through
   your DeepSeek account. English and Java comparisons are the defaults; French
   and Scala-only explanations are also available.
3. Save settings, open a `.scala` source file, and click **Summarize this Scala file**.

The key is stored through IntelliJ Password Safe, using its configured credential
provider. Windows normally uses the platform's KeePass-based store. It is not
stored in the project's XML, plugin configuration, Git, or logs. Existing keys
are never populated into the settings field. A blank field preserves a saved key;
the forget checkbox removes it. `DEEPSEEK_API_KEY` from the IDE process environment
is a fallback when no key is saved. The development sandbox and regular IDE may
have separate Password Safe stores.

## What the button sends

An explicit click sends the **complete active Scala file snapshot**, including
comments and unsaved edits, to `https://api.deepseek.com/chat/completions`, along
with its base filename and the learning instructions. No directory path, sibling
file, project-wide search, Git history or previous summary is added. The source
is not saved or modified by this feature. A request never runs on file open,
selection change, settings save, or a timer.

The system prompt asks for concepts present in the source, concrete symbol
examples, useful Java comparisons, reminders, and one small practice exercise.
It treats source comments/string literals as data, distinguishes observations
from guesses and does not claim to know the user's learning history or mastery.
An AI explanation can still be wrong; native Scala services remain the source
of editor diagnostics and refactoring behavior.

Each result names its file and capture time. Editing during a request marks the
result as an older snapshot. Switching to a different tab does not relabel the
pending result. Notes stay in the panel for the project session; **Copy learning
notes** copies Markdown so it can be kept wherever the user records progress.
There is no automatic progress-file write or cross-file learning history yet.

## Request behavior

- Only real Scala `.scala` source editors are accepted. Empty files and files
  above 60,000 characters are rejected without silently truncating the source.
- One request at a time per panel. Cancellation invalidates late callbacks and
  cancels the HTTP future; closing the project disposes the request.
- 15-second connection timeout, 90-second overall deadline, 512 KiB response
  limit, 2,400 output-token limit, non-streaming output, thinking disabled.
- HTTP redirects and automatic application retries are disabled. Cancellation
  cannot undo work already processed or billed by DeepSeek.
- The panel explains invalid credentials, balance, rate limiting, invalid model,
  timeout and unavailable-service errors without exposing raw provider bodies.
- Returned Markdown is escaped before displaying a limited set of formatting.
  Raw HTML, images and links are not activated, and no model tools are enabled.
- This feature makes account-billed API requests when the user clicks; it does
  not rely on an OpenAI account, SDK or API key.

## Validation and references

The test suite uses a local HTTP server and fake test credentials. It exercises
the request shape, source boundary, provider errors, malformed/partial/oversized
responses, deadlines and cancellation without contacting DeepSeek. Platform
fixtures check unsaved snapshots, stale detection, non-Scala rejection, action
availability and inert rendering. A modal-dialog regression checks that settings
save finishes while the dialog is open without accessing Password Safe. A live
DeepSeek summary was also observed in the running IDE using a user-configured
key; no real credentials are used by the automated tests.

Provider/API references checked 2026-09-18:

- [DeepSeek first API call and current model identifiers](https://api-docs.deepseek.com/)
- [IntelliJ credential storage and background-thread requirements](https://plugins.jetbrains.com/docs/intellij/persisting-sensitive-data.html)
