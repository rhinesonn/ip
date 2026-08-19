# Caitlyn UI test plan

The `test-ui` skill runs each case in a fresh Caitlyn process. Expected output lines are required substrings and must appear in the listed order; the banner and separators are intentionally not repeated here.

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
