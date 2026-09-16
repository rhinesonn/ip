# Caitlyn User Guide

## Within-period tasks

A within-period task is an action you complete once at any point during an inclusive window. It is distinct from an event that takes place from start to end.

```text
within DESCRIPTION /from START /to END
```

Use lowercase `within`, `/from`, and `/to`. Provide a nonempty description and both boundaries, with exactly one `/from` followed by exactly one `/to`. Surrounding whitespace and spaces or tabs between command parts are accepted; internal description whitespace is preserved. Standalone `/from` and `/to` are reserved and cannot appear in a command description. Words such as `/fromage`, pipes, and backslashes are ordinary description text. There is no quoting or escape syntax for commands.

### Dates, times, and boundaries

Each boundary requires a full date. These formats are accepted independently at either end:

| Format | Example |
| --- | --- |
| ISO date | `2027-01-15` |
| Day/month/year | `15/1/2027` |
| Date and compact 24-hour time | `2027-01-15 0900`, `15/1/2027 0900` |
| Date and colon-separated 24-hour time | `2027-01-15 09:00`, `15/1/2027 09:00` |
| ISO local date-time | `2027-01-15T09:00` |

Both endpoints are inclusive. A date-only start begins that day, and a date-only end includes the whole day. You may mix date-only and timed boundaries. Equal dates mean a whole-day window; equal timed endpoints mean a single instant. The start cannot be after the effective end.

Times use minute precision. ISO inputs with zero seconds/fractions, such as `2027-01-15T09:00:00.000`, are accepted and normalized. Nonzero seconds/fractions are rejected. Natural dates, time-only endpoints, and timezone offsets are not supported.

Past windows, duplicates, overlaps, and windows of any duration supported by the date parser are allowed. Completion is manual: you can mark or unmark at any time. There are no automatic expiry labels, reminders, completion timestamps, or window-editing commands.

### Examples and responses

The following output is the GUI response text. The CLI adds its existing indentation and separator lines.

Starting with an empty list:

```text
within collect certificate /from 2027-01-15 /to 2027-01-25
```

```text
Got it. I've added this task:
[W][ ] collect certificate (within: Jan 15 2027 to: Jan 25 2027)
Now you have 1 tasks in the list.
```

These additional examples show the task line included in the usual add confirmation:

| Command | Task line |
| --- | --- |
| `within collect certificate /from 15/1/2027 0900 /to 25/1/2027 17:00` | `[W][ ] collect certificate (within: Jan 15 2027 9:00 AM to: Jan 25 2027 5:00 PM)` |
| `within submit form /from 2027-01-15 0900 /to 2027-01-15` | `[W][ ] submit form (within: Jan 15 2027 9:00 AM to: Jan 15 2027)` |
| `within submit form /from 2027-01-15 /to 2027-01-15` | `[W][ ] submit form (within: Jan 15 2027 to: Jan 15 2027)` |
| `within press button /from 2027-01-15T09:00 /to 2027-01-15T09:00` | `[W][ ] press button (within: Jan 15 2027 9:00 AM to: Jan 15 2027 9:00 AM)` |

Within-period tasks support `list`, `find KEYWORD`, `mark NUMBER`, `unmark NUMBER`, and `delete NUMBER`. List order is insertion order; completed tasks remain visible. Search is case-insensitive, matches descriptions only, and retains original list numbers. Deletion renumbers the remaining tasks.

For the first example, `mark 1` responds:

```text
As you wish, master. I have marked this task as done:
[W][X] collect certificate (within: Jan 15 2027 to: Jan 25 2027)
```

### Invalid commands

Rejected commands do not change tasks or their saved file. Errors are checked in this order: load-failure protection, command structure, start date and precision, end date and precision, then window order. Only the first applicable error is shown.

| Invalid input | Exact response |
| --- | --- |
| `within task /from 2027-01-15` | `I beg your pardon, master. Please use: within task /from start /to end. Provide a description and both boundaries, with /from followed by /to exactly once.` |
| `within task /from 2027-02-30 /to 2027-03-05` | `I beg your pardon, master. Please provide a valid start date, for example: 2027-01-15 or 15/1/2027 0900.` |
| `within task /from 2027-01-15 /to tomorrow` | `I beg your pardon, master. Please provide a valid end date, for example: 2027-01-25 or 25/1/2027 1700.` |
| `within task /from 2027-01-15T09:00:01 /to 2027-01-25` | `I beg your pardon, master. The start time must use minute precision; seconds and fractional seconds must be zero.` |
| `within task /from 2027-01-15 /to 2027-01-25T17:00:00.001` | `I beg your pardon, master. The end time must use minute precision; seconds and fractional seconds must be zero.` |
| `within task /from 2027-01-26 /to 2027-01-25` | `I beg your pardon, master. The start of the window must not be after its end.` |
| `WITHIN task /from 2027-01-15 /to 2027-01-25` | `I humbly beg your pardon, master. I do not know how to carry out that command.` |

Missing descriptions, empty boundaries, repeated markers, reversed marker order, and malformed markers such as `/from2027-01-15` produce the structure error. Extra text in a boundary, such as `/to 2027-01-25 extra`, produces the corresponding invalid-date error.

### Saving and compatibility

Successful task changes are saved automatically before success is reported. If saving fails, the in-memory change is rolled back and Caitlyn responds:

```text
I beg your pardon, master. I could not save your tasks to disk.
```

The UTF-8 file is `data/duke.txt`, relative to the directory from which you run Caitlyn. A within-period record has exactly five fields:

```text
W | STATUS | DESCRIPTION | START | END
```

Status is `0` for incomplete and `1` for complete. Dates are saved as ISO dates or local date-times, preserving whether a time was supplied:

```text
W | 0 | collect certificate | 2027-01-15 | 2027-01-25
W | 1 | collect certificate | 2027-01-15T09:00 | 2027-01-25T17:00
W | 0 | submit form | 2027-01-15T09:00 | 2027-01-15
```

The existing storage escaping is retained: `\` becomes `\\`, `|` becomes `\|`, and line breaks use `\n` or `\r`. For example, `collect | file \backup` is stored as `collect \| file \\backup`. Loading accepts the existing date parser's formats and validates descriptions, precision, and window order. Command marker restrictions do not apply to text already inside a stored description.

Existing `T`, `D`, and `E` records retain their meaning and validation; no migration is required. Older Caitlyn builds cannot read `W` records. Do not open a file containing them in an older build: it may start empty after the loading error and overwrite saved tasks after a later change. There is no downgrade/export feature.

### Recovery after a load failure

A missing save file is normal first-time use. An invalid or unreadable save file starts an empty, protected session in both the CLI and GUI. No partial set of records is loaded and the original file is left unchanged. The startup message is:

```text
I could not read data/duke.txt. Task changes are disabled to protect your saved data. Repair the file or its access permissions, then restart Caitlyn.
```

`list`, `find`, and `bye` remain available. Every recognized task-changing command (`todo`, `deadline`, `event`, `within`, `mark`, `unmark`, `delete`) instead responds:

```text
I beg your pardon, master. Task changes are disabled because saved tasks could not be loaded. Repair data/duke.txt or its access permissions, then restart Caitlyn.
```

Open the saved file in a text editor and correct invalid field counts, task/status markers, dates, or window order, or restore access to the file if permissions prevented reading it. Restart Caitlyn after repairing it. Repairs made while the app is still open do not unlock that session. There is no built-in repair command or automatic backup. An ordinary save failure after a successful load does not permanently lock the session.

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Feature ABC

// Feature details


## Feature XYZ

// Feature details
