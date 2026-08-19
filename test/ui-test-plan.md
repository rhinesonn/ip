# Caitlyn UI test plan

The `test-ui` skill runs each case in a fresh Caitlyn process. Expected output lines are required substrings and must appear in the listed order; the banner and separators are intentionally not repeated here.

The state-sensitive cases below deliberately interleave valid and invalid commands. Their later `list` and status assertions verify that rejected inputs do not corrupt the task list.

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

## Test case 2: Add a Deadline with arbitrary date text

Aim: Verify that a deadline preserves its `/by` text without converting it to a date/time object.

Input:

```text
deadline do homework /by no idea :-p
list
bye
```

Expected output:

```text
[D][ ] do homework (by: no idea :-p)
1.[D][ ] do homework (by: no idea :-p)
```

## Test case 3: Add an Event with start and end text

Aim: Verify that an event preserves both `/from` and `/to` values, including spaces and date-like text.

Input:

```text
event orientation week /from 4/10/2019 /to 11/10/2019
list
bye
```

Expected output:

```text
[E][ ] orientation week (from: 4/10/2019 to: 11/10/2019)
1.[E][ ] orientation week (from: 4/10/2019 to: 11/10/2019)
```

## Test case 4: Use polymorphic task storage and status changes

Aim: Verify that ToDo, Deadline, and Event objects coexist in one task list and can all be marked and unmarked through the common `Task` interface.

Input:

```text
todo read book
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
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
2.[D][ ] return book (by: Sunday)
3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
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
deadline submit report /by Friday
deadline missing deadline
event team meeting /from Monday /to 3pm
event /from Tuesday /to 4pm
list
bye
```

Expected output:

```text
[D][ ] submit report (by: Friday)
I beg your pardon, master. Please provide a deadline in the format: deadline task /by date.
[E][ ] team meeting (from: Monday to: 3pm)
I beg your pardon, master. Please provide an event in the format: event task /from start /to end.
Here are the tasks in your list:
1.[D][ ] submit report (by: Friday)
2.[E][ ] team meeting (from: Monday to: 3pm)
```

## Test case 10: Preserve task status after invalid mark commands

Aim: Verify that an out-of-range or non-numeric status command does not change existing task statuses.

Input:

```text
todo finish project
deadline review report /by Sunday
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
2.[D][ ] review report (by: Sunday)
I beg your pardon, master. Please provide a valid task number, for example: unmark 2.
[T][ ] finish project
1.[T][ ] finish project
2.[D][ ] review report (by: Sunday)
```
