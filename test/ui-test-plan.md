# Caitlyn UI test plan

The `test-ui` skill runs each case in a fresh Caitlyn process. Expected output lines are required substrings and must appear in the listed order; the banner and separators are intentionally not repeated here.

The state-sensitive cases below deliberately interleave valid and invalid commands. Their later `list` and status assertions verify that rejected inputs do not corrupt the task list.

The cases below exercise the command-line entry point. For the JavaFX entry point, perform this manual smoke check after running `./gradlew run`: enter `todo buy milk`, press Enter, enter `list` and click **Send**, then enter `bye`. The transcript should show the user commands, the added task, the task list, and Caitlyn's farewell; the task count should change from `0 tasks saved` to `1 task saved`, and the input field and Send button should be disabled after `bye`.

## Test case 1: Add and list a ToDo

Aim: Verify that a date-free task is stored, displayed with the `T` type marker, and included in the task count.

Input:

```text
todo borrow book
list
bye
```

Expected output:

```text
[T][ ] borrow book
Now you have 1 tasks in the list.
1.[T][ ] borrow book
```

## Test case 2: Parse and format a Deadline date and time

Aim: Verify that a deadline parses the example day/month/year and compact 24-hour time into a date/time value, displays it in a different format, and saves it canonically.

Input:

```text
deadline return book /by 2/12/2019 1800
list
bye
```

Expected output:

```text
[D][ ] return book (by: Dec 2 2019 6:00 PM)
1.[D][ ] return book (by: Dec 2 2019 6:00 PM)
```

Expected `data/duke.txt` contents after the case:

```text
D | 0 | return book | 2019-12-02T18:00
```

## Test case 3: Parse and format an Event with date-only values

Aim: Verify that an event stores typed start and end dates and displays them in the requested readable format.

Input:

```text
event orientation week /from 2019-10-04 /to 2019-10-11
list
bye
```

Expected output:

```text
[E][ ] orientation week (from: Oct 4 2019 to: Oct 11 2019)
1.[E][ ] orientation week (from: Oct 4 2019 to: Oct 11 2019)
```

## Test case 4: Use polymorphic task storage and status changes

Aim: Verify that ToDo, Deadline, and Event objects coexist in one task list and can all be marked and unmarked through the common `Task` interface.

Input:

```text
todo read book
deadline return book /by 2019-10-20
event project meeting /from 2019-10-21 1400 /to 2019-10-21 1600
mark 1
unmark 1
list
bye
```

Expected output:

```text
[T][X] read book
[T][ ] read book
1.[T][ ] read book
2.[D][ ] return book (by: Oct 20 2019)
3.[E][ ] project meeting (from: Oct 21 2019 2:00 PM to: Oct 21 2019 4:00 PM)
```

## Test case 5: Reject an incomplete Deadline command

Aim: Verify that a malformed Deadline does not add a task and that the application remains usable.

Input:

```text
deadline missing deadline
list
bye
```

Expected output:

```text
Please provide a deadline in the format: deadline task /by date
Here are the tasks in your list:
```

## Test case 6: Reject an empty ToDo description

Aim: Verify that an empty ToDo is rejected with a Caitlyn exception and that no empty task is added.

Input:

```text
todo
list
bye
```

Expected output:

```text
I beg your pardon, master. I cannot prepare a task without a description.
Here are the tasks in your list:
```

## Test case 7: Reject an unknown command

Aim: Verify that an unrecognized command is rejected and that Caitlyn continues accepting commands.

Input:

```text
blah
list
bye
```

Expected output:

```text
I humbly beg your pardon, master. I do not know how to carry out that command.
Here are the tasks in your list:
```

## Test case 8: Preserve ToDo state after an empty description

Aim: Verify that an empty ToDo between two valid ToDos is rejected without creating an empty task or changing the task count.

Input:

```text
todo buy milk
todo
list
todo read book
list
bye
```

Expected output:

```text
[T][ ] buy milk
I beg your pardon, master. I cannot prepare a task without a description.
Here are the tasks in your list:
1.[T][ ] buy milk
[T][ ] read book
Now you have 2 tasks in the list.
1.[T][ ] buy milk
2.[T][ ] read book
```

## Test case 9: Preserve mixed task state after malformed task commands

Aim: Verify that malformed Deadline and Event commands do not add partial tasks between valid tasks.

Input:

```text
deadline submit report /by 2019-10-18
deadline missing deadline
event team meeting /from 2019-10-21 /to 2019-10-21 1500
event /from Tuesday /to 4pm
list
bye
```

Expected output:

```text
[D][ ] submit report (by: Oct 18 2019)
I beg your pardon, master. Please provide a deadline in the format: deadline task /by date.
[E][ ] team meeting (from: Oct 21 2019 to: Oct 21 2019 3:00 PM)
I beg your pardon, master. Please provide an event in the format: event task /from start /to end.
Here are the tasks in your list:
1.[D][ ] submit report (by: Oct 18 2019)
2.[E][ ] team meeting (from: Oct 21 2019 to: Oct 21 2019 3:00 PM)
```

## Test case 10: Preserve task status after invalid mark commands

Aim: Verify that an out-of-range or non-numeric status command does not change existing task statuses.

Input:

```text
todo finish project
deadline review report /by 2019-10-20
mark 1
mark 3
list
unmark two
unmark 1
list
bye
```

Expected output:

```text
[T][X] finish project
I beg your pardon, master, but I could not find task 3.
1.[T][X] finish project
2.[D][ ] review report (by: Oct 20 2019)
I beg your pardon, master. Please provide a valid task number, for example: unmark 2.
[T][ ] finish project
1.[T][ ] finish project
2.[D][ ] review report (by: Oct 20 2019)
```

## Test case 11: Delete a task and renumber the remaining list

Aim: Verify that a task can be removed by its displayed number, that the removed task is confirmed, and that the remaining tasks are renumbered.

Input:

```text
todo read book
deadline return book /by 2019-06-06
event project meeting /from 2019-08-06 1400 /to 2019-08-06 1600
todo join sports club
delete 3
list
bye
```

Expected output:

```text
Noted. I've removed this task:
[E][ ] project meeting (from: Aug 6 2019 2:00 PM to: Aug 6 2019 4:00 PM)
Now you have 3 tasks in the list.
1.[T][ ] read book
2.[D][ ] return book (by: Jun 6 2019)
3.[T][ ] join sports club
```

## Test case 12: Reject an invalid delete command

Aim: Verify that an invalid task number does not delete an existing task or change the task count.

Input:

```text
todo keep this task
delete 2
list
bye
```

Expected output:

```text
I beg your pardon, master, but I could not find task 2.
1.[T][ ] keep this task
```

## Test case 13: Save task changes automatically

Aim: Verify that adding, completing, and deleting tasks all use the automatic save path. After this case, `data/duke.txt` should contain the final task list in storage format.

Input:

```text
todo read book
deadline return book /by 2019-06-06
mark 1
delete 2
bye
```

Expected output:

```text
[T][ ] read book
[D][ ] return book (by: Jun 6 2019)
[T][X] read book
Now you have 1 tasks in the list.
```

Expected `data/duke.txt` contents after the case:

```text
T | 1 | read book
```

## Test case 14: Load saved tasks at startup

Aim: Verify that ToDos, deadlines, events, and their saved completion statuses are restored when Caitlyn starts.

Initial data/duke.txt contents:

```text
T | 1 | read book
D | 0 | return book | 2019-10-20
E | 0 | project meeting | 2019-10-21 | 2019-10-21T16:00
T | 1 | path \\backup \| notes
```

Input:

```text
list
bye
```

Expected output:

```text
Here are the tasks in your list:
1.[T][X] read book
2.[D][ ] return book (by: Oct 20 2019)
3.[E][ ] project meeting (from: Oct 21 2019 to: Oct 21 2019 4:00 PM)
4.[T][X] path \backup | notes
```

## Test case 15: Recover from malformed saved data

Aim: Verify that an invalid saved record does not crash Caitlyn and that the chatbot starts with an empty usable list.

Initial data/duke.txt contents:

```text
T | 2 | invalid status
```

Input:

```text
list
bye
```

Expected output:

```text
I could not read the saved tasks, so I am starting with an empty list.
Here are the tasks in your list:
```

## Test case 16: Preserve special characters during saving

Aim: Verify that pipes and backslashes in task fields are escaped when saved instead of being mistaken for storage separators.

Input:

```text
todo plan | review \backup
deadline send | mail /by 2019-10-15
event call | backup /from 2019-10-15 /to 2019-10-16
bye
```

Expected output:

```text
[T][ ] plan | review \backup
[D][ ] send | mail (by: Oct 15 2019)
[E][ ] call | backup (from: Oct 15 2019 to: Oct 16 2019)
```

Expected `data/duke.txt` contents after the case:

```text
T | 0 | plan \| review \\backup
D | 0 | send \| mail | 2019-10-15
E | 0 | call \| backup | 2019-10-15 | 2019-10-16
```

## Test case 17: Start without an existing data file

Aim: Verify that a first run works when both the `data` directory and `duke.txt` file are absent, then creates them when the first task is added.

Input:

```text
list
todo first-run task
bye
```

Expected output:

```text
Here are the tasks in your list:
[T][ ] first-run task
Now you have 1 tasks in the list.
```

Expected `data/duke.txt` contents after the case:

```text
T | 0 | first-run task
```

## Test case 18: Reject an invalid date

Aim: Verify that an impossible calendar date is rejected without crashing Caitlyn or adding a task.

Input:

```text
deadline invalid date /by 2019-02-30
list
bye
```

Expected output:

```text
Please use a valid date such as 2019-10-15 or 2/12/2019 1800.
Here are the tasks in your list:
```

## Test case 19: Exit immediately after the bye command

Aim: Verify that Caitlyn stops without waiting for another input line after the user enters `bye`.

Input:

```text
bye
```

Expected output:

```text
Farewell, master. It has been my pleasure to serve you.
```

## Test case 20: Find tasks by keyword

Aim: Verify that a keyword search is case-insensitive, returns only matching descriptions, preserves the original task numbers, and displays all task types in their normal format.

Input:

```text
todo read book
deadline return book /by 2019-06-06
todo buy milk
find BOOK
bye
```

Expected output:

```text
Here are the matching tasks in your list:
1.[T][ ] read book
2.[D][ ] return book (by: Jun 6 2019)
```

## Test case 21: Reject a find command without a keyword

Aim: Verify that a find command without a keyword is rejected and does not affect the task list.

Input:

```text
todo keep this task
find
list
bye
```

Expected output:

```text
Please provide a keyword, for example: find book.
1.[T][ ] keep this task
```
